package com.filmeverwaltung.javaprojektfilmverwaltung.db;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.HttpUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository für die Films-Tabelle.
 * Speichert Filme und verwaltet Zugriffszahlen für Beliebtheitsranking.
 */
public class FilmRepository
{
    private static final Logger LOGGER = Logger.getLogger(FilmRepository.class.getName());

    /**
     * Speichert einen Film oder aktualisiert den View Count wenn bereits vorhanden
     */
    public void addOrUpdateFilm(Filmmodel film) throws SQLException
    {
        if (film.getUserId() == null) {
            LOGGER.log(Level.WARNING, "Versuch Film zu speichern ohne USER_ID: " + film.getTitle());
            return;
        }

        String sqlCheckExists = "SELECT VIEW_COUNT FROM films WHERE IMDB_ID = ? AND USER_ID = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement psCheck = c.prepareStatement(sqlCheckExists))
        {
            psCheck.setString(1, film.getImdbID());
            psCheck.setLong(2, film.getUserId());
            try (ResultSet rs = psCheck.executeQuery())
            {
                if (rs.next())
                {
                    // Film existiert bereits - View Count erhöhen
                    int currentCount = rs.getInt("VIEW_COUNT");
                    updateViewCount(film.getImdbID(), film.getUserId(), currentCount + 1);
                    LOGGER.log(Level.INFO, "Film View Count erhöht: " + film.getTitle());
                }
                else
                {
                    // Neuer Film - einfügen
                    insertFilm(film);
                    LOGGER.log(Level.INFO, "Neuer Film gespeichert: " + film.getTitle());
                }
            }
        }
    }

    /**
     * Fügt einen neuen Film ein
     */
    private void insertFilm(Filmmodel film) throws SQLException
    {
        String sql = "INSERT INTO films (USER_ID, IMDB_ID, TITLE, YEAR, WRITER, PLOT, TEASER, IMDB_RATING, VIEW_COUNT, LAST_VIEWED) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setLong(1, film.getUserId());
            ps.setString(2, film.getImdbID());
            ps.setString(3, film.getTitle());
            ps.setString(4, film.getYear());
            ps.setString(5, film.getWriter());
            ps.setString(6, film.getPlot());
            ps.setString(7, film.getTeaser());
            ps.setString(8, film.getImdbRating());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    /**
     * Aktualisiert den View Count eines Films
     */
    private void updateViewCount(String imdbId, Long userId, int newCount) throws SQLException
    {
        String sql = "UPDATE films SET VIEW_COUNT = ?, LAST_VIEWED = ? WHERE IMDB_ID = ? AND USER_ID = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setInt(1, newCount);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, imdbId);
            ps.setLong(4, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Ruft die beliebtesten Filme ab (von TMDB, Seite 1) und konvertiert sie in Filmmodel
     */
    public List<Filmmodel> getTopMovies(int limit) {
        List<Filmmodel> movies = new ArrayList<>();

        // URL dynamisch mit API-Key und Sprache
        String url = "https://api.themoviedb.org/3/movie/popular"
                + "?api_key=" + ApiConfig.TMDB_API_KEY
                + "&language=" + ApiConfig.TMDB_LANGUAGE
                + "&page=1";

        try {
            // GET-Request über HttpUtil (kann IOException/InterruptedException werfen)
            String json = HttpUtil.get(url);

            JsonElement rootEl = JsonParser.parseString(json);
            if (rootEl != null && rootEl.isJsonObject()) {
                JsonObject root = rootEl.getAsJsonObject();
                JsonArray results = root.has("results") && root.get("results").isJsonArray()
                        ? root.getAsJsonArray("results") : null;

                if (results != null) {
                    for (int i = 0; i < results.size() && movies.size() < limit; i++) {
                        JsonObject m = results.get(i).getAsJsonObject();

                        Filmmodel film = new Filmmodel();

                        // TMDB liefert eine TMDB-ID (numerisch) - wir speichern sie als String
                        if (m.has("id") && !m.get("id").isJsonNull()) {
                            film.setImdbID(m.get("id").getAsString());
                        }
                        film.setTitle(m.has("title") && !m.get("title").isJsonNull() ? m.get("title").getAsString() : null);
                        film.setYear(m.has("release_date") && !m.get("release_date").isJsonNull() ? m.get("release_date").getAsString() : null);
                        film.setPlot(m.has("overview") && !m.get("overview").isJsonNull() ? m.get("overview").getAsString() : null);
                        if (m.has("vote_average") && !m.get("vote_average").isJsonNull()) {
                            try {
                                film.setImdbRating(String.valueOf(m.get("vote_average").getAsDouble()));
                            } catch (Exception ex) {
                                film.setImdbRating(m.get("vote_average").getAsString());
                            }
                        }

                        movies.add(film);
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING, "HTTP-Fehler beim Abruf der beliebtesten Filme von TMDB", e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fehler beim Verarbeiten der TMDB-Antwort", e);
        }

        return movies;
    }


    /**
     * Prüft ob ein Film bereits existiert
     */
    public boolean exists(String imdbId) throws SQLException
    {
        String sql = "SELECT 1 FROM films WHERE IMDB_ID = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, imdbId);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next();
            }
        }
    }
}
