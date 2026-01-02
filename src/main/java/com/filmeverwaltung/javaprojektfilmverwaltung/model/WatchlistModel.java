package com.filmeverwaltung.javaprojektfilmverwaltung.model;

import java.time.LocalDateTime;

/**
 * Model-Klasse für einen Watchlist-Film mit Priorität und Status.
 */
public class WatchlistModel {
    private String imdbID;
    private String title;
    private String year;
    private String posterUrl;
    private int priority;                  // 1 (hoch) - 5 (niedrig)
    private boolean watched;               // Schon angesehen?
    private LocalDateTime addedAt;         // Wann hinzugefügt
    private LocalDateTime reminderDate;    // Erinnerung setzen
    private String notes;                  // Notizen

    public WatchlistModel() {
        this.addedAt = LocalDateTime.now();
        this.priority = 3;  // Standard: mittlere Priorität
        this.watched = false;
    }

    public WatchlistModel(String imdbID) {
        this.imdbID = imdbID;
        this.addedAt = LocalDateTime.now();
        this.priority = 3;
        this.watched = false;
    }

    public WatchlistModel(String imdbID, String title, String year, String posterUrl) {
        this.imdbID = imdbID;
        this.title = title;
        this.year = year;
        this.posterUrl = posterUrl;
        this.addedAt = LocalDateTime.now();
        this.priority = 3;
        this.watched = false;
    }

    // Getters & Setters
    public String getImdbID() {
        return imdbID;
    }

    public void setImdbID(String imdbID) {
        this.imdbID = imdbID;
    }

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

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority >= 1 && priority <= 5) {
            this.priority = priority;
        }
    }

    public boolean isWatched() {
        return watched;
    }

    public void setWatched(boolean watched) {
        this.watched = watched;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public LocalDateTime getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(LocalDateTime reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPriorityLabel() {
        return switch (priority) {
            case 1 -> "Sehr hoch";
            case 2 -> "Hoch";
            case 3 -> "Mittel";
            case 4 -> "Niedrig";
            case 5 -> "Sehr niedrig";
            default -> "Unbekannt";
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WatchlistModel that = (WatchlistModel) obj;
        return imdbID != null && imdbID.equals(that.imdbID);
    }

    @Override
    public int hashCode() {
        return imdbID != null ? imdbID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "WatchlistModel{" +
                "imdbID='" + imdbID + '\'' +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", watched=" + watched +
                ", addedAt=" + addedAt +
                '}';
    }
}

