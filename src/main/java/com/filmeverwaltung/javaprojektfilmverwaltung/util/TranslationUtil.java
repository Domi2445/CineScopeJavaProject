package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.TranslationResponse;
import com.google.gson.Gson;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class TranslationUtil {
    private static final Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(TranslationUtil.class.getName());
    private static final String TRANSLATE_URL = "https://api.mymemory.translated.net/get";
    private static boolean apiAvailable = true;
    private static long lastFailTime = 0;
    private static final long RETRY_INTERVAL = 60000;

    public String translate(String text, String sourceLang, String targetLang) {


        if (sourceLang != null && sourceLang.equals(targetLang)) {
            return text;
        }

        if (!apiAvailable && (System.currentTimeMillis() - lastFailTime) < RETRY_INTERVAL) {
            return text;
        }

        try {
            String langPair = (sourceLang != null ? sourceLang : "en") + "|" + (targetLang != null ? targetLang : "de");
            String url = TRANSLATE_URL + "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                    + "&langpair=" + URLEncoder.encode(langPair, StandardCharsets.UTF_8);

            String responseBody = HttpUtil.get(URI.create(url).toString());

            if (responseBody == null || responseBody.isBlank()) {
                throw new Exception("Leere API-Antwort");
            }

            TranslationResponse response = gson.fromJson(responseBody, TranslationResponse.class);

            if (response == null || response.getResponseData() == null
                    || !"200".equals(response.getResponseStatus())) {
                throw new Exception("API-Fehler");
            }

            String translatedText = response.getResponseData().getTranslatedText();
            apiAvailable = true;
            return (translatedText != null && !translatedText.isBlank()) ? translatedText : text;

        } catch (Exception e) {
            apiAvailable = false;
            lastFailTime = System.currentTimeMillis();
            LOGGER.warning("Übersetzung fehlgeschlagen: " + e.getMessage());
            return text;
        }
    }
}
