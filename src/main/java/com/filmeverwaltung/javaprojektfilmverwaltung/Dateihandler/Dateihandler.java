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

    public List<String> lesen(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            List<String> list = gson.fromJson(br, LIST_TYPE);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void speichern(List<String> ids, String path) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            gson.toJson(ids, bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
