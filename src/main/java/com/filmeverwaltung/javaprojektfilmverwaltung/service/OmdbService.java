package com.filmeverwaltung.javaprojektfilmverwaltung.service;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.HttpUtil;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.TranslationUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OmdbService
{

    private static final String BASE_URL = "https://www.omdbapi.com/";
    private static final Logger LOGGER = Logger.getLogger(OmdbService.class.getName());

    private final String apiKey;
    private final Gson gson = new Gson();
    private final TranslationUtil translationUtil = new TranslationUtil();

    public OmdbService(String apiKey)
    {
        this.apiKey = apiKey;
    }

    /**
     * Hauptmethode: Film anhand eines (deutschen oder englischen) Titels abrufen.
     */
    public Filmmodel getFilmByTitle(String title)
    {
        Filmmodel film = requestOmdb(title);

        if (film == null || "False".equalsIgnoreCase(film.getResponse()))
        {
            // Versuche eine Übersetzung in Englisch basierend auf der aktuellen UI-Sprache
            String uiLangCode = "de"; // default
            try {
                uiLangCode = com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil.getLanguage() == null ? "de" :
                        switch (com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil.getLanguage()) {
                            case EN -> "en";
                            case AR -> "ar";
                            case PL -> "pl";
                            default -> "de";
                        };
            } catch (Exception ex) {
                // Falls LanguageUtil Probleme macht, verwende Default
                uiLangCode = "de";
            }

            if (!"en".equalsIgnoreCase(uiLangCode)) {
                String translated = translationUtil.translate(title, uiLangCode, "en");
                if (translated != null && !translated.equalsIgnoreCase(title)) {
                    film = requestOmdb(translated);
                }
            }
        }

        return film;
    }

    /**
     * Lade Filmdetails anhand einer imdbID (i=)
     */
    public Filmmodel getFilmById(String imdbID) {
        try {
            // Wenn das ID-Format eindeutig TMDb ist (numeric) oder bereits 'tmdb_' prefixed,
            // dann direkt TMDb verwenden und OMDb-Aufruf vermeiden.
            if (imdbID != null && (imdbID.startsWith("tmdb_") || imdbID.matches("\\d+"))) {
                LOGGER.log(Level.INFO, "ID sieht nach TMDb aus, lade direkt von TMDb: " + imdbID);
                return getFilmFromTMDb(imdbID);
            }

            String encoded = URLEncoder.encode(imdbID, StandardCharsets.UTF_8);
            String url = BASE_URL + "?i=" + encoded + "&apikey=" + apiKey;

            String json = HttpUtil.get(url);
            LOGGER.log(Level.INFO, "OMDb API Response for ID " + imdbID + ": " + json);

            Filmmodel film = gson.fromJson(json, Filmmodel.class);

            if (film == null || "False".equalsIgnoreCase(film.getResponse())) {
                LOGGER.log(Level.WARNING, "OMDb lieferte keine Ergebnisse für ID: " + imdbID + ". Versuche TMDb.");
                return getFilmFromTMDb(imdbID);
            }

            return film;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Abrufen der Filmdaten für ID: " + imdbID, e);
            return null;
        }
    }

    private Filmmodel getFilmFromTMDb(String imdbID) {
        try {
            TMDbService tmdb = new TMDbService(com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig.TMDB_API_KEY);

            // Wenn die ID mit "tmdb_" geprefixet ist -> extrahiere die Zahl
            if (imdbID != null && imdbID.startsWith("tmdb_")) {
                String tmdbId = imdbID.substring("tmdb_".length());
                return tmdb.getMovieById(tmdbId);
            }

            // Reine numerische IDs -> TMDb-ID
            if (imdbID != null && imdbID.matches("\\d+")) {
                return tmdb.getMovieById(imdbID);
            }

            // Falls wir eine IMDb-ID (tt...) haben, nutze TMDb /find/ um die TMDb-ID zu ermitteln
            if (imdbID != null && imdbID.startsWith("tt")) {
                String findUrl = "https://api.themoviedb.org/3/find/" + URLEncoder.encode(imdbID, StandardCharsets.UTF_8) + "?api_key=" + com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig.TMDB_API_KEY + "&external_source=imdb_id";
                String json = HttpUtil.get(findUrl);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                if (root.has("movie_results")) {
                    JsonArray arr = root.getAsJsonArray("movie_results");
                    if (arr != null && arr.size() > 0) {
                        JsonObject first = arr.get(0).getAsJsonObject();
                        if (first.has("id")) {
                            String tmdbId = first.get("id").getAsString();
                            return tmdb.getMovieById(tmdbId);
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Abrufen von TMDb für ID: " + imdbID, e);
            return null;
        }
    }


    /**
     * Suche Filme anhand eines Titels (s=) mit Pagination (bis zu 6 Seiten = 60 Ergebnisse)
     * Fetched full details including genre for each film
     */
    public List<Filmmodel> searchByTitle(String query)
    {
        List<Filmmodel> allResults = new ArrayList<>();

        // Abrufen von bis zu 6 Seiten (OMDB gibt max. 10 pro Seite)
        for (int page = 1; page <= 6; page++)
        {
            try
            {
                String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String url = BASE_URL + "?s=" + encoded + "&page=" + page + "&apikey=" + apiKey + "&type=movie";

                String json = HttpUtil.get(url);
                Map<?, ?> root = gson.fromJson(json, Map.class);

                if (root == null || !"True".equalsIgnoreCase(String.valueOf(root.get("Response"))))
                {
                    break; // Keine weiteren Ergebnisse
                }

                Object searchObj = root.get("Search");
                if (!(searchObj instanceof List<?> list) || list.isEmpty())
                {
                    break; // Keine Ergebnisse auf dieser Seite
                }

                for (Object o : list)
                {
                    if (o instanceof Map<?, ?> m)
                    {
                        String imdbID = Objects.toString(m.get("imdbID"), "");

                        // Fetch full film details to get genre information
                        Filmmodel fullFilm = null;
                        if (!imdbID.isBlank())
                        {
                            fullFilm = getFilmById(imdbID);
                        }

                        // Use full film data if available, otherwise create basic entry
                        Filmmodel fm;
                        if (fullFilm != null && !"False".equalsIgnoreCase(fullFilm.getResponse()))
                        {
                            fm = fullFilm;
                        } else {
                            // Fallback: Create basic film entry if full details unavailable
                            fm = new Filmmodel(
                                    Objects.toString(m.get("Title"), ""),
                                    Objects.toString(m.get("Year"), ""),
                                    null,
                                    null
                            );
                            fm.setImdbID(imdbID);
                            String poster = Objects.toString(m.get("Poster"), "");
                            if (!poster.equalsIgnoreCase("N/A"))
                            {
                                fm.setPoster(poster);
                            }
                            // Set default genre if not available
                            fm.setGenre("N/A");
                        }

                        allResults.add(fm);
                    }
                }

            } catch (Exception e)
            {
                e.printStackTrace();
                break; // Bei Fehler abbrechen
            }
        }

        return allResults;
    }

    /**
     * Anfrage an die OMDb API.
     */
    private Filmmodel requestOmdb(String title)
    {
        try
        {
            String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = BASE_URL + "?t=" + encoded + "&apikey=" + apiKey;

            String json = HttpUtil.get(url);
            return gson.fromJson(json, Filmmodel.class);

        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
