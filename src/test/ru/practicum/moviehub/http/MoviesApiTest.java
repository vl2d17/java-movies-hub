package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.store.MoviesStore;
import ru.practicum.moviehub.model.Movie;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static MoviesStore store;
    private static HttpClient client;
    private static final Gson gson = new Gson();

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(store, 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void setUp() {
        store.clear();
    }

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());
        assertEquals("application/json; charset=UTF-8",
                resp.headers().firstValue("Content-Type").orElse(""));

        String body = resp.body().trim();
        assertEquals("[]", body);
    }

    @Test
    void getMovies_withData_returnsMovies() throws Exception {

        Movie movie1 = new Movie("Начало", 120, 2010, "Нолан К.");
        Movie movie2 = new Movie("Матрица", 130, 1999, "Вачовски");
        store.addMovie(movie1);
        store.addMovie(movie2);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"));
        assertTrue(body.contains("Начало"));
        assertTrue(body.contains("Матрица"));
        assertTrue(body.contains("2010"));
        assertTrue(body.contains("1999"));
    }

    @Test
    void postMovies_withInvalidJson_returns400() throws Exception {
        String invalidJson = "{this is not valid json}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals(400, error.getStatus());
        assertTrue(error.getMessage().contains("Invalid JSON format"));
    }

    @Test
    void postMovies_withEmptyBody_returns400() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals(400, error.getStatus());
        assertTrue(error.getMessage().contains("Request body is empty") ||
                error.getMessage().contains("No movies provided"));
    }

    @Test
    void postMovies_withWrongContentType_returns400() throws Exception {
        String validJson = "[{\"title\":\"Начало\",\"duration\":120,\"year\":2010,\"director\":\"Нолан К.\"}]";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(validJson))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals(400, error.getStatus());
        assertTrue(error.getMessage().contains("Content-Type must be application/json"));
    }

    @Test
    void postMovies_withEmptyMovieList_returns400() throws Exception {
        String emptyListJson = "[]";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(emptyListJson))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals(400, error.getStatus());
        assertTrue(error.getMessage().contains("No movies provided"));
    }

    @Test
    void postMovies_withNullMovie_returns400() throws Exception {
        String jsonWithNull = "[null]";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonWithNull))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals(400, error.getStatus());
        assertTrue(error.getMessage().contains("Movie cannot be null"));
    }

    @Test
    void postMovies_withInvalidMovieData_returns400() throws Exception {

        String invalidMovieJson = "[{\"title\":\"\",\"duration\":120,\"year\":2010,\"director\":\"Нолан К.\"}]";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidMovieJson))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals(400, error.getStatus());
        assertTrue(error.getMessage().contains("title cannot be empty"));
    }

    @Test
    void postMovies_withNegativeYear_returns400() throws Exception {
        String invalidYearJson = "[{\"title\":\"Начало\",\"duration\":120,\"year\":-2010,\"director\":\"Нолан К.\"}]";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidYearJson))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals(400, error.getStatus());
        System.out.println("Actual error message: " + error.getMessage());
        assertTrue(error.getMessage().contains("year"));
    }

    @Test
    void postMovies_withTooEarlyYear_returns400() throws Exception {
        String invalidYearJson = "[{\"title\":\"Начало\",\"duration\":120,\"year\":1800,\"director\":\"Нолан К.\"}]";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidYearJson))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        ErrorResponse error = gson.fromJson(resp.body(), ErrorResponse.class);
        assertEquals(400, error.getStatus());


        assertEquals("Invalid movie data: Movie year cannot be earlier than 1888 (the birth of cinema)",
                error.getMessage());

    }

}
