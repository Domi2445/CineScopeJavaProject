package com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryHandler extends Dateihandler {

    private static final String HISTORY_PATH = "search_history.json";

    public SearchHistoryHandler() {
        super();
        try {
            File file = new File(HISTORY_PATH);
            if (!file.exists()) {
                file.createNewFile();
                try (FileWriter fw = new FileWriter(HISTORY_PATH)) {
                    fw.write("[]");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> lesen() {
        return lesen(HISTORY_PATH);
    }

    public void speichern(List<String> entries) {
        speichern(entries, HISTORY_PATH);
    }

    public void fuegeEintragHinzu(String query) {
        if (query == null || query.isBlank()) return;
        List<String> list = lesen();
        // Entferne vorhandene gleiche Einträge (Duplikate) und füge ganz oben ein
        list.removeIf(q -> q.equalsIgnoreCase(query));
        list.add(0, query);
        // Begrenze Verlauf auf z.B. 100 Einträge
        if (list.size() > 100) {
            list = list.subList(0, 100);
        }
        speichern(list);
    }

    public void loescheAlle() {

        speichern(new ArrayList<>());
    }
}
