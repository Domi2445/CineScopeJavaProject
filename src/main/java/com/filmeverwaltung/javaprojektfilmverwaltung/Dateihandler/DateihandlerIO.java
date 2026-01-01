package com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler;

import java.io.*;

public class DateihandlerIO extends Dateihandler {


    public DateihandlerIO() {
        try {
            File file = new File(WATCHLIST_PATH);
            if (!file.exists()) {
                file.createNewFile();
                try (FileWriter fw = new FileWriter(WATCHLIST_PATH)) {
                    fw.write("[]");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

