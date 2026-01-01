package com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler;

import java.io.*;
import java.util.List;

public class FavoritesHandler extends Dateihandler {

    private static final String FAVORITES_PATH = "favorites.json";

    public FavoritesHandler() {
        super();
        try {
            File file = new File(FAVORITES_PATH);
            if (!file.exists()) {
                file.createNewFile();
                try (FileWriter fw = new FileWriter(FAVORITES_PATH)) {
                    fw.write("[]");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> lesen() {
        return lesen(FAVORITES_PATH);
    }

    public void speichern(List<String> ids) {
        speichern(ids, FAVORITES_PATH);
    }

    public void fuegeFilmHinzu(String imdbID) {
        List<String> liste = lesen();
        if (!liste.contains(imdbID)) {
            liste.add(imdbID);
            speichern(liste);
        }
    }

    public void entferneFilm(String imdbID) {
        List<String> liste = lesen();
        if (liste.remove(imdbID)) {
            speichern(liste);
        }
    }

    public boolean istFavorit(String imdbID) {
        return lesen().contains(imdbID);
    }
}

