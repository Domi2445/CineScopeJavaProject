package com.filmeverwaltung.javaprojektfilmverwaltung;

public final class ApiConfig
{
    public static final String OMDB_API_KEY;
    public static final String TMDB_API_KEY;
    public static final String TMDB_LANGUAGE;

    static
    {
        Config config = Config.load();
        OMDB_API_KEY = (config != null && config.api != null && config.api.omdb != null) ? config.api.omdb : "";
        TMDB_API_KEY = (config != null && config.api != null && config.api.tmdb != null) ? config.api.tmdb : "";
        TMDB_LANGUAGE = (config != null && config.api != null && config.api.tmdbLanguage != null) ? config.api.tmdbLanguage : "de-DE";
    }
}
