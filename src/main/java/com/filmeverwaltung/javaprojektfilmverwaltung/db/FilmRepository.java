package com.filmeverwaltung.javaprojektfilmverwaltung.db;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;

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
        String sqlCheckExists = "SELECT VIEW_COUNT FROM films WHERE IMDB_ID = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement psCheck = c.prepareStatement(sqlCheckExists))
        {
            psCheck.setString(1, film.getImdbID());
            try (ResultSet rs = psCheck.executeQuery())
            {
                if (rs.next())
                {
                    // Film existiert bereits - View Count erhöhen
                    int currentCount = rs.getInt("VIEW_COUNT");
                    updateViewCount(film.getImdbID(), currentCount + 1);
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
        String sql = "INSERT INTO films (IMDB_ID, TITLE, YEAR, WRITER, PLOT, IMDB_RATING, VIEW_COUNT, LAST_VIEWED) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 1, ?)";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, film.getImdbID());
            ps.setString(2, film.getTitle());
            ps.setString(3, film.getYear());
            ps.setString(4, film.getWriter());
            ps.setString(5, film.getPlot());
            ps.setString(6, film.getImdbRating());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    /**
     * Aktualisiert den View Count eines Films und das Rating
     */
    private void updateViewCount(String imdbId, int newCount) throws SQLException
    {
        String sql = "UPDATE films SET VIEW_COUNT = ?, LAST_VIEWED = ? WHERE IMDB_ID = ?";
        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setInt(1, newCount);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, imdbId);
            ps.executeUpdate();
        }
    }

    /**
     * Ruft die 10 beliebtesten Filme ab (nach View Count)
     * Nur Filme mit gültiger Bewertung werden zurückgegeben
     */
    public List<Filmmodel> getTopMovies(int limit) throws SQLException
    {
        List<Filmmodel> topMovies = new ArrayList<>();
        String sql = "SELECT IMDB_ID, TITLE, YEAR, WRITER, PLOT, IMDB_RATING, VIEW_COUNT " +
                     "FROM films " +
                     "WHERE IMDB_RATING IS NOT NULL AND IMDB_RATING != 'N/A' " +
                     "ORDER BY VIEW_COUNT DESC " +
                     "FETCH FIRST ? ROWS ONLY";

        try (Connection c = DatabaseManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    Filmmodel film = new Filmmodel();
                    film.setImdbID(rs.getString("IMDB_ID"));
                    film.setTitle(rs.getString("TITLE"));
                    film.setYear(rs.getString("YEAR"));
                    film.setWriter(rs.getString("WRITER"));
                    film.setPlot(rs.getString("PLOT"));
                    film.setImdbRating(rs.getString("IMDB_RATING"));
                    topMovies.add(film);
                }
            }
        }
        return topMovies;
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

