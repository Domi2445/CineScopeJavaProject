package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class TranslationUtil {

    private static final Logger LOGGER = Logger.getLogger(TranslationUtil.class.getName());
    private static final String MYMEMORY_URL = "https://api.mymemory.translated.net/get";
    private static final String DEEPL_URL = "https://api-free.deepl.com/v2/translate";
    private static final String DEEPL_KEY = "aa9c064d-a7e0-40ef-a726-6d14fc3c20b0:fx";

    private static volatile String lastErrorMessage = null;

    public String translate(String text, String sourceLang, String targetLang) {
        if (text == null || text.isBlank()) return text;

        // 1. Versuch: MyMemory
        String result = translateWithMyMemory(text, sourceLang, targetLang);

        // 2. Failover: Wenn MyMemory das Original zurückgibt oder ein Fehler vorliegt
        if (lastErrorMessage != null || result.equals(text)) {
            LOGGER.info("MyMemory fehlgeschlagen oder Limit erreicht. Nutze DeepL Backup...");
            result = translateWithDeepL(text, targetLang);
        }

        return result;
    }

    private String translateWithMyMemory(String text, String sourceLang, String targetLang) {
        try {
            String effectiveSource = (sourceLang == null) ? "autodetect" : sourceLang;
            String langPair = effectiveSource + "|" + targetLang;
            String url = MYMEMORY_URL + "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                    + "&langpair=" + URLEncoder.encode(langPair, StandardCharsets.UTF_8);

            String response = HttpUtil.get(url);
            JsonObject root = JsonParser.parseString(response).getAsJsonObject();

            if (root.get("responseStatus").getAsInt() == 200) {
                lastErrorMessage = null;
                return root.getAsJsonObject("responseData").get("translatedText").getAsString();
            } else {
                lastErrorMessage = "MyMemory Status: " + root.get("responseStatus").getAsInt();
            }
        } catch (Exception e) {
            lastErrorMessage = "MyMemory Error: " + e.getMessage();
        }
        return text;
    }

    private String translateWithDeepL(String text, String targetLang) {
        try {
            // DeepL braucht den Key im Header oder als Parameter
            String url = DEEPL_URL + "?auth_key=" + DEEPL_KEY
                    + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                    + "&target_lang=" + targetLang.toUpperCase();

            String response = HttpUtil.get(url);
            JsonObject root = JsonParser.parseString(response).getAsJsonObject();

            if (root.has("translations")) {
                lastErrorMessage = null; // DeepL war erfolgreich
                return root.getAsJsonArray("translations").get(0).getAsJsonObject().get("text").getAsString();
            }
        } catch (Exception e) {
            LOGGER.severe("DeepL Failover ebenfalls gescheitert: " + e.getMessage());
            lastErrorMessage = "Beide Dienste nicht verfügbar.";
        }
        return text;
    }

    public static String getLastError() { return lastErrorMessage; }
    public static void clearLastError() { lastErrorMessage = null; }
}