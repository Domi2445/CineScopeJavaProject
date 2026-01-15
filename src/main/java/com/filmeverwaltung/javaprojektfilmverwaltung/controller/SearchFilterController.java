package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import java.util.List;
import java.util.Arrays;

public class SearchFilterController {

    public static final List<String> AVAILABLE_GENRES = Arrays.asList(
            "Alle Genres",
            "Action", "Adventure", "Animation", "Biography", "Comedy",
            "Crime", "Documentary", "Drama", "Family", "Fantasy",
            "Film-Noir", "History", "Horror", "Musical", "Mystery",
            "Romance", "Sci-Fi", "Sport", "Thriller", "War", "Western"
    );

    public List<String> getAvailableGenres() {
        return AVAILABLE_GENRES;
    }

    public boolean filterByGenre(String filmGenres, String selectedGenre) {
        // If "Alle Genres" is selected, show all films
        if (selectedGenre == null || "Alle Genres".equalsIgnoreCase(selectedGenre)) {
            return true;
        }

        // If film has no genre data, exclude it
        if (filmGenres == null || filmGenres.isEmpty() || "N/A".equalsIgnoreCase(filmGenres)) {
            return false;
        }

        // Split comma-separated genres and check if selected genre is in the list
        String[] genreArray = filmGenres.split(",");
        for (String genre : genreArray) {
            if (genre.trim().equalsIgnoreCase(selectedGenre)) {
                return true;
            }
        }

        return false;
    }
}
