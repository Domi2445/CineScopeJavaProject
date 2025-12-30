package com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DateihandlerIO extends Dateihandler {

    private static final Gson gson = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    public DateihandlerIO() {
        try {
            File file = new File(WATCHLIST_PATH);
            if (!file.exists()) {
                file.createNewFile();
                try (FileWriter fw = new FileWriter(WATCHLIST_PATH)) {
                    fw.write("[]");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void fuegeFilmHinzu(String imdbID) {
        List<String> liste = leseWatchlist();
        if (!liste.contains(imdbID)) {
            liste.add(imdbID);
            speichereWatchlist(liste);
        }
    }

    public void entferneFilm(String imdbID) {
        List<String> liste = leseWatchlist();
        if (liste.remove(imdbID)) {
            speichereWatchlist(liste);
        }
    }

}