package com.filmeverwaltung.javaprojektfilmverwaltung;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config
{
    public Database database;
    public ApiKeys api;
    public App app;

    public static Config load()
    {
        try (FileReader reader = new FileReader("src/main/resources/config/config.json"))
        {
            Gson gson = new Gson();
            return gson.fromJson(reader, Config.class);
        } catch (IOException e)
        {
            System.err.println("Fehler beim Laden der Config: " + e.getMessage());
            // Fallback-Werte
            Config config = new Config();
            config.database = new Database();
            config.api = new ApiKeys();
            config.app = new App();
            config.app.language = "de";
            config.app.debug = false;
            config.api.omdb = "";
            config.api.tmdb = "";
            return config;
        }
    }

    public static class Database
    {
        public String url;
        public String user;
        public String password;
    }

    public static class ApiKeys
    {
        public String omdb;
        public String tmdb;
        public String tmdbLanguage;
    }

    public static class App
    {
        public String language;
        public boolean debug;
        public boolean darkMode;
    }

    public void save() {

        String path = "src/main/resources/config/config.json";


        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(this, writer);
            System.out.println("Konfiguration erfolgreich gespeichert.");
        } catch (IOException e) {
            System.err.println("Fehler beim Speichern der Config: " + e.getMessage());
        }
    }
}