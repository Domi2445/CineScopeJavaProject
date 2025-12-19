package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DetailController implements Initializable {

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblYear;

    @FXML
    private Label lblWriter;

    @FXML
    private TextArea txtPlot;

    @FXML
    private ImageView imgPoster;

    private Stage dialogStage;
    private Filmmodel film;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setFilm(Filmmodel film) {
        System.out.println("DetailController.setFilm called with film=" + (film == null ? "null" : film.getTitle()));
        this.film = film;
        // Versuche sofort UI zu aktualisieren; falls Felder noch null, wird initialize() es später erledigen
        populateFields();
        loadPosterAsync();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("DetailController.initialize called. FXML fields: lblTitle=" + (lblTitle == null ? "null" : "ok") +
                ", lblYear=" + (lblYear == null ? "null" : "ok") + ", lblWriter=" + (lblWriter == null ? "null" : "ok") +
                ", txtPlot=" + (txtPlot == null ? "null" : "ok") + ", imgPoster=" + (imgPoster == null ? "null" : "ok"));
        // Falls film bereits gesetzt wurde, fülle die Felder
        populateFields();
        // Falls setFilm schon aufgerufen wurde, Poster laden
        loadPosterAsync();
    }

    private void populateFields() {
        // UI-Updates auf JavaFX Thread
        Platform.runLater(() -> {
            if (film == null) {
                System.out.println("DetailController.populateFields: film is null");
                return;
            }
            if (lblTitle != null) lblTitle.setText(film.getTitle() == null ? "-" : film.getTitle());
            if (lblYear != null) lblYear.setText(film.getYear() == null ? "-" : film.getYear());
            if (lblWriter != null) lblWriter.setText(film.getWriter() == null ? "-" : film.getWriter());
            if (txtPlot != null) txtPlot.setText(film.getPlot() == null ? "Keine Beschreibung" : film.getPlot());
            System.out.println("DetailController: populated fields for film: " + (film.getTitle() == null ? "<null>" : film.getTitle()));
        });
    }

    private void loadPosterAsync() {
        if (film == null) return;
        String posterUrl = film.getPoster();
        if ((posterUrl == null || posterUrl.isBlank()) && film.getImdbID() != null && !film.getImdbID().isBlank()) {
            try {
                String key = com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig.OMDB_API_KEY; // use centralized key
                posterUrl = "https://img.omdbapi.com/?i=" + URLEncoder.encode(film.getImdbID(), StandardCharsets.UTF_8) + "&apikey=" + URLEncoder.encode(key, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Prüfe auf gültige URL
        if (posterUrl == null || posterUrl.isBlank() || posterUrl.equalsIgnoreCase("N/A")) {
            System.out.println("DetailController: kein Poster-URL vorhanden");
            return;
        }
        // Lade Poster asynchron
        String finalPosterUrl = posterUrl;
        System.out.println("DetailController: versuche Poster zu laden von: " + finalPosterUrl);
        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                try (InputStream is = new URL(finalPosterUrl).openStream()) {
                    return new Image(is);
                }
            }
        };
        // Bei Erfolg
        task.setOnSucceeded(ev -> {
            Image img = task.getValue();
            Platform.runLater(() -> {
                if (imgPoster != null) imgPoster.setImage(img);
            });
        });
        // Fehlerbehandlung
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            System.out.println("DetailController: Poster laden fehlgeschlagen: " + ex);
            ex.printStackTrace();
        });

        Thread th = new Thread(task, "poster-load");
        th.setDaemon(true);
        th.start();
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) dialogStage.close();
    }
}
