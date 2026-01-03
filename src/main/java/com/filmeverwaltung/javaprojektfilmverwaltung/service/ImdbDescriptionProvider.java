package com.filmeverwaltung.javaprojektfilmverwaltung.service;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Hilfsklasse, die ggf. fehlende Beschreibungen (Plot) über die OMDb/IMDb oder TMDb nachlädt.
 */
public class ImdbDescriptionProvider {

    private final OmdbService omdbService;
    private final TMDbClient tmdbClient;

    public ImdbDescriptionProvider() {
        this.omdbService = new OmdbService(ApiConfig.OMDB_API_KEY);
        this.tmdbClient = new TMDbClient(ApiConfig.TMDB_API_KEY);
    }

    /**
     * Versucht, für die gegebene imdbID eine Plot-Beschreibung zu laden.
     * Zuerst OMDb, falls keine Beschreibung vorhanden -> TMDb (overview).
     */
    public String fetchPlotByImdbId(String imdbId) {
        if (imdbId == null || imdbId.isBlank()) return null;

        try {
            // 1) OMDb
            Filmmodel full = omdbService.getFilmById(imdbId);
            if (full != null) {
                String plot = full.getPlot();
                if (plot != null && !plot.isBlank() && !"N/A".equalsIgnoreCase(plot)) {
                    return plot;
                }
            }

            // 2) TMDb (falls API-Key gesetzt)
            if (ApiConfig.TMDB_API_KEY != null && !ApiConfig.TMDB_API_KEY.isBlank()) {
                try {
                    JsonObject found = tmdbClient.findByImdbId(imdbId);
                    if (found != null && found.has("movie_results")) {
                        JsonArray arr = found.getAsJsonArray("movie_results");
                        if (arr != null && arr.size() > 0) {
                            JsonObject first = arr.get(0).getAsJsonObject();
                            if (first.has("id")) {
                                int tmdbId = first.get("id").getAsInt(); // TMDb Movie ID
                                JsonObject details = tmdbClient.getMovieDetails(tmdbId, "de"); // versuche deutsche Beschreibung
                                if (details != null && details.has("overview"))
                                {
                                    String overview = details.get("overview").getAsString();
                                    if (overview != null && !overview.isBlank()) return overview;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Fehler beim TMDb-Abgleich: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Beschreibung: " + e.getMessage());
        }

        return null;
    }
}
