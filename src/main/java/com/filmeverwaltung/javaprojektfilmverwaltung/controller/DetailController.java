package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.WatchlistHandler;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.ImdbDescriptionProvider;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.TMDbService;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.TranslationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;

public class DetailController implements Initializable {

    @FXML
    private Label lblTitle;
    @FXML
    private Label lblTeaser;
    @FXML
    private Label lblYear;
    @FXML
    private Label lblWriter;
    @FXML
    private TextArea txtPlot;
    @FXML
    private ImageView imgPoster;
    @FXML
    private HBox streamingProvidersBox;
    @FXML
    private Button btnSimilarMovies;
    @FXML
    private VBox similarMoviesSection;
    @FXML
    private TableView<Filmmodel> tableSimilarMovies;
    @FXML
    private TableColumn<Filmmodel, String> colSimilarTitle;
    @FXML
    private TableColumn<Filmmodel, String> colSimilarYear;
    @FXML
    private TableColumn<Filmmodel, String> colSimilarRating;
    @FXML
    private TableColumn<Filmmodel, String> colSimilarPlot;
    @FXML
    private ProgressIndicator progressSimilar;

    private Stage dialogStage;
    private Filmmodel film;
    private String lastLoadedFilmTitle = null;  // Verfolge welcher Film bereits geladen wurde

    // Provider zum Nachladen von Beschreibungen
    private final ImdbDescriptionProvider descriptionProvider = new ImdbDescriptionProvider();
    private final TMDbService tmdbService = new TMDbService(ApiConfig.TMDB_API_KEY);


    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setFilm(Filmmodel film) {
        this.film = film;
        // Reset: neuer Film soll neu geladen werden
        lastLoadedFilmTitle = null;
        streamingProvidersBox.getChildren().clear();
        if (similarMoviesSection != null) {
            similarMoviesSection.setVisible(false);
        }
        aktualisiereUI();
        ladePoster();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisiere Similar Movies Tabelle
        if (colSimilarTitle != null) {
            colSimilarTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
            colSimilarYear.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getYear()));
            colSimilarRating.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getImdbRating()));
            colSimilarPlot.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getPlot() != null && c.getValue().getPlot().length() > 100
                            ? c.getValue().getPlot().substring(0, 100) + "..."
                            : c.getValue().getPlot()
            ));
        }

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

        // Teaser anzeigen oder nachladen
        if (film.getTeaser() != null && !film.getTeaser().isBlank() && !"N/A".equalsIgnoreCase(film.getTeaser())) {
            lblTeaser.setText(film.getTeaser());
            lblTeaser.setVisible(true);
        } else {
            lblTeaser.setText("");
            lblTeaser.setVisible(false);

            // Lade Teaser asynchron von TMDB
            if (film.getTitle() != null && !film.getTitle().isBlank()) {
                Task<String> teaserTask = new Task<>() {
                    @Override
                    protected String call() throws Exception {
                        return tmdbService.getTeaserForMovie(film.getTitle());
                    }
                };

                teaserTask.setOnSucceeded(e -> {
                    String teaser = teaserTask.getValue();
                    if (teaser != null && !teaser.isBlank()) {
                        film.setTeaser(teaser);
                        lblTeaser.setText(teaser);
                        lblTeaser.setVisible(true);
                    }
                });

                new Thread(teaserTask).start();
            }
        }

        // Lade Streaming-Anbieter asynchron
        ladeStreamingAnbieter();

        // Wenn Plot fehlt, zuerst Platzhalter setzen und asynchron nachladen
        if ("N/A".equals(film.getPlot()) || film.getPlot() == null || film.getPlot().isBlank()) {
            txtPlot.setText("No Description Available");

            if (film.getImdbID() != null && !film.getImdbID().isBlank()) {
                Task<String> t = new Task<>() {
                    @Override
                    protected String call() throws Exception {
                        return descriptionProvider.fetchPlotByImdbId(film.getImdbID());
                    }
                };

                t.setOnSucceeded(e -> {
                    String fetched = t.getValue();
                    if (fetched != null && !fetched.isBlank()) {
                        film.setPlot(fetched);
                        txtPlot.setText(fetched);
                    }
                });

                new Thread(t).start();
            }

        } else {
            txtPlot.setText(film.getPlot());
        }
    }

    private void ladeStreamingAnbieter() {
        if (film == null || film.getTitle() == null) return;

        // Verhindere doppeltes Laden: Wenn dieser Film bereits geladen wurde, nicht erneut laden
        if (lastLoadedFilmTitle != null && lastLoadedFilmTitle.equals(film.getTitle())) {
            return;
        }

        // Markiere diesen Film als geladen
        lastLoadedFilmTitle = film.getTitle();

        // Leere die Box komplett
        streamingProvidersBox.getChildren().clear();
        Label labelLoading = new Label("Wird geladen...");
        streamingProvidersBox.getChildren().add(labelLoading);

        Task<List<TMDbService.StreamingProvider>> task = new Task<>() {
            @Override
            protected List<TMDbService.StreamingProvider> call() throws Exception {
                return tmdbService.getStreamingProvidersForMovie(film.getTitle());
            }
        };

        task.setOnSucceeded(e -> {
            List<TMDbService.StreamingProvider> providers = task.getValue();

            if (providers.isEmpty()) {
                streamingProvidersBox.getChildren().clear();
                streamingProvidersBox.getChildren().add(new Label("Keine Streaming-Anbieter gefunden"));
            } else {
                // Lade ALLE Logos ZUERST, bevor sie angezeigt werden
                List<Image> loadedImages = new java.util.ArrayList<>();
                final int[] loadedCount = {0};

                for (int i = 0; i < providers.size(); i++) {
                    TMDbService.StreamingProvider provider = providers.get(i);

                    Task<Image> logoTask = new Task<>() {
                        @Override
                        protected Image call() throws Exception {
                            try (InputStream is = URI.create(provider.logoUrl).toURL().openStream()) {
                                return new Image(is);
                            }
                        }
                    };

                    logoTask.setOnSucceeded(ev -> {
                        loadedImages.add(logoTask.getValue());
                        loadedCount[0]++;

                        // Wenn ALLE Logos geladen sind, DANN anzeigen
                        if (loadedCount[0] == providers.size()) {
                            displayStreamingLogos(providers, loadedImages);
                        }
                    });

                    logoTask.setOnFailed(ev -> {
                        System.err.println("Fehler beim Laden des Logos für: " + provider.name);
                        loadedImages.add(null);  // Placeholder für fehlended Bild
                        loadedCount[0]++;

                        if (loadedCount[0] == providers.size()) {
                            displayStreamingLogos(providers, loadedImages);
                        }
                    });

                    new Thread(logoTask).start();
                }
            }
        });

        task.setOnFailed(e -> {
            streamingProvidersBox.getChildren().clear();
            streamingProvidersBox.getChildren().add(new Label("Fehler beim Laden von Streaming-Anbietern"));
        });

        new Thread(task).start();
    }

    /**
     * Zeigt alle Streaming-Logos an (wird aufgerufen, nachdem ALLE geladen sind)
     */
    private void displayStreamingLogos(List<TMDbService.StreamingProvider> providers, List<Image> images) {
        streamingProvidersBox.getChildren().clear();

        for (int i = 0; i < providers.size(); i++) {
            Image image = images.get(i);
            if (image != null) {
                ImageView logoView = new ImageView(image);
                logoView.setFitWidth(50);
                logoView.setFitHeight(50);
                logoView.setPreserveRatio(true);
                logoView.setStyle("-fx-border-color: #ccc; -fx-padding: 3;");

                // Tooltip mit Provider-Namen
                Tooltip tooltip = new Tooltip(providers.get(i).name);
                Tooltip.install(logoView, tooltip);

                streamingProvidersBox.getChildren().add(logoView);
            }
        }
    }

    /**
     * Lädt ähnliche Filme vom aktuellen Film
     */
    @FXML
    private void handleShowSimilarMovies() {
        if (film == null || film.getTitle() == null) {
            showAlert("Fehler", "Kein Film ausgewählt", "Es konnte kein Film geladen werden.");
            return;
        }

        btnSimilarMovies.setDisable(true);
        progressSimilar.setVisible(true);
        similarMoviesSection.setVisible(true);

        Task<List<Filmmodel>> task = new Task<>() {
            @Override
            protected List<Filmmodel> call() throws Exception {
                return tmdbService.getSimilarMoviesForMovie(film.getTitle());
            }
        };

        task.setOnSucceeded(e -> {
            List<Filmmodel> similarMovies = task.getValue();

            if (similarMovies == null || similarMovies.isEmpty()) {
                showAlert("Keine Ergebnisse", "TMDb", "Für diesen Film konnten keine ähnlichen Filme gefunden werden.");
                progressSimilar.setVisible(false);
                btnSimilarMovies.setDisable(false);
                similarMoviesSection.setVisible(false);
                return;
            }

            tableSimilarMovies.setItems(FXCollections.observableArrayList(similarMovies));
            progressSimilar.setVisible(false);
            btnSimilarMovies.setDisable(false);
        });

        task.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
            showAlert("Fehler", "Ähnliche Filme konnten nicht geladen werden",
                    "Es ist ein Fehler bei der Abfrage der TMDB-API aufgetreten.");
            progressSimilar.setVisible(false);
            btnSimilarMovies.setDisable(false);
            similarMoviesSection.setVisible(false);
        });

        new Thread(task).start();
    }

    private String valueOrDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private void ladePoster() {
        if (film == null) return;

        String url = film.getPoster();

        if ((url == null || url.isBlank() || url.equalsIgnoreCase("N/A")) && film.getImdbID() != null) {
            url = "https://img.omdbapi.com/?i=" + URLEncoder.encode(film.getImdbID(), StandardCharsets.UTF_8) + "&apikey=" + ApiConfig.OMDB_API_KEY;
        }

        if (url == null || url.isBlank()) return;

        final String posterUrl = url;

        Task<Image> task = new Task<>() {
            @Override
            protected Image call() throws Exception {
                try (InputStream is = URI.create(posterUrl).toURL().openStream()) {
                    return new Image(is);
                } catch (Exception e) {
                    // Falls OMDB fehlschlägt, versuche TMDB als Fallback
                    if (film.getTitle() != null && !film.getTitle().isEmpty()) {
                        String tmdbPosterUrl = tmdbService.getPosterUrlForMovie(film.getTitle());
                        if (tmdbPosterUrl != null && !tmdbPosterUrl.isEmpty()) {
                            try (InputStream isTmdb = URI.create(tmdbPosterUrl).toURL().openStream()) {
                                return new Image(isTmdb);
                            }
                        }
                    }
                    throw e; // Wenn auch TMDB fehlschlägt, werfe Exception
                }
            }
        };

        task.setOnSucceeded(e -> imgPoster.setImage(task.getValue()));

        task.setOnFailed(e -> {
            System.err.println("Fehler beim Laden des Posters: " + e.getSource().getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void handleTranslatePlot() {
        if (film == null) return;
        txtPlot.setText(new TranslationUtil().translate(txtPlot.getText(), "en", "de"));
        lblTitle.setText(new TranslationUtil().translate(lblTitle.getText(), "en", "de"));

        // Wenn MyMemory API einen Fehler gemeldet hat, zeige einen Alert in der GUI
        String tmError = com.filmeverwaltung.javaprojektfilmverwaltung.util.TranslationUtil.getLastError();
        if (tmError != null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Übersetzungsfehler");
            alert.setHeaderText("Fehler bei der Übersetzung (MyMemory API)");
            alert.setContentText(tmError);
            alert.showAndWait();
            com.filmeverwaltung.javaprojektfilmverwaltung.util.TranslationUtil.clearLastError();
        }
    }

    @FXML
    private void handleAddToWatchList() {
        WatchlistHandler handler = new WatchlistHandler();
        handler.fuegeFilmHinzu(film.getImdbID());
    }

    /**
     * Hilfsmethode zum Anzeigen von Alerts
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
