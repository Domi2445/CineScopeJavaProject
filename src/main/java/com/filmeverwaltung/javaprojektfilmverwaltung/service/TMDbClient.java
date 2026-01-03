package com.filmeverwaltung.javaprojektfilmverwaltung.service;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Einfacher TMDb-Client zum Nachladen von ergänzenden Informationen.
 * Beispiele: Suche per IMDb-ID (find endpoint) und Laden von Movie-Details.
 */
public class TMDbClient {
    private static final String BASE = "https://api.themoviedb.org/3";

    private final String apiKey;
    private final HttpClient http;
    private final Gson gson;

    public TMDbClient(String apiKey) {
        this.apiKey = (apiKey == null) ? "" : apiKey;
        this.http = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public TMDbClient() {
        this(ApiConfig.TMDB_API_KEY);
    }

    /**
     * Findet TMDb-Einträge anhand einer externen imdbId. Rückgabe: Rohes JSON (JsonObject) mit Feldern wie "movie_results".
     */
    public JsonObject findByImdbId(String imdbId) throws IOException, InterruptedException {
        if (imdbId == null || imdbId.isBlank()) return null;
        String encoded = URLEncoder.encode(imdbId, StandardCharsets.UTF_8);
        String url = String.format("%s/find/%s?api_key=%s&external_source=imdb_id", BASE, encoded, apiKey);
        return sendGet(url);
    }

    /**
     * Holt Detaildaten für einen TMDb-Film.
     */
    public JsonObject getMovieDetails(int tmdbId, String language) throws IOException, InterruptedException {
        String lang = (language == null || language.isBlank()) ? "en" : language;
        String url = String.format("%s/movie/%d?api_key=%s&language=%s", BASE, tmdbId, apiKey, URLEncoder.encode(lang, StandardCharsets.UTF_8));
        return sendGet(url);
    }

    private JsonObject sendGet(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
            return gson.fromJson(resp.body(), JsonObject.class);
        }
        return null;
    }

    // Beispiel:
    // TMDbClient client = new TMDbClient();
    // JsonObject found = client.findByImdbId("tt0111161");
    // JsonArray results = found.getAsJsonArray("movie_results");
}

