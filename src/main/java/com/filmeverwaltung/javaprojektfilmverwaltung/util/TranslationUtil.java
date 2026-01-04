package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

        // Parse die JSON-Antwort robust (MyMemory kann gelegentlich Felder in unterschiedlichem Typ zurückgeben)
        String translatedText = null;
        double matchValue = 0d;
        try
        {
            JsonElement rootEl = JsonParser.parseString(responseBody);
            if (!rootEl.isJsonObject())
            {
                handleApiError("Unerwartetes JSON-Format (root nicht Objekt)");
                return text;
            }

            JsonObject root = rootEl.getAsJsonObject();

            // responseStatus: kann numerisch oder String sein
            boolean statusOk = true;
            if (root.has("responseStatus") && !root.get("responseStatus").isJsonNull())
            {
                JsonElement statusEl = root.get("responseStatus");
                try
                {
                    int statusInt = statusEl.getAsInt();
                    statusOk = (statusInt == 200);
                } catch (Exception ex)
                {
                    statusOk = "200".equals(statusEl.getAsString());
                }
            }

            if (!statusOk)
            {
                handleApiError("API-Status ungleich 200");
                return text;
            }

            if (root.has("responseData") && root.get("responseData").isJsonObject())
            {
                JsonObject rd = root.getAsJsonObject("responseData");
                if (rd.has("translatedText") && !rd.get("translatedText").isJsonNull())
                {
                    translatedText = rd.get("translatedText").getAsString();
                }
                if (rd.has("match") && !rd.get("match").isJsonNull())
                {
                    try
                    {
                        matchValue = rd.get("match").getAsDouble();
                    } catch (Exception ex)
                    {
                        try
                        {
                            matchValue = Double.parseDouble(rd.get("match").getAsString());
                        } catch (Exception ignore)
                        {
                        }
                    }
                }
            }

            // `matches` kann Array oder String sein; wir loggen den Fall
            if (root.has("matches") && !root.get("matches").isJsonNull())
            {
                JsonElement matchesEl = root.get("matches");
                if (matchesEl.isJsonArray())
                {
                    LOGGER.fine(() -> "MyMemory: matches array size=" + matchesEl.getAsJsonArray().size());
                } else
                {
                    LOGGER.fine(() -> "MyMemory: matches ist kein Array (type=" + matchesEl.getClass().getSimpleName() + ")");
                }
            }

            // Ende des try-Blocks
        }
        catch (JsonSyntaxException e)
        {
            handleApiError("Ungültiges JSON: " + e.getMessage());
            return text;
        }
        catch (Exception e)
        {
            handleApiError("Fehler beim Parsen der API-Antwort: " + e.getMessage());
            return text;
        }

        if (translatedText == null || translatedText.isBlank())
        {
            handleApiError("Keine Übersetzung erhalten");
            return text;
        }

        apiAvailable = true;
        final double match = matchValue;
        LOGGER.fine(() -> "Übersetzung erfolgreich: Match=" + match);
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
