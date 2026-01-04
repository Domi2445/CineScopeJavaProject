package com.filmeverwaltung.javaprojektfilmverwaltung.model;

import java.time.LocalDateTime;

/**
 * Model-Klasse für einen Favoriten-Film mit zusätzlichen Metadaten.
 */
public class FavoriteModel
{
    private String imdbID;
    private String title;
    private String year;
    private String posterUrl;
    private int userRating;           // 1-10
    private String notes;             // Benutzernotizen
    private LocalDateTime addedAt;    // Wann hinzugefügt

    public FavoriteModel()
    {
        this.addedAt = LocalDateTime.now();
        this.userRating = 0;
    }

    public FavoriteModel(String imdbID)
    {
        this.imdbID = imdbID;
        this.addedAt = LocalDateTime.now();
        this.userRating = 0;
    }

    public FavoriteModel(String imdbID, String title, String year, String posterUrl)
    {
        this.imdbID = imdbID;
        this.title = title;
        this.year = year;
        this.posterUrl = posterUrl;
        this.addedAt = LocalDateTime.now();
        this.userRating = 0;
    }

    // Getters & Setters
    public String getImdbID()
    {
        return imdbID;
    }

    public void setImdbID(String imdbID)
    {
        this.imdbID = imdbID;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getYear()
    {
        return year;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    public String getPosterUrl()
    {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl)
    {
        this.posterUrl = posterUrl;
    }

    public int getUserRating()
    {
        return userRating;
    }

    public void setUserRating(int userRating)
    {
        if (userRating >= 0 && userRating <= 10)
        {
            this.userRating = userRating;
        }
    }

    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public LocalDateTime getAddedAt()
    {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt)
    {
        this.addedAt = addedAt;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FavoriteModel that = (FavoriteModel) obj;
        return imdbID != null && imdbID.equals(that.imdbID);
    }

    @Override
    public int hashCode()
    {
        return imdbID != null ? imdbID.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "FavoriteModel{" + "imdbID='" + imdbID + '\'' + ", title='" + title + '\'' + ", userRating=" + userRating + ", addedAt=" + addedAt + '}';
    }
}

