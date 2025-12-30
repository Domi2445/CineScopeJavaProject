package com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;




public abstract class Dateihandler
{
    protected static final String WATCHLIST_PATH = "watchlist.json";
    protected BufferedWriter writer;
    protected BufferedReader reader;
    protected final Gson gson = new Gson();
    protected final Type LIST_TYPE = new TypeToken<List<String>>() {}.getType();

    public Dateihandler()
    {

    }

    public List<String> leseWatchlist() {
        try (BufferedReader br = new BufferedReader(new FileReader(WATCHLIST_PATH))) {
            List<String> list = gson.fromJson(br, LIST_TYPE);
            return list != null ? list : new ArrayList<>();
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
