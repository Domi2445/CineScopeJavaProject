package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Language;
import java.util.Locale;
import java.util.ResourceBundle;




public final class LanguageUtil {

    private static Language currentLanguage = Language.DE;
    private static ResourceBundle bundle = loadBundle();

    private LanguageUtil() {}

    private static ResourceBundle loadBundle() {
        return ResourceBundle.getBundle(
                "i18n.messages",
                getLocale()
        );
    }

    public static void setLanguage(Language lang) {
        if (lang != null) {
            currentLanguage = lang;
            bundle = loadBundle();
        }
    }

    public static Language getLanguage() {
        return currentLanguage;
    }

    public static Locale getLocale() {
        return switch (currentLanguage) {
            case EN -> Locale.ENGLISH;
            case AR -> Locale.forLanguageTag("ar");
            case PL -> Locale.forLanguageTag("pl");
            default -> Locale.GERMAN;
        };
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }


}
