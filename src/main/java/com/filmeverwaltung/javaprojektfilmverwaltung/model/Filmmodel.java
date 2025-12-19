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
    @SerializedName("Response")
    private String response; // wichtig für Fehlerprüfung
    @SerializedName("imdbID")
    private String imdbID; // OMDb imdbID
    @SerializedName("Poster")
    private String poster; // OMDb Poster-URL

    public Filmmodel() {
        // Für Gson
    }

    public Filmmodel(String title, String year, String writer, String plot) {
        this.title = title;
        this.year = year;
        this.writer = writer;
        this.plot = plot;
    }

    // Getter & Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    @Override
    public String toString() {
        return title + " (" + year + ")\n" + plot;
    }
}
