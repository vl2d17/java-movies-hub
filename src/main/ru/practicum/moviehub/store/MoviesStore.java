package ru.practicum.moviehub.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import ru.practicum.moviehub.model.Movie;

public class MoviesStore {
    private final Map<Integer, Movie> movies = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies.values());
    }

    public Optional<Movie> getMovie(Integer id) {
        return Optional.ofNullable(movies.get(id));
    }

    public Movie addMovie(Movie movie) {
        long id = idGenerator.getAndIncrement();
        movie.setId((int) id);
        movies.put((int) id, movie);
        return movie;
    }

    public Optional<Movie> updateMovie(Integer id, Movie movie) {
        if (movies.containsKey(id)) {
            movie.setId(id);
            movies.put(id, movie);
            return Optional.of(movie);
        }
        return Optional.empty();
    }

    public boolean deleteMovie(Integer id) {
        return movies.remove(id) != null;
    }

    public void clear() {
        movies.clear();
        idGenerator.set(1);
    }
}
