package com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.DateihandlerIO.WATCHLIST_PATH;


public abstract class Dateihandler
{
    private static final String WATCHLIST_PATH = ;
    protected BufferedWriter writer;
    protected BufferedReader reader;

    public Dateihandler()
    {

    }

    public List<String> leseWatchlist() {
        try (BufferedReader br = new BufferedReader(new FileReader(WATCHLIST_PATH))) {
            return gson.fromJson(br, LIST_TYPE);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void speichereWatchlist(List<String> ids) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(WATCHLIST_PATH))) {
            gson.toJson(ids, bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
