package com.filmeverwaltung.javaprojektfilmverwaltung.service;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.HttpUtil;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.TranslationUtil;
import com.google.gson.Gson;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OmdbService
{

    private static final String BASE_URL = "https://www.omdbapi.com/";

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
            String translated = translationUtil.translate(title, "de", "en");

            if (!translated.equalsIgnoreCase(title))
            {
                film = requestOmdb(translated);
            }
        }

        return film;
    }

    /**
     * Lade Filmdetails anhand einer imdbID (i=)
     */
    public Filmmodel getFilmById(String imdbID)
    {
        try
        {
            String encoded = URLEncoder.encode(imdbID, StandardCharsets.UTF_8);
            String url = BASE_URL + "?i=" + encoded + "&apikey=" + apiKey;

            String json = HttpUtil.get(url);
            return gson.fromJson(json, Filmmodel.class);

        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Suche Filme anhand eines Titels (s=) mit Pagination (bis zu 3 Seiten = 30 Ergebnisse)
     */
    public List<Filmmodel> searchByTitle(String query)
    {
        List<Filmmodel> allResults = new ArrayList<>();

        // Abrufen von bis zu 3 Seiten (OMDB gibt max. 10 pro Seite)
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
                        Filmmodel fm = new Filmmodel(Objects.toString(m.get("Title"), ""), Objects.toString(m.get("Year"), ""), null, null);

                        String imdbID = Objects.toString(m.get("imdbID"), "");
                        String poster = Objects.toString(m.get("Poster"), "");

                        if (!imdbID.isBlank()) fm.setImdbID(imdbID);
                        if (!poster.equalsIgnoreCase("N/A")) fm.setPoster(poster);

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
