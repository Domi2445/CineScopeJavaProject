package com.filmeverwaltung.javaprojektfilmverwaltung;

public final class ApiConfig
{
    public static final String OMDB_API_KEY;
    public static final String TMDB_API_KEY;

    static
    {
        Config config = Config.load();
        OMDB_API_KEY = (config != null && config.api != null && config.api.omdb != null) ? config.api.omdb : "";
        TMDB_API_KEY = (config != null && config.api != null && config.api.tmdb != null) ? config.api.tmdb : "";
    }

    /**
     * Gibt die TMDB-Sprache basierend auf der aktuellen UI-Sprache zurück
     */
    public static String getTMDBLanguage() {
        return switch (com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil.getLanguage()) {
            case DE -> "de-DE";
            case EN -> "en-US";
            case AR -> "ar-SA";
            case PL -> "pl-PL";
        };
    }
}
