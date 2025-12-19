package com.filmeverwaltung.javaprojektfilmverwaltung.service;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;

import java.util.List;

public class OmdbCliTest {
    public static void main(String[] args) {
        OmdbService s = new OmdbService("2c918cab");
        String q = args.length > 0 ? String.join(" ", args) : "Inception";
        System.out.println("Search für: " + q);
        List<Filmmodel> res = s.searchByTitle(q);
        System.out.println("Ergebnisse: " + res.size());
        for (Filmmodel f : res) {
            System.out.println(f);
        }

        // Versuche genaues Match
        Filmmodel single = s.getFilmByTitle(q);
        if (single != null) {
            System.out.println("\nDetails (getFilmByTitle):\n" + single);
        } else {
            System.out.println("Kein exaktes Ergebnis gefunden.");
        }
    }
}

