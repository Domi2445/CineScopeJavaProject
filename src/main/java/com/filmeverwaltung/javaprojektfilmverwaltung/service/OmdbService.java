package com.filmeverwaltung.javaprojektfilmverwaltung.service;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OmdbService {

    private static final String BASE_URL = "https://www.omdbapi.com/";
    private static final String TRANSLATE_URL = "https://libretranslate.de/translate";

    private final String apiKey;
    private final Gson gson = new Gson();

    public OmdbService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Hauptmethode: Film anhand eines (deutschen oder englischen) Titels abrufen.
     */
    public Filmmodel getFilmByTitle(String title) {
        // 1. Versuch: Originaltitel
        Filmmodel film = requestOmdb(title);

        if (film == null || "False".equalsIgnoreCase(film.getResponse())) {
            // 2. Versuch: Übersetzen
            String translated = translateToEnglish(title);

            if (!translated.equalsIgnoreCase(title)) {
                film = requestOmdb(translated);
            }
        }

        return film;
    }

    /**
     * Lade Filmdetails anhand einer imdbID (i=)
     */
    public Filmmodel getFilmById(String imdbID) {
        try {
            String encoded = URLEncoder.encode(imdbID, StandardCharsets.UTF_8);
            String urlString = BASE_URL + "?i=" + encoded + "&apikey=" + apiKey;
            String json = sendGetRequest(urlString);
            return gson.fromJson(json, Filmmodel.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Suche: mehrere Treffer anhand einer Query (OMDb 's=' Parameter).
     */
    public List<Filmmodel> searchByTitle(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String urlString = BASE_URL + "?s=" + encoded + "&apikey=" + apiKey + "&type=movie";
            String json = sendGetRequest(urlString);

            Map<?, ?> root = gson.fromJson(json, Map.class);
            if (root == null) return Collections.emptyList();
            Object resp = root.get("Response");
            if (resp == null || "False".equalsIgnoreCase(String.valueOf(resp))) {
                return Collections.emptyList();
            }

            Object searchObj = root.get("Search");
            if (!(searchObj instanceof List)) return Collections.emptyList();

            List<?> searchList = (List<?>) searchObj;
            List<Filmmodel> results = new ArrayList<>();
            for (Object o : searchList) {
                if (o instanceof Map) {
                    Map<?, ?> m = (Map<?, ?>) o;
                    String title = Objects.toString(m.get("Title"), "");
                    String year = Objects.toString(m.get("Year"), "");
                    String imdbID = Objects.toString(m.get("imdbID"), "");
                    String poster = Objects.toString(m.get("Poster"), "");
                    // Erzeuge Filmmodel und setze imdbID/poster wenn vorhanden
                    Filmmodel fm = new Filmmodel(title, year, null, null);
                    if (!imdbID.isBlank()) fm.setImdbID(imdbID);
                    if (!poster.isBlank() && !poster.equalsIgnoreCase("N/A")) fm.setPoster(poster);
                    results.add(fm);
                }
            }
            return results;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Anfrage an die OMDb API.
     */
    private Filmmodel requestOmdb(String title) {
        try {
            String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String urlString = BASE_URL + "?t=" + encoded + "&apikey=" + apiKey;

            String json = sendGetRequest(urlString);
            return gson.fromJson(json, Filmmodel.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Übersetzt deutschen Text ins Englische über LibreTranslate.
     */
    private String translateToEnglish(String text) {
        try {
            URL url = new URL(TRANSLATE_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String jsonInput = """
            {
                "q": "%s",
                "source": "de",
                "target": "en",
                "format": "text"
            }
            """.formatted(text);

            try (OutputStream os = con.getOutputStream()) {
                os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
            }

            String response = readResponse(con);
            Map<?, ?> json = gson.fromJson(response, Map.class);

            return json.get("translatedText").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return text; // Fallback
        }
    }

    /**
     * GET‑Request senden.
     */
    private String sendGetRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return readResponse(con);
    }

    /**
     * Antwort auslesen.
     */
    private String readResponse(HttpURLConnection con) throws Exception {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)
        );

        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        reader.close();
        return response.toString();
    }
}