package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.TranslationResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility-Klasse für Übersetzungen mit der MyMemory Translation API
 * <a href="https://mymemory.translated.net/doc/spec.php">API-Dokumentation</a>
 */
public class TranslationUtil {

    private static final Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(TranslationUtil.class.getName());
    private static final String TRANSLATE_URL = "https://api.mymemory.translated.net/get";
    private static boolean apiAvailable = true;
    private static long lastFailTime = 0;
    private static final long RETRY_INTERVAL = 60000; // 1 Minute

    /**
     * Übersetzt einen Text von einer Sprache in eine andere
     *
     * @param text       Der zu übersetzende Text
     * @param sourceLang Quellsprache (z.B. "en", "de") - kann null sein für Auto-Erkennung
     * @param targetLang Zielsprache (z.B. "de", "en")
     * @return Der übersetzte Text oder der Originaltext bei Fehlern
     */
    public String translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.isBlank()) {
            return text;
        }

        // Wenn Sprachen gleich sind, nicht übersetzen
        if (sourceLang != null && sourceLang.equals(targetLang)) {
            return text;
        }

        // API Cooldown: Wenn API nicht antwortet, für 1 Minute nicht versuchen
        if (!apiAvailable && (System.currentTimeMillis() - lastFailTime) < RETRY_INTERVAL) {
            LOGGER.fine("Übersetzungs-API ist derzeit nicht verfügbar (Cooldown). Verwende Originaltext.");
            return text;
        }

        // MyMemory API verwendet URL-Parameter statt JSON-Body
        String langPair = (sourceLang != null ? sourceLang : "en") + "|" + (targetLang != null ? targetLang : "de");

        // Kodiere nur den Text, nicht das gesamte URL
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String encodedLangPair = URLEncoder.encode(langPair, StandardCharsets.UTF_8);

        // Baue URL mit kodierten Parametern
        String url = TRANSLATE_URL + "?q=" + encodedText + "&langpair=" + encodedLangPair;

        String responseBody;
        try {
            // Erstelle URI direkt aus dem String - HttpClient kümmert sich um die Validierung
            URI uri = URI.create(url);
            responseBody = HttpUtil.get(uri.toString());
            apiAvailable = true; // API antwortet wieder
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Ungültige URI erstellt: {0}", e.getMessage());
            apiAvailable = false;
            lastFailTime = System.currentTimeMillis();
            return text;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Fehler beim HTTP-GET zur Übersetzungs-API: {0}", e.getMessage());
            apiAvailable = false;
            lastFailTime = System.currentTimeMillis();
            return text;
        }

        if (responseBody == null || responseBody.isBlank()) {
            LOGGER.warning("Leere API-Antwort von MyMemory");
            apiAvailable = false;
            lastFailTime = System.currentTimeMillis();
            return text;
        }

        // Kurzes Logging (bei langen Antworten nur die Länge)
        LOGGER.fine(() -> "MyMemory API Antwortlänge: " + responseBody.length() + " Zeichen");

        TranslationResponse response;
        try {
            response = gson.fromJson(responseBody, TranslationResponse.class);
        } catch (JsonSyntaxException e) {
            LOGGER.log(Level.WARNING, "Ungültiges JSON von MyMemory API: {0}", e.getMessage());
            LOGGER.fine(() -> "Rohantwort: " + (responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody));
            apiAvailable = false;
            lastFailTime = System.currentTimeMillis();
            return text;
        }

        if (response == null || response.getResponseData() == null) {
            LOGGER.warning("Keine responseData in MyMemory-Antwort");
            apiAvailable = false;
            lastFailTime = System.currentTimeMillis();
            return text;
        }

        // Prüfe Response-Status
        if (response.getResponseStatus() != null && !response.getResponseStatus().equals("200")) {
            LOGGER.log(Level.WARNING, "MyMemory API Fehler: Status={0}, Details={1}",
                    new Object[]{response.getResponseStatus(), response.getResponseDetails()});
            apiAvailable = false;
            lastFailTime = System.currentTimeMillis();
            return text;
        }

        String translatedText = response.getResponseData().getTranslatedText();

        if (translatedText == null || translatedText.isBlank()) {
            LOGGER.warning("Keine Übersetzung in MyMemory-Antwort erhalten");
            return text;
        }

        apiAvailable = true; // Erfolgreiche Übersetzung
        LOGGER.fine(() -> String.format("Übersetzung erfolgreich: '%s' -> '%s' (Match: %.2f)",
                text.substring(0, Math.min(50, text.length())),
                translatedText.substring(0, Math.min(50, translatedText.length())),
                response.getResponseData().getMatch()));

        return translatedText;
    }
}
