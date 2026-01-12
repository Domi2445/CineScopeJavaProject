package com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WatchlistHandler extends Dateihandler
{

    private static final String WATCHLIST_PATH = "watchlist.json";

    public WatchlistHandler()
    {

        super();
        try
        {
            File file = new File(WATCHLIST_PATH);
            if (!file.exists())
            {
                file.createNewFile();
                try (FileWriter fw = new FileWriter(WATCHLIST_PATH))
                {
                    fw.write("[]");
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public List<String> lesen()
    {

        return lesen(WATCHLIST_PATH);
    }

    public void speichern(List<String> ids)
    {

        speichern(ids, WATCHLIST_PATH);
    }

    public void fuegeFilmHinzu(String imdbID)
    {

        List<String> liste = lesen();
        if (!liste.contains(imdbID))
        {
            liste.add(imdbID);
            speichern(liste);
        }
    }

    public void entferneFilm(String imdbID)
    {

        List<String> liste = lesen();
        if (liste.remove(imdbID))
        {
            speichern(liste);
        }
    }

    public boolean istInWatchlist(String imdbID)
    {

        return lesen().contains(imdbID);
    }

}