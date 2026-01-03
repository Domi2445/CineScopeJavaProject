package com.filmeverwaltung.javaprojektfilmverwaltung;

public final class ApiConfig {
    public static final String OMDB_API_KEY;

    static {
        Config config = Config.load();
        OMDB_API_KEY = config.api.omdb;
    }
}

