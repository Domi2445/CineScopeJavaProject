package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.TranslationResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * Utility-Klasse für Übersetzungen mit der MyMemory Translation API
 * <a href="https://mymemory.translated.net/doc/spec.php">API-Dokumentation</a>
 */
public class TranslationUtil
{

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
    public String translate(String text, String sourceLang, String targetLang)
    {
        // Überprüfe auf leeren Text oder gleiche Sprachen
        if (text == null || text.isBlank() || (sourceLang != null && sourceLang.equals(targetLang)))
        {
            return text;
        }

        if (!apiAvailable && (System.currentTimeMillis() - lastFailTime) < RETRY_INTERVAL)
        {
            return text;
        }


        String langPair = (sourceLang != null ? sourceLang : "en") + "|" + (targetLang != null ? targetLang : "de");
        String url = TRANSLATE_URL + "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8) + "&langpair=" + URLEncoder.encode(langPair, StandardCharsets.UTF_8);

        String responseBody;
        // Führe die HTTP-GET-Anfrage aus
        try
        {
            responseBody = HttpUtil.get(url);
            if (responseBody == null || responseBody.isBlank())
            {
                handleApiError("Leere API-Antwort");
                return text;
            }
            apiAvailable = true;
        } catch (Exception e)
        {
            handleApiError("HTTP-GET Fehler: " + e.getMessage());
            return text;
        }

        // Parse die JSON-Antwort
        TranslationResponse response;
        try
        {
            response = gson.fromJson(responseBody, TranslationResponse.class);
        } catch (JsonSyntaxException e)
        {
            handleApiError("Ungültiges JSON: " + e.getMessage());
            return text;
        }

        // Überprüfe die API-Antwort auf Fehler
        if (response == null || response.getResponseData() == null || (response.getResponseStatus() != null && !response.getResponseStatus().equals("200")))
        {
            handleApiError("Ungültige Response oder Status-Fehler");
            return text;
        }

        // Extrahiere die übersetzte Zeichenkette
        String translatedText = response.getResponseData().getTranslatedText();
        if (translatedText == null || translatedText.isBlank())
        {
            handleApiError("Keine Übersetzung erhalten");
            return text;
        }

        apiAvailable = true;
        LOGGER.fine(() -> "Übersetzung erfolgreich: Match=" + response.getResponseData().getMatch());
        return translatedText;
    }
    // Behandle API-Fehler und setze den Status
    private void handleApiError(String message)
    {
        LOGGER.warning("MyMemory API: " + message);
        apiAvailable = false;
        lastFailTime = System.currentTimeMillis();

    }
}
