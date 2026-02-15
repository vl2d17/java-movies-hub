package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import ru.practicum.moviehub.api.ListOfMoviesTypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class MoviesHandler extends BaseHttpHandler {
    private static final int MAX_REQUEST_SIZE = 1_000_000;
    private static final int FIRST_MOVIE_YEAR = 1888;
    private final Gson gson;
    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if (path.equals("/movies")) {
            handleMoviesCollection(ex, method);
        } else {
            sendNotFound(ex, "Endpoint not found");
        }
    }

    private void handleMoviesCollection(HttpExchange ex, String method) throws IOException {
        switch (method) {
            case "GET":
                handleGetMovies(ex);
                break;
            case "POST":
                handlePostMovies(ex);
                break;
            default:
                sendMethodNotAllowed(ex);
        }
    }

    private void handleGetMovies(HttpExchange ex) throws IOException {
        var movies = store.getAllMovies();
        sendJson(ex, 200, movies);
    }

    private void handlePostMovies(HttpExchange ex) throws IOException {

        String contentType = ex.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith
                ("application/json")) {
            sendBadRequest(ex, "Content-Type must be application/json");
            return;
        }

        byte[] bodyBytes = ex.getRequestBody().readAllBytes();
        if (bodyBytes.length > MAX_REQUEST_SIZE) {
            sendBadRequest(ex, "Request body too large. Maximum size is " + MAX_REQUEST_SIZE
                    + " bytes.");
            return;
        }
        if (bodyBytes.length == 0) {
            sendBadRequest(ex, "Request body is empty");
            return;
        }

        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        try {
            List<Movie> newMovies = gson.fromJson(body, ListOfMoviesTypeToken.get());

            if (newMovies == null || newMovies.isEmpty()) {
                sendBadRequest(ex, "No movies provided");
                return;
            }

            for (Movie movie : newMovies) {
                validateMovie(movie);
            }
            for (Movie movie : newMovies) {
                store.addMovie(movie);
            }

            sendJson(ex, 201, newMovies);

        } catch (JsonSyntaxException e) {
            sendBadRequest(ex, "Invalid JSON format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            sendBadRequest(ex, "Invalid movie data: " + e.getMessage());
        } catch (Exception e) {
            sendError(ex, 500, "Server error: " + e.getMessage());
        }
    }

    private void validateMovie(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }

        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Movie title cannot be empty");
        }
        if (movie.getYear() <= FIRST_MOVIE_YEAR) {
            throw new IllegalArgumentException("Movie year cannot be earlier than " +
                    FIRST_MOVIE_YEAR + " (the birth of cinema)");
        }

        int currentYear = java.time.Year.now().getValue();
        if (movie.getYear() < FIRST_MOVIE_YEAR) {
            throw new IllegalArgumentException("Movie year cannot be earlier than " + FIRST_MOVIE_YEAR
                    + " (the birth of cinema)");
        }

        if (movie.getYear() > currentYear + 5) {
            throw new IllegalArgumentException("Movie year cannot be in the distant future");
        }
    }
}