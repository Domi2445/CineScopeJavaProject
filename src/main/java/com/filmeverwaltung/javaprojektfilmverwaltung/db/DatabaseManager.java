package com.filmeverwaltung.javaprojektfilmverwaltung.db;

import com.filmeverwaltung.javaprojektfilmverwaltung.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Liefert JDBC-Connections zur in `Config` konfigurierten Oracle-Datenbank.
 */
public class DatabaseManager
{
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final Config config = Config.load();


    public static Connection getConnection() throws SQLException
    {
        if (config == null || config.database == null || config.database.url == null || config.database.url.isBlank())
        {
            throw new SQLException("Datenbank-Konfiguration fehlt oder ist unvollständig");
        }

        String url = config.database.url;
        String user = config.database.user;
        String password = config.database.password;

        try
        {
            Connection conn = DriverManager.getConnection(url, user, password);
            LOGGER.log(Level.INFO, "Erfolgreich mit Oracle-Datenbank verbunden");
            return conn;
        } catch (SQLException e)
        {
            LOGGER.log(Level.SEVERE, "Fehler beim Verbinden mit Oracle-Datenbank", e);
            throw e;
        }
    }
}
