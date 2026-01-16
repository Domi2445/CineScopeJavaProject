// src/main/java/com/filmeverwaltung/javaprojektfilmverwaltung/model/Filmmodel.java
package com.filmeverwaltung.javaprojektfilmverwaltung.model;

import com.google.gson.annotations.SerializedName;

public class Filmmodel {
    @SerializedName("Title")
    private String title;

    @SerializedName("Year")
    private String year;

    @SerializedName("Writer")
    private String writer;

    @SerializedName("Plot")
    private String plot;

    // NEW: Teaser/Tagline (kurze Beschreibung)
    private String teaser;

    @SerializedName("Genre")
    private String genre;

    // NEW: Sprache (Language) - OMDb returns comma-separated languages
    @SerializedName("Language")
    private String language;

    // NEW: Laufzeit (Runtime) - OMDb returns e.g., "123 min"
    @SerializedName("Runtime")
    private String runtime;

    @SerializedName("Response")
    private String response;

    @SerializedName("imdbID")
    private String imdbID;

    @SerializedName("Poster")
    private String poster;

    @SerializedName("imdbRating")
    private String imdbRating;

    // NEW: User ID (Foreign Key zur users-Tabelle)
    private Long userId;

    public Filmmodel() { }

    public Filmmodel(String title, String year, String writer, String plot) {
        this.title = title;
        this.year = year;
        this.writer = writer;
        this.plot = plot;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getWriter() { return writer; }
    public void setWriter(String writer) { this.writer = writer; }

    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }

    public String getTeaser() { return teaser; }
    public void setTeaser(String teaser) { this.teaser = teaser; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    // NEW: Sprache
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    // NEW: Laufzeit (raw string like "123 min")
    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getImdbID() { return imdbID; }
    public void setImdbID(String imdbID) { this.imdbID = imdbID; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getRuntimeInMinutes() {
        if (runtime == null || runtime.isBlank() || "N/A".equalsIgnoreCase(runtime)) {
            return -1;
        }
        try {
            String num = runtime.toLowerCase().replace("min", "").trim();
            return Integer.parseInt(num);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
    @Override
    public String toString() { return title + " (" + year + ")\n" + plot; }
}
