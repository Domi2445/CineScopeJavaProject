package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TranslationUtil {

    private static final Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(TranslationUtil.class.getName());
    private static final String TRANSLATE_URL = "https://libretranslate.de/translate";

    public String translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.isBlank()) {
            return text;
        }

        Map<String, String> payload = Map.of(
                "q", text,
                "source", sourceLang != null ? sourceLang : "",
                "target", targetLang != null ? targetLang : "",
                "format", "text"
        );
        String jsonBody = gson.toJson(payload);

        String responseBody;
        try {
            responseBody = HttpUtil.post(TRANSLATE_URL, jsonBody, "application/json");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fehler beim HTTP-POST zur Übersetzungs-API: {0}", e.getMessage());
            return text;
        }

        if (responseBody == null || responseBody.isBlank()) {
            LOGGER.warning("Leere API-Antwort");
            return text;
        }

        // Kurzes Logging (bei langen Antworten nur die Länge und einen Ausschnitt)
        LOGGER.fine(() -> "Übersetzungs-API Antwortlänge: " + responseBody.length()
                + " Vorschau: " + (responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody));

        Map<?, ?> json;
        try {
            json = gson.fromJson(responseBody, Map.class);
        } catch (JsonSyntaxException e) {
            LOGGER.log(Level.WARNING, "Ungültiges JSON von Übersetzungs-API: {0}", e.getMessage());
            LOGGER.fine(() -> "Rohantwort: " + responseBody);
            return text;
        }

        if (json == null || !json.containsKey("translatedText")) {
            LOGGER.warning("Kein 'translatedText' in Antwort: " + (responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody));
            return text;
        }

        Object translated = json.get("translatedText");
        return translated != null ? translated.toString() : text;
    }
}
