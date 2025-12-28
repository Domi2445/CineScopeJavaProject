package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.TranslationUtil;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class DetailController implements Initializable {

    @FXML private Label lblTitle;
    @FXML private Label lblYear;
    @FXML private Label lblWriter;
    @FXML private TextArea txtPlot;
    @FXML private ImageView imgPoster;

    private Stage dialogStage;
    private Filmmodel film;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setFilm(Filmmodel film) {
        this.film = film;
        aktualisiereUI();
        ladePoster();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (film != null) {
            aktualisiereUI();
            ladePoster();
        }
    }

    private void aktualisiereUI() {
        if (film == null) return;

        lblTitle.setText(valueOrDash(film.getTitle()));
        lblYear.setText(valueOrDash(film.getYear()));
        lblWriter.setText(valueOrDash(film.getWriter()));

        if("N/A".equals(film.getPlot()) || film.getPlot() == null || film.getPlot().isBlank()) {
            txtPlot.setText("No Description Available");
        } else
        {
            txtPlot.setText(film.getPlot());
        }

    }

    private String valueOrDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private void ladePoster() {
        if (film == null) return;

        String url = film.getPoster();

        if ((url == null || url.isBlank() || url.equalsIgnoreCase("N/A"))
                && film.getImdbID() != null) {
            url = "https://img.omdbapi.com/?i=" +
                    URLEncoder.encode(film.getImdbID(), StandardCharsets.UTF_8) +
                    "&apikey=" + ApiConfig.OMDB_API_KEY;
        }

        if (url == null || url.isBlank()) return;

        final String posterUrl = url;

        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                try (InputStream is = new URL(posterUrl).openStream()) {
                    return new Image(is);
                }
            }
        };

        task.setOnSucceeded(e -> imgPoster.setImage(task.getValue()));


        new Thread(task).start();
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void handleAddToWatchlist()
    {
        System.out.println("Watchlist-Button funktioniert!");
    }

    @FXML
    private void handleTranslatePlot() {
        if (film == null) return;
        txtPlot.setText(new TranslationUtil().translate(txtPlot.getText(), "en", "de"));
        lblTitle.setText(new TranslationUtil().translate(lblTitle.getText(), "en", "de"));

    }


}
