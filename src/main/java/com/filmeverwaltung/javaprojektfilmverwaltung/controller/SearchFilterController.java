package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import java.util.Arrays;
import java.util.List;

public class SearchFilterController {

    public static final List<String> AVAILABLE_GENRES = Arrays.asList(
            "Alle Genres",
            "Action", "Adventure", "Animation", "Biography", "Comedy",
            "Crime", "Documentary", "Drama", "Family", "Fantasy",
            "Film-Noir", "History", "Horror", "Musical", "Mystery",
            "Romance", "Sci-Fi", "Sport", "Thriller", "War", "Western"
    );

    public static final List<String> AVAILABLE_LANGUAGES = Arrays.asList(
            "Alle Sprachen",
            "English", "German", "French", "Spanish", "Italian",
            "Japanese", "Chinese", "Korean", "Hindi", "Portuguese",
            "Russian", "Arabic", "Turkish", "Dutch", "Swedish",
            "Polish", "Danish", "Finnish", "Norwegian", "Czech"
    );


    public static final List<String> AVAILABLE_RATINGS = Arrays.asList(
            "Alle Bewertungen",
            "6.0", "6.5", "7.0", "7.5", "8.0", "8.5", "9.0"
    );


    public static final List<String> AVAILABLE_RUNTIMES = Arrays.asList(
            "Alle Laufzeiten",
            "Unter 90 Min",
            "90-120 Min",
            "120-150 Min",
            "Über 150 Min"
    );

    public List<String> getAvailableGenres() {
        return AVAILABLE_GENRES;
    }

    public List<String> getAvailableLanguages() {
        return AVAILABLE_LANGUAGES;
    }

    public List<String> getAvailableRatings() {
        return AVAILABLE_RATINGS;
    }

    public List<String> getAvailableRuntimes() {
        return AVAILABLE_RUNTIMES;
    }

    public boolean filterByGenre(String filmGenres, String selectedGenre) {
        if (selectedGenre == null || "Alle Genres".equalsIgnoreCase(selectedGenre)) return true;
        if (filmGenres == null || filmGenres.isBlank() || "N/A".equalsIgnoreCase(filmGenres)) return false;
        for (String g : filmGenres.split(",")) {
            if (g.trim().equalsIgnoreCase(selectedGenre)) return true;
        }
        return false;
    }

    public boolean filterByLanguage(String filmLanguages, String selectedLanguage) {
        if (selectedLanguage == null || "Alle Sprachen".equalsIgnoreCase(selectedLanguage)) return true;
        if (filmLanguages == null || filmLanguages.isBlank() || "N/A".equalsIgnoreCase(filmLanguages)) return false;
        for (String l : filmLanguages.split(",")) {
            if (l.trim().equalsIgnoreCase(selectedLanguage)) return true;
        }
        return false;
    }


    public boolean filterByMinRating(String imdbRating, String selectedRating) {
        if (selectedRating == null || "Alle Bewertungen".equalsIgnoreCase(selectedRating)) {
            return true;
        }
        if (imdbRating == null || imdbRating.isBlank() || "N/A".equalsIgnoreCase(imdbRating)) {
            return false;
        }
        try {
            double minThreshold = Double.parseDouble(selectedRating);
            double filmRating = Double.parseDouble(imdbRating);
            return filmRating >= minThreshold;
        } catch (NumberFormatException ex) {
            return false;
        }
    }


    public boolean filterByRuntime(int runtimeMinutes, String selectedRuntime) {
        if (selectedRuntime == null || "Alle Laufzeiten".equalsIgnoreCase(selectedRuntime)) {
            return true;
        }
        if (runtimeMinutes < 0) {
            return false;
        }

        return switch (selectedRuntime) {
            case "Unter 90 Min" -> runtimeMinutes < 90;
            case "90-120 Min" -> runtimeMinutes >= 90 && runtimeMinutes <= 120;
            case "120-150 Min" -> runtimeMinutes > 120 && runtimeMinutes <= 150;
            case "Über 150 Min" -> runtimeMinutes > 150;
            default -> true;
        };
    }
}
