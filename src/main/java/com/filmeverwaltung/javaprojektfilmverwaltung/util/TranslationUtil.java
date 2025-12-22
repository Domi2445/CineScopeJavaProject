package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.google.gson.Gson;
import java.util.Map;

public class TranslationUtil
{


        private static final Gson gson = new Gson();

        public String translate(String text, String sourceLang, String targetLang)
        {
            try
            {
                String jsonBody = """
                {
                    "q": "%s",
                    "source": "%s",
                    "target": "%s",
                    "format": "text"
                }
                """.formatted(text, sourceLang, targetLang);

                String response = HttpUtil.post(
                        "https://libretranslate.de/translate",
                        jsonBody,
                        "application/json"
                );

                Map<?, ?> json = gson.fromJson(response, Map.class);
                return json.get("translatedText").toString();

            } catch (Exception e) {
                e.printStackTrace();
                return text; // Fallback
            }
        }
}


