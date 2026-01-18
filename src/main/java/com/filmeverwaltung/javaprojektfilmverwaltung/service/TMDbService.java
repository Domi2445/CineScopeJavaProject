package com.filmeverwaltung.javaprojektfilmverwaltung.service;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.HttpUtil;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service für TMDb API - lädt Streaming-Anbieter, ähnliche Filme und weitere Informationen
 */
public class TMDbService {

    private static final Logger LOGGER = Logger.getLogger(TMDbService.class.getName());
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private final String apiKey;

    public TMDbService(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Sucht einen Film nach Titel und gibt die TMDb ID zurück
     */
    public String getMovieIdByTitle(String title) {
        try {
            LOGGER.log(Level.INFO, "🔍 Suche nach Film: " + title);
            System.out.println("🔍 Suche nach Film: " + title);

            String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
            
            // Use current language setting from LanguageUtil
            String currentLang = LanguageUtil.getTmdbLanguageCode();

            // Versuche zuerst mit der aktuellen Sprache
            String url = BASE_URL + "/search/movie?api_key=" + apiKey + "&query=" + encoded + "&language=" + currentLang;
            LOGGER.log(Level.INFO, "Suche URL (" + currentLang + "): " + url);
            System.out.println("Suche URL (" + currentLang + "): " + url);

            String json = HttpUtil.get(url);
            JsonObject response = JsonParser.parseString(json).getAsJsonObject();
            JsonArray results = response.getAsJsonArray("results");

            if (results != null && results.size() > 0) {
                String id = results.get(0).getAsJsonObject().get("id").getAsString();
                LOGGER.log(Level.INFO, "✅ ID gefunden (" + currentLang + "): " + id);
                System.out.println("✅ ID gefunden (" + currentLang + "): " + id);
                return id;
            }

            LOGGER.log(Level.INFO, "⚠️ Keine Ergebnisse in der aktuellen Sprache, versuche Englisch...");
            System.out.println("⚠️ Keine Ergebnisse in der aktuellen Sprache, versuche Englisch...");

            // Fallback auf Englisch wenn nicht bereits Englisch
            if (!"en-US".equals(currentLang)) {
                url = BASE_URL + "/search/movie?api_key=" + apiKey + "&query=" + encoded + "&language=en-US";
                LOGGER.log(Level.INFO, "Suche URL (EN): " + url);
                System.out.println("Suche URL (EN): " + url);

                json = HttpUtil.get(url);
                response = JsonParser.parseString(json).getAsJsonObject();
                results = response.getAsJsonArray("results");

                if (results != null && results.size() > 0) {
                    String id = results.get(0).getAsJsonObject().get("id").getAsString();
                    LOGGER.log(Level.INFO, "✅ ID gefunden (EN): " + id);
                    System.out.println("✅ ID gefunden (EN): " + id);
                    return id;
                }
            }

            LOGGER.log(Level.WARNING, "❌ Keine Ergebnisse gefunden für: " + title);
            System.out.println("❌ Keine Ergebnisse gefunden für: " + title);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ Fehler bei TMDb Suche: " + e.getMessage(), e);
            System.err.println("❌ Fehler bei TMDb Suche: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Klasse zur Darstellung eines Streaming-Anbieters mit Logo
     */
    public static class StreamingProvider {

        public String name;
        public String logoUrl;

        public StreamingProvider(String name, String logoUrl) {
            this.name = name;
            this.logoUrl = logoUrl;
        }
    }

    /**
     * Ruft Streaming-Anbieter für einen Film ab (Deutschland)
     * Gibt nur die ersten 3 Anbieter mit Logo-URLs zurück
     */
    public List<StreamingProvider> getStreamingProviders(String tmdbId) {
        List<StreamingProvider> providers = new ArrayList<>();

        try {
            String url = BASE_URL + "/movie/" + tmdbId + "/watch/providers?api_key=" + apiKey;
            String json = HttpUtil.get(url);
            JsonObject response = JsonParser.parseString(json).getAsJsonObject();
            JsonObject results = response.getAsJsonObject("results");

            if (results != null && results.has("DE")) {
                JsonObject deData = results.getAsJsonObject("DE");

                // Prüfe auf verschiedene Streaming-Typen
                if (deData.has("flatrate") && providers.size() < 3) {
                    JsonArray flatrate = deData.getAsJsonArray("flatrate");
                    for (int i = 0; i < flatrate.size() && providers.size() < 3; i++) {
                        JsonObject provider = flatrate.get(i).getAsJsonObject();
                        String name = provider.get("provider_name").getAsString();
                        String logo = provider.get("logo_path").getAsString();
                        String logoUrl = "https://image.tmdb.org/t/p/original" + logo;
                        providers.add(new StreamingProvider(name, logoUrl));
                    }
                }

                // Prüfe auf Kauf/Miete falls noch Platz
                if (deData.has("buy") && providers.size() < 3) {
                    JsonArray buy = deData.getAsJsonArray("buy");
                    for (int i = 0; i < buy.size() && providers.size() < 3; i++) {
                        JsonObject provider = buy.get(i).getAsJsonObject();
                        String name = provider.get("provider_name").getAsString() + " (Kauf)";
                        String logo = provider.get("logo_path").getAsString();
                        String logoUrl = "https://image.tmdb.org/t/p/original" + logo;
                        providers.add(new StreamingProvider(name, logoUrl));
                    }
                }

                if (deData.has("rent") && providers.size() < 3) {
                    JsonArray rent = deData.getAsJsonArray("rent");
                    for (int i = 0; i < rent.size() && providers.size() < 3; i++) {
                        JsonObject provider = rent.get(i).getAsJsonObject();
                        String name = provider.get("provider_name").getAsString() + " (Miete)";
                        String logo = provider.get("logo_path").getAsString();
                        String logoUrl = "https://image.tmdb.org/t/p/original" + logo;
                        providers.add(new StreamingProvider(name, logoUrl));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fehler beim Laden von Streaming-Anbietern: " + e.getMessage(), e);
        }

        return providers;
    }

    /**
     * Kombiniert Filmsuche und Streaming-Anbieter (max. 3 mit Logos)
     */
    public List<StreamingProvider> getStreamingProvidersForMovie(String movieTitle) {
        String tmdbId = getMovieIdByTitle(movieTitle);
        if (tmdbId != null) {
            return getStreamingProviders(tmdbId);
        }
        return new ArrayList<>();
    }

    /**
     * Ruft ähnliche Filme für einen Film ab (basierend auf TMDb ID)
     * Gibt bis zu 20 ähnliche Filme zurück
     */
    public List<Filmmodel> getSimilarMovies(String tmdbId) {
        List<Filmmodel> similarMovies = new ArrayList<>();

        try {
            String url = BASE_URL + "/movie/" + tmdbId + "/similar?api_key=" + apiKey + "&language=" + LanguageUtil.getTmdbLanguageCode() + "&page=1";
            String json = HttpUtil.get(url);
            JsonObject response = JsonParser.parseString(json).getAsJsonObject();
            JsonArray results = response.getAsJsonArray("results");

            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    JsonObject movieJson = results.get(i).getAsJsonObject();
                    Filmmodel film = convertTmdbToFilmmodel(movieJson);
                    if (film != null && film.getTitle() != null && !film.getTitle().isEmpty()) {
                        similarMovies.add(film);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fehler beim Laden ähnlicher Filme: " + e.getMessage(), e);
        }

        return similarMovies;
    }

    /**
     * Kombiniert Filmsuche und ähnliche Filme
     */
    public List<Filmmodel> getSimilarMoviesForMovie(String movieTitle) {
        String tmdbId = getMovieIdByTitle(movieTitle);
        if (tmdbId != null && !tmdbId.isEmpty()) {
            return getSimilarMovies(tmdbId);
        }
        return new ArrayList<>();
    }

    /**
     * Holt die Poster-URL von TMDB für einen Film
     */
    public String getPosterUrlForMovie(String movieTitle) {
        try {
            String tmdbId = getMovieIdByTitle(movieTitle);
            if (tmdbId == null) return null;

            String url = BASE_URL + "/movie/" + tmdbId + "?api_key=" + apiKey + "&language=" + LanguageUtil.getTmdbLanguageCode();
            String json = HttpUtil.get(url);
            JsonObject movie = JsonParser.parseString(json).getAsJsonObject();

            if (movie.has("poster_path") && !movie.get("poster_path").isJsonNull()) {
                String posterPath = movie.get("poster_path").getAsString();
                if (posterPath != null && !posterPath.isEmpty()) {
                    return "https://image.tmdb.org/t/p/w500" + posterPath;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fehler beim Laden des TMDB-Posters: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Holt den Teaser/Tagline von TMDB für einen Film
     */
    public String getTeaserForMovie(String movieTitle) {
        try {
            String tmdbId = getMovieIdByTitle(movieTitle);
            if (tmdbId == null) {
                LOGGER.log(Level.INFO, "Keine TMDB ID gefunden für: " + movieTitle);
                return null;
            }

            String url = BASE_URL + "/movie/" + tmdbId + "?api_key=" + apiKey + "&language=" + LanguageUtil.getTmdbLanguageCode();
            String json = HttpUtil.get(url);
            JsonObject movie = JsonParser.parseString(json).getAsJsonObject();

            if (movie.has("tagline") && !movie.get("tagline").isJsonNull()) {
                String tagline = movie.get("tagline").getAsString();
                if (tagline != null && !tagline.isEmpty() && !tagline.isBlank()) {
                    LOGGER.log(Level.INFO, "Teaser geladen für: " + movieTitle + " -> " + tagline);
                    return tagline;
                } else {
                    LOGGER.log(Level.INFO, "Tagline ist leer für: " + movieTitle);
                }
            } else {
                LOGGER.log(Level.INFO, "Keine tagline in TMDB Response für: " + movieTitle);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fehler beim Laden des TMDB-Teasers für " + movieTitle + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Holt einen Trailer (YouTube) von TMDb und liefert eine Embed-URL
     */
    public String getTrailerUrlForMovie(String movieTitle) {
        try {
            String tmdbId = getMovieIdByTitle(movieTitle);
            if (tmdbId == null) return null;

            String url = BASE_URL + "/movie/" + tmdbId + "/videos?api_key=" + apiKey + "&language=" + LanguageUtil.getTmdbLanguageCode();
            String json = HttpUtil.get(url);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray results = root.has("results") && root.get("results").isJsonArray() ? root.getAsJsonArray("results") : null;
            if (results == null || results.size() == 0) return null;

            String fallback = null;
            for (int i = 0; i < results.size(); i++) {
                JsonObject vid = results.get(i).getAsJsonObject();
                if (!vid.has("site") || !"YouTube".equalsIgnoreCase(vid.get("site").getAsString())) continue;
                if (!vid.has("key")) continue;
                String key = vid.get("key").getAsString();
                String type = vid.has("type") ? vid.get("type").getAsString() : "";
                boolean official = vid.has("official") && vid.get("official").getAsBoolean();
                String embed = "https://www.youtube.com/embed/" + key + "?rel=0&autoplay=0";
                if ("Trailer".equalsIgnoreCase(type) && official) return embed;
                if (fallback == null && "Trailer".equalsIgnoreCase(type)) fallback = embed;
            }
            return fallback;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fehler beim Laden des TMDb-Trailers: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Konvertiert ein TMDB JSON-Objekt zu einem Filmmodel
     */
    private Filmmodel convertTmdbToFilmmodel(JsonObject tmdbMovie) {
        try {
            Filmmodel film = new Filmmodel();

            // Titel
            if (tmdbMovie.has("title") && !tmdbMovie.get("title").isJsonNull()) {
                film.setTitle(tmdbMovie.get("title").getAsString());
            }

            // Veröffentlichungsjahr
            if (tmdbMovie.has("release_date") && !tmdbMovie.get("release_date").isJsonNull()) {
                String releaseDate = tmdbMovie.get("release_date").getAsString();
                if (releaseDate != null && !releaseDate.isEmpty() && releaseDate.length() >= 4) {
                    film.setYear(releaseDate.substring(0, 4));
                }
            }

            // Handlung/Plot
            if (tmdbMovie.has("overview") && !tmdbMovie.get("overview").isJsonNull()) {
                film.setPlot(tmdbMovie.get("overview").getAsString());
            }

            // Bewertung
            if (tmdbMovie.has("vote_average") && !tmdbMovie.get("vote_average").isJsonNull()) {
                double rating = tmdbMovie.get("vote_average").getAsDouble();
                film.setImdbRating(String.format("%.1f", rating));
            }

            // Poster
            if (tmdbMovie.has("poster_path") && !tmdbMovie.get("poster_path").isJsonNull()) {
                String posterPath = tmdbMovie.get("poster_path").getAsString();
                if (posterPath != null && !posterPath.isEmpty()) {
                    film.setPoster("https://image.tmdb.org/t/p/w500" + posterPath);
                }
            }

            // TMDb ID als Referenz
            if (tmdbMovie.has("id") && !tmdbMovie.get("id").isJsonNull()) {
                film.setImdbID("tmdb_" + tmdbMovie.get("id").getAsInt());
            }

            return film;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fehler beim Konvertieren von TMDB Film zu Filmmodel: " + e.getMessage(), e);
            return null;
        }
    }
}
