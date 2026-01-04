package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import javafx.scene.Parent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Cache für geladene FXML-Views um zu vermeiden, dass diese jedes Mal neu geladen werden.
 * Dies ist wichtig für Performance und um Datenbankverbindungen zu sparen.
 */
public class ViewCache
{
    private static final Logger LOGGER = Logger.getLogger(ViewCache.class.getName());
    private static final Map<String, Parent> viewCache = new HashMap<>();

    /**
     * Speichert eine View im Cache
     */
    public static void put(String path, Parent view)
    {
        viewCache.put(path, view);
        LOGGER.info("View gecacht: " + path);
    }

    /**
     * Holt eine View aus dem Cache
     */
    public static Parent get(String path)
    {
        return viewCache.get(path);
    }

    /**
     * Prüft ob eine View im Cache ist
     */
    public static boolean contains(String path)
    {
        return viewCache.containsKey(path);
    }

    /**
     * Leert den gesamten Cache
     */
    public static void clear()
    {
        viewCache.clear();
        LOGGER.info("View-Cache geleert");
    }

    /**
     * Entfernt eine spezifische View aus dem Cache
     */
    public static void remove(String path)
    {
        viewCache.remove(path);
        LOGGER.info("View aus Cache entfernt: " + path);
    }
}

