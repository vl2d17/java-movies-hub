package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;
import ru.practicum.moviehub.api.ListOfMoviesTypeToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
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
        // Читаем тело запроса
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        try {

            List<Movie> newMovies = gson.fromJson(body, ListOfMoviesTypeToken.get());

            if (newMovies == null || newMovies.isEmpty()) {
                sendBadRequest(ex, "No movies provided");
                return;
            }


            for (Movie movie : newMovies) {
                store.addMovie(movie);
            }

            sendJson(ex, 201, newMovies);
        } catch (Exception e) {
            sendBadRequest(ex, "Invalid JSON format");
        }
    }
}