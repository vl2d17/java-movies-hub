package ru.practicum.moviehub.model;

public class Movie {
    private String title;
    private Integer id;
    private Integer year;
    private String director;

    public Movie(String title, Integer id, Integer year, String director) {
        this.title = title;
        this.id = id;
        this.year = year;
        this.director = director;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }
}
