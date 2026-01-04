package com.filmeverwaltung.javaprojektfilmverwaltung.model;

import com.google.gson.annotations.SerializedName;

/**
 * Datenmodell für einen Film, basierend auf den Feldern der OMDb-API.
 * Wird für die JSON-Deserialisierung mit Gson verwendet.
 */
public class Filmmodel
{
    /** Filmmodel-Eigenschaften, die den OMDb-API-Antwortfeldern entsprechen. */
    /**
     * Titel des Films.
     */
    @SerializedName("Title")
    private String title;
    /**
     * Erscheinungsjahr des Films.
     */
    @SerializedName("Year")
    private String year;
    /**
     * Autor des Films.
     */
    @SerializedName("Writer")
    private String writer;
    /**
     * Handlung des Films.
     */
    @SerializedName("Plot")
    private String plot;
    /**
     * Antwortstatus des Films.
     */
    @SerializedName("Response")
    private String response; // wichtig für Fehlerprüfung
    /**
     * IMDb-ID des Films.
     */
    @SerializedName("imdbID")
    private String imdbID; // OMDb imdbID
    /**
     * Poster-URL des Films.
     */
    @SerializedName("Poster")
    private String poster; // OMDb Poster-URL

    /**
     * Standardkonstruktor für Filmmodel.
     */
    public Filmmodel()
    {
        // Für Gson
    }

    /**
     * Konstruktor für Filmmodel.
     *
     * @param title  Der Titel des Films.
     * @param year   Das Erscheinungsjahr des Films.
     * @param writer Der Autor des Films.
     * @param plot   Die Beschreibung des Films.
     */
    public Filmmodel(String title, String year, String writer, String plot)
    {
        this.title = title;
        this.year = year;
        this.writer = writer;
        this.plot = plot;
    }

    // Getter & Setter

    /**
     * Getter für den Titel des Films.
     *
     * @return Der Titel des Films.
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Setter für den Titel des Films.
     *
     * @param title Der Titel des Films.
     */

    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * Getter für das Erscheinungsjahr des Films.
     *
     * @return Das Erscheinungsjahr des Films.
     */

    public String getYear()
    {
        return year;
    }

    /**
     * Setter für das Erscheinungsjahr des Films.
     *
     * @param year Das Erscheinungsjahr des Films.
     */
    public void setYear(String year)
    {
        this.year = year;
    }

    /**
     * Getter für den Autor des Films.
     *
     * @return Der Autor des Films.
     */
    public String getWriter()
    {
        return writer;
    }

    /**
     * Setter für den Autor des Films.
     *
     * @param writer Der Autor des Films.
     */

    public void setWriter(String writer)
    {
        this.writer = writer;
    }

    /**
     * Getter für die Handlung des Films.
     *
     * @return Die Handlung des Films.
     */
    public String getPlot()
    {
        return plot;
    }

    public void setPlot(String plot)
    {
        this.plot = plot;
    }

    /**
     * Getter für die Antwort des Films.
     *
     * @return Die Antwort des Films.
     */

    public String getResponse()
    {
        return response;
    }

    /**
     * Setter für die Antwort des Films.
     *
     * @param response Die Antwort des Films.
     */
    public void setResponse(String response)
    {
        this.response = response;
    }

    /**
     * Getter für die IMDb-ID des Films.
     *
     * @return Die IMDb-ID des Films.
     */
    public String getImdbID()
    {
        return imdbID;
    }

    /**
     * Setter für die IMDb-ID des Films.
     *
     * @param imdbID Die IMDb-ID des Films.
     */

    public void setImdbID(String imdbID)
    {
        this.imdbID = imdbID;
    }

    /**
     * Getter für die Poster-URL des Films.
     *
     * @return Die Poster-URL des Films.
     */
    public String getPoster()
    {
        return poster;
    }

    /**
     * Setter für die Poster-URL des Films.
     *
     * @param poster Die Poster-URL des Films.
     */
    public void setPoster(String poster)
    {
        this.poster = poster;
    }

    /**
     * Überschreibt die toString-Methode, um eine lesbare Darstellung des Films zu liefern.
     *
     * @return Eine lesbare Darstellung des Films.
     */
    @Override
    public String toString()
    {
        return title + " (" + year + ")\n" + plot;
    }
}
