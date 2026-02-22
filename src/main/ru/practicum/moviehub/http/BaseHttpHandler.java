package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.ErrorResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8";
    private static final Gson gson = new Gson();

    protected void sendJson(HttpExchange ex, int status, Object data) throws IOException {
        String json = gson.toJson(data);
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, responseBytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(responseBytes);
            os.flush();
        }
    }

    protected void sendNoContent(HttpExchange ex) throws IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
        ex.getResponseBody().close();
    }


    protected void sendError(HttpExchange ex, int status, String message) throws IOException {
        String errorText = switch (status) {
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
        sendJson(ex, status, new ErrorResponse(errorText, message, status));
    }

    protected void sendMethodNotAllowed(HttpExchange ex) throws IOException {
        sendError(ex, 405, "Method not allowed");
    }

    protected void sendNotFound(HttpExchange ex, String message) throws IOException {
        sendError(ex, 404, message);
    }

    protected void sendBadRequest(HttpExchange ex, String message) throws IOException {
        sendError(ex, 400, message);
    }

    protected void sendConflict(HttpExchange ex, String message) throws IOException {
        sendError(ex, 409, message);
    }
}