package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Language;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class LanguageUtil {

    private static Language currentLanguage = Language.DE;
    private static ResourceBundle bundle;

    static {
        // sicherstellen, dass beim Klassenladen ein Bundle initialisiert wird
        setLanguage(currentLanguage);
    }

    public static void setLanguage(Language lang) {
        currentLanguage = lang;
        try {
            bundle = ResourceBundle.getBundle("i18n.messages", getLocale());
        } catch (MissingResourceException e) {
            // Fallback auf Default (deutsch) falls spezifische Datei fehlt
            try {
                bundle = ResourceBundle.getBundle("i18n.messages", Locale.GERMAN);
            } catch (MissingResourceException ex) {
                // als letzte Absicherung ein leeres Bundle (nicht null)
                bundle = new ResourceBundle() {
                    @Override
                    protected Object handleGetObject(String key) { return null; }
                    @Override
                    public java.util.Enumeration<String> getKeys() { return java.util.Collections.emptyEnumeration(); }
                };
            }
        }
    }

    public static Language getLanguage() {
        return currentLanguage;
    }

    public static ResourceBundle getBundle() {
        if (bundle == null) {
            setLanguage(currentLanguage);
        }
        return bundle;
    }

    private static Locale getLocale() {
        return switch (currentLanguage) {
            case EN -> Locale.ENGLISH;
            case AR -> Locale.forLanguageTag("ar");
            case PL -> Locale.forLanguageTag("pl");
            default -> Locale.GERMAN;
        };
    }
}
