package com.filmeverwaltung.javaprojektfilmverwaltung.service;

import com.filmeverwaltung.javaprojektfilmverwaltung.util.HttpUtil;
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
 * Service für TMDb API - lädt Streaming-Anbieter und weitere Informationen
 */
public class TMDbService
{

    private static final Logger LOGGER = Logger.getLogger(TMDbService.class.getName());
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private final String apiKey;

    public TMDbService(String apiKey)
    {
        this.apiKey = apiKey;
    }

    /**
     * Sucht einen Film nach Titel und gibt die TMDb ID zurück
     */
    public String getMovieIdByTitle(String title)
    {
        try
        {
            String encoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String url = BASE_URL + "/search/movie?api_key=" + apiKey + "&query=" + encoded + "&language=de";

            String json = HttpUtil.get(url);
            JsonObject response = JsonParser.parseString(json).getAsJsonObject();
            JsonArray results = response.getAsJsonArray("results");

            if (results != null && results.size() > 0)
            {
                JsonObject firstResult = results.get(0).getAsJsonObject();
                return firstResult.get("id").getAsString();
            }
        } catch (Exception e)
        {
            LOGGER.log(Level.WARNING, "Fehler bei TMDb Suche: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Klasse zur Darstellung eines Streaming-Anbieters mit Logo
     */
    public static class StreamingProvider
    {

        public String name;
        public String logoUrl;

        public StreamingProvider(String name, String logoUrl)
        {
            this.name = name;
            this.logoUrl = logoUrl;
        }
    }

    /**
     * Ruft Streaming-Anbieter für einen Film ab (Deutschland)
     * Gibt nur die ersten 3 Anbieter mit Logo-URLs zurück
     */
    public List<StreamingProvider> getStreamingProviders(String tmdbId)
    {
        List<StreamingProvider> providers = new ArrayList<>();

        try
        {
            String url = BASE_URL + "/movie/" + tmdbId + "/watch/providers?api_key=" + apiKey;
            String json = HttpUtil.get(url);
            JsonObject response = JsonParser.parseString(json).getAsJsonObject();
            JsonObject results = response.getAsJsonObject("results");

            if (results != null && results.has("DE"))
            {
                JsonObject deData = results.getAsJsonObject("DE");

                // Prüfe auf verschiedene Streaming-Typen
                if (deData.has("flatrate") && providers.size() < 3)
                {
                    JsonArray flatrate = deData.getAsJsonArray("flatrate");
                    for (int i = 0; i < flatrate.size() && providers.size() < 3; i++)
                    {
                        JsonObject provider = flatrate.get(i).getAsJsonObject();
                        String name = provider.get("provider_name").getAsString();
                        String logo = provider.get("logo_path").getAsString();
                        String logoUrl = "https://image.tmdb.org/t/p/original" + logo;
                        providers.add(new StreamingProvider(name, logoUrl));
                    }
                }

                // Prüfe auf Kauf/Miete falls noch Platz
                if (deData.has("buy") && providers.size() < 3)
                {
                    JsonArray buy = deData.getAsJsonArray("buy");
                    for (int i = 0; i < buy.size() && providers.size() < 3; i++)
                    {
                        JsonObject provider = buy.get(i).getAsJsonObject();
                        String name = provider.get("provider_name").getAsString() + " (Kauf)";
                        String logo = provider.get("logo_path").getAsString();
                        String logoUrl = "https://image.tmdb.org/t/p/original" + logo;
                        providers.add(new StreamingProvider(name, logoUrl));
                    }
                }

                if (deData.has("rent") && providers.size() < 3)
                {
                    JsonArray rent = deData.getAsJsonArray("rent");
                    for (int i = 0; i < rent.size() && providers.size() < 3; i++)
                    {
                        JsonObject provider = rent.get(i).getAsJsonObject();
                        String name = provider.get("provider_name").getAsString() + " (Miete)";
                        String logo = provider.get("logo_path").getAsString();
                        String logoUrl = "https://image.tmdb.org/t/p/original" + logo;
                        providers.add(new StreamingProvider(name, logoUrl));
                    }
                }
            }
        } catch (Exception e)
        {
            LOGGER.log(Level.WARNING, "Fehler beim Laden von Streaming-Anbietern: " + e.getMessage(), e);
        }

        return providers;
    }

    /**
     * Kombiniert Filmsuche und Streaming-Anbieter (max. 3 mit Logos)
     */
    public List<StreamingProvider> getStreamingProvidersForMovie(String movieTitle)
    {
        String tmdbId = getMovieIdByTitle(movieTitle);
        if (tmdbId != null)
        {
            return getStreamingProviders(tmdbId);
        }
        return new ArrayList<>();
    }
}

