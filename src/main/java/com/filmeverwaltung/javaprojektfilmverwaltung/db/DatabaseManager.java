package com.filmeverwaltung.javaprojektfilmverwaltung.db;

import com.filmeverwaltung.javaprojektfilmverwaltung.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
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
        if (config == null || config.database == null) {
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
            if (sharedConnection == null || sharedConnection.isClosed())
            {
                LOGGER.log(Level.WARNING, "Datenbankverbindung wurde geschlossen oder ist nicht vorhanden, stelle wieder her");
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
     * Erstellt eine neue Datenbankverbindung. Versucht zuerst lokale Access-DB im Ordner 'Datenbank',
     * danach die konfigurierte JDBC-URL als Fallback.
     */
    private static void initializeConnection() throws SQLException
    {
        // 1) Versuche immer zuerst lokale Access-DB in Projekt-Ordner "Datenbank" zu verwenden
        try {
            java.io.File dbDir = new java.io.File("Datenbank");
            if (dbDir.exists() && dbDir.isDirectory()) {
                java.io.File[] accdbs = dbDir.listFiles((d, n) -> {
                    String ln = n.toLowerCase();
                    return ln.endsWith(".accdb") || ln.endsWith(".mdb");
                });
                if (accdbs != null && accdbs.length > 0) {
                    java.io.File acc = accdbs[0];
                    String accPath = acc.getAbsolutePath();
                    String ucanUrl = "jdbc:ucanaccess://" + accPath;
                    try {
                        sharedConnection = DriverManager.getConnection(ucanUrl);
                        LOGGER.log(Level.INFO, "Verwende lokale Access-DB: " + accPath);
                        try {
                            ensureAccessSchema(sharedConnection);
                        } catch (Exception ex) {
                            LOGGER.log(Level.WARNING, "Fehler beim Sicherstellen des Access-Schemas", ex);
                        }
                        return; // Verbindung steht, Ende
                    } catch (SQLException ex) {
                        LOGGER.log(Level.WARNING, "Verbindung zur lokalen Access-DB fehlgeschlagen, versuche Fallback", ex);
                        // Fallthrough: versuche konfigurierten JDBC-URL
                    }
                } else {
                    LOGGER.log(Level.INFO, "Keine lokale Access-Datei im Ordner 'Datenbank' gefunden");
                }
            } else {
                LOGGER.log(Level.INFO, "Ordner 'Datenbank' nicht vorhanden oder kein Verzeichnis");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Fehler beim Prüfen des lokalen Datenbank-Ordners", ex);
        }

        // 2) Fallback: benutze die konfigurierte JDBC-URL (wie vorher)
        String url = config.database.url;
        String user = config.database.user;
        String password = config.database.password;

        if (url == null || url.isBlank()) {
            throw new SQLException("Keine lokale Access-DB gefunden und keine JDBC-URL konfiguriert");
        }

        try
        {
            if (user != null && !user.isBlank()) {
                sharedConnection = DriverManager.getConnection(url, user, password);
            } else {
                sharedConnection = DriverManager.getConnection(url);
            }
            LOGGER.log(Level.INFO, "Datenbankverbindung hergestellt und wird wiederverwendet");
        }
        catch (SQLException e)
        {
            LOGGER.log(Level.SEVERE, "Fehler beim Verbinden mit konfigurierter Datenbank", e);
            // Wenn auch Fallback fehlschlägt, werfe die Exception weiter
            throw e;
        }
    }

    /**
     * Erzeugt bei Bedarf die für das Projekt erwarteten Tabellen in einer MS Access DB.
     * Implementierung: prüft metadata auf Existenz der Tabelle 'users' und legt sie an, falls nicht vorhanden.
     */
    private static void ensureAccessSchema(Connection conn)
    {
        try
        {
            DatabaseMetaData md = conn.getMetaData();
            try (ResultSet rs = md.getTables(null, null, "users", new String[]{"TABLE"}))
            {
                if (!rs.next())
                {
                    LOGGER.log(Level.INFO, "Tabelle 'users' existiert nicht - erstelle sie für Access DB");
                    try (Statement st = conn.createStatement())
                    {
                        // Access-spezifische DDL: USER_ID als COUNTER (Autoincrement)
                        String create = "CREATE TABLE users ("
                                + "USER_ID COUNTER PRIMARY KEY, "
                                + "USERNAME TEXT(255), "
                                + "PASSWORD_HASH TEXT(255), "
                                + "EMAIL TEXT(255), "
                                + "CREATED_AT DATETIME, "
                                + "LAST_LOGIN DATETIME, "
                                + "IS_ACTIVE INTEGER)";
                        st.executeUpdate(create);
                        LOGGER.log(Level.INFO, "Tabelle 'users' erfolgreich erstellt");
                    }
                } else {
                    LOGGER.log(Level.FINE, "Tabelle 'users' bereits vorhanden");
                }
            }

            // Prüfe/erstelle Tabelle 'films'
            try (ResultSet rsf = md.getTables(null, null, "films", new String[]{"TABLE"}))
            {
                if (!rsf.next())
                {
                    LOGGER.log(Level.INFO, "Tabelle 'films' existiert nicht - erstelle sie für Access DB");
                    try (Statement st = conn.createStatement())
                    {
                        // Access-kompatible Spalten
                        String createFilms = "CREATE TABLE films ("
                                + "ID COUNTER PRIMARY KEY, "
                                + "IMDB_ID TEXT(50), "
                                + "TITLE TEXT(255), "
                                + "YEAR TEXT(10), "
                                + "WRITER MEMO, "
                                + "PLOT MEMO, "
                                + "IMDB_RATING TEXT(20), "
                                + "VIEW_COUNT INTEGER, "
                                + "LAST_VIEWED DATETIME)";
                        st.executeUpdate(createFilms);
                        LOGGER.log(Level.INFO, "Tabelle 'films' erfolgreich erstellt");
                    }
                } else {
                    LOGGER.log(Level.FINE, "Tabelle 'films' bereits vorhanden");
                }
            }
        }
        catch (SQLException e)
        {
            LOGGER.log(Level.WARNING, "Fehler beim Überprüfen/Erzeugen des Schemas", e);
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
