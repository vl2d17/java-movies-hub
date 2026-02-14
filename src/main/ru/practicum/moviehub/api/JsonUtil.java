package ru.practicum.moviehub.api;

import ru.practicum.moviehub.model.Movie;


import java.util.List;

public class JsonUtil {

    public static String toJson(Object obj) {
        if (obj instanceof List) {
            return listToJson((List<?>) obj);
        } else if (obj instanceof Movie) {
            return movieToJson((Movie) obj);
        } else if (obj instanceof ErrorResponse) {
            return errorToJson((ErrorResponse) obj);
        }
        return "{}";
    }

    private static String listToJson(List<?> list) {
        if (list.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            json.append(toJson(list.get(i)));
            if (i < list.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String movieToJson(Movie movie) {
        return String.format(
                "{\"id\":%d,\"title\":\"%s\",\"year\":%d,\"director\":\"%s\"}",
                movie.getId(),
                escapeJson(movie.getTitle()),
                movie.getYear(),
                escapeJson(movie.getDirector())
        );
    }

    private static String errorToJson(ErrorResponse error) {
        return String.format(
                "{\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}",
                escapeJson(error.getError()),
                escapeJson(error.getMessage()),
                error.getStatus()
        );
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}
