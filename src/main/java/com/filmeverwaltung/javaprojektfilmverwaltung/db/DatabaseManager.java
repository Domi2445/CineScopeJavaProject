package com.filmeverwaltung.javaprojektfilmverwaltung.db;

import com.filmeverwaltung.javaprojektfilmverwaltung.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Verwaltet eine einzelne, wiederverwendbare JDBC-Datenbankverbindung.
 * Verwendet Singleton-Pattern für effiziente Ressourcennutzung.
 */
public class DatabaseManager
{
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final Config config = Config.load();
    private static Connection sharedConnection = null;
    private static final Object connectionLock = new Object();

    /**
     * Gibt eine wiederverwendbare Datenbankverbindung zurück.
     * Die Verbindung wird beim ersten Aufruf erstellt und dann wiederverwendet.
     */
    public static Connection getConnection() throws SQLException
    {
        if (config == null || config.database == null || config.database.url == null || config.database.url.isBlank())
        {
            throw new SQLException("Datenbank-Konfiguration fehlt oder ist unvollständig");
        }

        // Double-checked locking für thread-safe Singleton
        if (sharedConnection == null)
        {
            synchronized (connectionLock)
            {
                if (sharedConnection == null)
                {
                    initializeConnection();
                }
            }
        }

        // Prüfe ob Connection noch aktiv ist
        try
        {
            if (sharedConnection.isClosed())
            {
                LOGGER.log(Level.WARNING, "Datenbankverbindung wurde geschlossen, stelle wieder her");
                synchronized (connectionLock)
                {
                    initializeConnection();
                }
            }
        }
        catch (SQLException e)
        {
            LOGGER.log(Level.WARNING, "Fehler beim Prüfen der Verbindung", e);
            synchronized (connectionLock)
            {
                initializeConnection();
            }
        }

        return sharedConnection;
    }

    /**
     * Erstellt eine neue Datenbankverbindung
     */
    private static void initializeConnection() throws SQLException
    {
        String url = config.database.url;
        String user = config.database.user;
        String password = config.database.password;

        try
        {
            sharedConnection = DriverManager.getConnection(url, user, password);
            LOGGER.log(Level.INFO, "Datenbankverbindung hergestellt und wird wiederverwendet");
        }
        catch (SQLException e)
        {
            LOGGER.log(Level.SEVERE, "Fehler beim Verbinden mit Oracle-Datenbank", e);
            throw e;
        }
    }

    /**
     * Schließt die Datenbankverbindung (bei Anwendungsende aufrufen)
     */
    public static void closeConnection()
    {
        synchronized (connectionLock)
        {
            if (sharedConnection != null)
            {
                try
                {
                    sharedConnection.close();
                    LOGGER.log(Level.INFO, "Datenbankverbindung geschlossen");
                }
                catch (SQLException e)
                {
                    LOGGER.log(Level.WARNING, "Fehler beim Schließen der Datenbankverbindung", e);
                }
                finally
                {
                    sharedConnection = null;
                }
            }
        }
    }
}
