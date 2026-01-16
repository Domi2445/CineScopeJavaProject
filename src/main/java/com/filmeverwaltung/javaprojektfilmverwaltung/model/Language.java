package com.filmeverwaltung.javaprojektfilmverwaltung.model;

public enum Language {
    DE("Deutsch"),
    EN("Englisch"),
    AR("Arabisch"),
    PL("Polnisch");

    private final String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName; // wichtig für ComboBox
    }
}
