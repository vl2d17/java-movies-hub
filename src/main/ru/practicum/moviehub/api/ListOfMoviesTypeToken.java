package ru.practicum.moviehub.api;

import com.google.gson.reflect.TypeToken;
import ru.practicum.moviehub.model.Movie;

import java.lang.reflect.Type;
import java.util.List;

public class ListOfMoviesTypeToken extends TypeToken<List<Movie>> {
    private static final Type TYPE = new TypeToken<List<Movie>>() {
    }.getType();

    public static Type get() {
        return TYPE;
    }
}