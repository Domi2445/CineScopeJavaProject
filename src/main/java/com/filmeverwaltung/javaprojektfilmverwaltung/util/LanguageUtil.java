package com.filmeverwaltung.javaprojektfilmverwaltung.util;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Language;

/**
 * Kürzere Utility-Klasse für sprachabhängige UI-Texte.
 */
public final class LanguageUtil {
    private LanguageUtil() { }

    public static String getTopMoviesTitle(Language lang) {
        if (lang == null) lang = Language.DE;
        switch (lang) {
            case EN: return "Top Movies";
            case AR: return "الأفلام الأكثر شعبية";
            default:  return "Beliebteste Filme";
        }
    }

    public static String getTopMoviesSubtitle(Language lang) {
        if (lang == null) lang = Language.DE;
        switch (lang) {
            case EN: return "Here are the 10 most viewed movies";
            case AR: return "إليك أفضل 10 أفلام حسب عدد المشاهدات";
            default:  return "Hier sind die 10 am häufigsten angesehenen Filme";
        }
    }

    public static String getNoMoviesFound(Language lang) {
        if (lang == null) lang = Language.DE;
        switch (lang) {
            case EN: return "No movies viewed yet";
            case AR: return "لم يتم مشاهدة أي أفلام بعد";
            default:  return "Noch keine Filme angesehen";
        }
    }

    public static String getViewCountLabel(Language lang) {
        if (lang == null) lang = Language.DE;
        switch (lang) {
            case EN: return "Views: ";
            case AR: return "المشاهدات: ";
            default:  return "Aufrufe: ";
        }
    }

    public static String formatViewCount(Language lang, int count) {
        return getViewCountLabel(lang) + count;
    }
}
