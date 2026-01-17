package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Language;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class LanguageUtil {

    private static Language currentLanguage = Language.DE;
    private static ResourceBundle bundle;

    public static void setLanguage(Language lang) {
        currentLanguage = lang;
        bundle = ResourceBundle.getBundle(
                "i18n.messages",
                getLocale()
        );
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