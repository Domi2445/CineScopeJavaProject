package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.WatchlistHandler;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.ImdbDescriptionProvider;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.TMDbService;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil;
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
import javafx.scene.web.WebView;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DetailController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DetailController.class.getName());

    //initialisierung der FXML-Elemente
    @FXML
    private Label lblTitle;
    @FXML
    private WebView trailerWebView;
    @FXML
    private VBox trailerContainer;
    @FXML
    private Hyperlink lnkTrailerExternal;
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
    private Button btnTranslate;
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

    @FXML
    private Button btnTrailer;

    private Stage dialogStage;
    private Filmmodel film;
    private String lastLoadedFilmTitle = null;  // Verfolge welcher Film bereits geladen wurde

    // Provider zum Nachladen von Beschreibungen
    private final ImdbDescriptionProvider descriptionProvider = new ImdbDescriptionProvider();
    private final TMDbService tmdbService = new TMDbService(ApiConfig.TMDB_API_KEY);

/**
     * Setzt die Dialog-Stage und konfiguriert das Custom-Theme sowie das Schließen-Verhalten.
 * @param stage Die Stage des Dialogs
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;


        if (stage != null && stage.getScene() != null) {
            try {
                var themeUrl = getClass().getResource("/styles/theme.css");
                if (themeUrl != null) {
                    String themeResource = themeUrl.toExternalForm();
                    stage.getScene().getStylesheets().add(themeResource);
                    LOGGER.log(Level.INFO, "✓ Custom-Theme geladen");

                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Fehler beim Laden des Custom-Themes: " + e.getMessage());
            }
        }

        //
        if (stage != null) {
            stage.setOnCloseRequest(event -> stopVideo());
        }
    }

    /**
     * Stoppt das Video in der WebView
     */
    private void stopVideo() {
        if (trailerWebView != null && trailerWebView.getEngine() != null) {
            LOGGER.log(Level.INFO, "Video wird gestoppt");
            // Leere den WebView-Inhalt, um das Video zu stoppen
            trailerWebView.getEngine().loadContent("");
            trailerWebView.setVisible(false);
            trailerWebView.setManaged(false);
        }
    }

    /**
     * Setzt den anzuzeigenden Film und aktualisiert die UI entsprechend.
     * @param film Der anzuzeigende Film
     */
    public void setFilm(Filmmodel film) {

        stopVideo();

        this.film = film;
        lastLoadedFilmTitle = null;
        streamingProvidersBox.getChildren().clear();
        if (similarMoviesSection != null) {
            similarMoviesSection.setVisible(false);
        }
        if (trailerWebView != null) {
            trailerWebView.getEngine().loadContent("");
            trailerWebView.setVisible(false);
            trailerWebView.setManaged(false);
        }
        if (trailerContainer != null) {
            trailerContainer.setVisible(false);
            trailerContainer.setManaged(false);
        }
        if (lnkTrailerExternal != null) {
            lnkTrailerExternal.setText("");
            lnkTrailerExternal.setVisible(false);
            lnkTrailerExternal.setManaged(false);
            lnkTrailerExternal.setOnAction(null);
        }
        if (btnTrailer != null) {
            btnTrailer.setVisible(false);
            btnTrailer.setManaged(false);
        }

        // Übersetzungsbutton-Sichtbarkeit basierend auf Datenquelle steuern
        updateTranslateButtonVisibility();

        aktualisiereUI();
        ladePoster();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        com.filmeverwaltung.javaprojektfilmverwaltung.util.LocalWebServer.start();



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


        if (film.getTitle() != null && !film.getTitle().isBlank()) {
            LOGGER.log(Level.INFO, "🎬 Starte Trailer-Laden für: " + film.getTitle());
            System.out.println("🎬 Starte Trailer-Laden für: " + film.getTitle());

            Task<String> trailerTask = new Task<>() {
                @Override
                protected String call() {
                    try {
                        String result = tmdbService.getTrailerUrlForMovie(film.getTitle());
                        LOGGER.log(Level.INFO, "TMDb-Service zurückgegeben: " + result);
                        System.out.println("TMDb-Service zurückgegeben: " + result);
                        return result;
                    } catch (Exception ex) {
                        LOGGER.log(Level.SEVERE, "Exception in getTrailerUrlForMovie: " + ex.getMessage(), ex);
                        System.err.println("Exception in getTrailerUrlForMovie: " + ex);
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            };

            trailerTask.setOnSucceeded(e -> {
                String trailerUrl = trailerTask.getValue();
                LOGGER.log(Level.INFO, "✓ Trailer-Callback - URL: " + trailerUrl);
                System.out.println("✓ Trailer-Callback - URL: " + trailerUrl);

                Platform.runLater(() -> {
                    if (trailerUrl != null && !trailerUrl.isBlank()) {
                        LOGGER.log(Level.INFO, "✓ Trailer URL ist nicht null - wird verarbeitet");
                        System.out.println("✓ Trailer URL ist nicht null - wird verarbeitet");

                        // Extrahiere Video-ID aus der Embed-URL
                        String videoId = null;
                        if (trailerUrl.contains("embed/")) {
                            videoId = trailerUrl.split("embed/")[1].split("\\?")[0].split("&")[0];
                        } else if (trailerUrl.contains("v=")) {
                            videoId = trailerUrl.split("v=")[1].split("&")[0];
                        }

                        if (videoId == null || videoId.isEmpty()) {
                            LOGGER.log(Level.WARNING, "Konnte Video-ID nicht extrahieren aus: " + trailerUrl);
                            videoId = trailerUrl;
                        }

                        String youtubeWatchUrl = "https://www.youtube.com/watch?v=" + videoId;
                        String youtubeEmbedUrl = "https://www.youtube.com/embed/" + videoId;

                        LOGGER.log(Level.INFO, "Video-ID: " + videoId);
                        LOGGER.log(Level.INFO, "Watch URL: " + youtubeWatchUrl);
                        LOGGER.log(Level.INFO, "Embed URL: " + youtubeEmbedUrl);
                        System.out.println("Video-ID: " + videoId);
                        System.out.println("Watch URL: " + youtubeWatchUrl);
                        System.out.println("Embed URL: " + youtubeEmbedUrl);

                        try {

                            javafx.scene.web.WebEngine engine = trailerWebView.getEngine();
                            engine.setJavaScriptEnabled(true);

                            LOGGER.log(Level.INFO, "WebView-Engine konfiguriert");
                            System.out.println("WebView-Engine konfiguriert");


                            String htmlContent = "<!DOCTYPE html>" +
                                    "<html>" +
                                    "<head>" +
                                    "    <meta charset='UTF-8'>" +
                                    "    <style>" +
                                    "        * { margin: 0; padding: 0; box-sizing: border-box; }" +
                                    "        html, body { width: 100%; height: 100%; background: #0f172a; display: flex; align-items: center; justify-content: center; }" +
                                    "        .wrapper { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; padding: 0; }" +
                                    "        .player { width: 100%; height: 100%; max-width: 1200px; min-height: 520px; aspect-ratio: 16 / 9; border-radius: 14px; overflow: hidden; box-shadow: 0 24px 60px rgba(0,0,0,0.38); }" +
                                    "        iframe { width: 100%; height: 100%; border: none; }" +
                                    "    </style>" +
                                    "</head>" +
                                    "<body>" +
                                    "    <div class='wrapper'>" +
                                    "        <div class='player'>" +
                                    "            <iframe " +
                                    "                src='" + youtubeEmbedUrl + "?autoplay=0&rel=0&fs=1&modestbranding=1' " +
                                    "                allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share' " +
                                    "                allowfullscreen title='Trailer'></iframe>" +
                                    "        </div>" +
                                    "    </div>" +
                                    "</body>" +
                                    "</html>";

                            LOGGER.log(Level.INFO, "HTML wird registriert. Länge: " + htmlContent.length());
                            System.out.println("HTML wird registriert. Länge: " + htmlContent.length());


                            String trailerRoute = "trailer_" + videoId;
                            com.filmeverwaltung.javaprojektfilmverwaltung.util.LocalWebServer.setContent(trailerRoute, htmlContent);
                            String serverUrl = com.filmeverwaltung.javaprojektfilmverwaltung.util.LocalWebServer.getUrl(trailerRoute);

                            LOGGER.log(Level.INFO, "Lade HTML von Webserver: " + serverUrl);
                            System.out.println("Lade HTML von Webserver: " + serverUrl);


                            engine.load(serverUrl);

                            trailerWebView.setVisible(true);
                            trailerWebView.setManaged(true);
                            trailerContainer.setVisible(true);
                            trailerContainer.setManaged(true);


                            lnkTrailerExternal.setText("🎬 Im Browser öffnen");
                            lnkTrailerExternal.setTooltip(new Tooltip("Öffnet den Trailer auf YouTube"));

                            lnkTrailerExternal.setOnAction(ev -> {
                                try {
                                    Desktop.getDesktop().browse(URI.create(youtubeEmbedUrl));
                                    LOGGER.log(Level.INFO, "Trailer-Link geöffnet: " + youtubeEmbedUrl);
                                    System.out.println("Trailer-Link geöffnet: " + youtubeEmbedUrl);
                                } catch (Exception ex) {
                                    LOGGER.log(Level.WARNING, "Konnte Trailer nicht öffnen: " + ex.getMessage(), ex);
                                    System.err.println("Konnte Trailer nicht öffnen: " + ex.getMessage());
                                }
                            });
                            lnkTrailerExternal.setVisible(true);
                            lnkTrailerExternal.setManaged(true);


                            btnTrailer.setVisible(true);
                            btnTrailer.setManaged(true);

                            LOGGER.log(Level.INFO, "✓ Trailer-Anzeige erfolgreich erstellt");
                            System.out.println("✓ Trailer-Anzeige erfolgreich erstellt");
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "❌ Fehler bei der Trailer-Anzeige: " + ex.getMessage(), ex);
                            System.err.println("❌ Fehler bei der Trailer-Anzeige: " + ex.getMessage());
                            ex.printStackTrace();

                            trailerWebView.setVisible(false);
                            trailerWebView.setManaged(false);
                            trailerContainer.setVisible(false);
                            trailerContainer.setManaged(false);
                        }
                    } else {
                        LOGGER.log(Level.INFO, "⚠ Kein Trailer gefunden für: " + film.getTitle());
                        System.out.println("⚠ Kein Trailer gefunden für: " + film.getTitle());
                        trailerWebView.setVisible(false);
                        trailerWebView.setManaged(false);
                        trailerContainer.setVisible(false);
                        trailerContainer.setManaged(false);
                        lnkTrailerExternal.setVisible(false);
                        lnkTrailerExternal.setManaged(false);
                        btnTrailer.setVisible(false);
                        btnTrailer.setManaged(false);
                    }
                });
            });

            trailerTask.setOnFailed(e -> {
                Throwable ex = e.getSource().getException();
                LOGGER.log(Level.SEVERE, "❌ Fehler beim Trailer-Laden: " + (ex != null ? ex.getMessage() : "Unbekannter Fehler"), ex);
                System.err.println("❌ Fehler beim Trailer-Laden: " + (ex != null ? ex.getMessage() : "Unbekannter Fehler"));
                if (ex != null) ex.printStackTrace();

                trailerWebView.setVisible(false);
                trailerWebView.setManaged(false);
                trailerContainer.setVisible(false);
                trailerContainer.setManaged(false);
                lnkTrailerExternal.setVisible(false);
                lnkTrailerExternal.setManaged(false);
                btnTrailer.setVisible(false);
                btnTrailer.setManaged(false);
            });

            Thread trailerThread = new Thread(trailerTask);
            trailerThread.setName("Trailer-Loader-" + film.getTitle());
            trailerThread.setDaemon(false);
            trailerThread.start();
        } else {
            LOGGER.log(Level.INFO, "Film-Titel ist leer, Trailer wird nicht geladen");
            trailerWebView.setVisible(false);
            trailerWebView.setManaged(false);
            trailerContainer.setVisible(false);
            trailerContainer.setManaged(false);
            lnkTrailerExternal.setVisible(false);
            lnkTrailerExternal.setManaged(false);
            btnTrailer.setVisible(false);
            btnTrailer.setManaged(false);
        }


        ladeStreamingAnbieter();


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
                    throw e;
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
        stopVideo();
        if (dialogStage != null) dialogStage.close();
    }

    @FXML
    private void handleTranslatePlot() {
        if (film == null) return;

        // Übersetze von Englisch in die aktuelle UI-Sprache
        String targetLang = switch (LanguageUtil.getLanguage()) {
            case DE -> "de";
            case EN -> "en"; // Keine Übersetzung nötig
            case AR -> "ar";
            case PL -> "pl";
        };

        if (!targetLang.equals("en")) {
            txtPlot.setText(new TranslationUtil().translate(txtPlot.getText(), "en", targetLang));
            lblTitle.setText(new TranslationUtil().translate(lblTitle.getText(), "en", targetLang));
        }

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
     * Zeigt den Trailer an (Toggle zwischen versteckt und sichtbar)
     */
    @FXML
    private void handleShowTrailer() {
        if (trailerContainer != null) {
            // Toggle Sichtbarkeit
            boolean currentlyVisible = trailerContainer.isVisible();
            trailerContainer.setVisible(!currentlyVisible);
            trailerContainer.setManaged(!currentlyVisible);
        }
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

    /**
     * Steuert die Sichtbarkeit des Übersetzungsbuttons basierend auf der Datenquelle und Sprachverfügbarkeit
     */
    private void updateTranslateButtonVisibility() {
        if (btnTranslate == null || film == null) return;


        boolean showTranslateButton = false;


        if (film.getTitle() != null && isEnglishText(film.getTitle())) {
            showTranslateButton = true;
        }


        if (film.getPlot() != null && !film.getPlot().equals("N/A") && isEnglishText(film.getPlot())) {
            showTranslateButton = true;
        }


        if (film.getWriter() != null && !film.getWriter().equals("N/A") && isEnglishText(film.getWriter())) {
            showTranslateButton = true;
        }

        btnTranslate.setVisible(showTranslateButton);
        btnTranslate.setManaged(showTranslateButton);
    }

    /**
     * Prüft ob der Text wahrscheinlich auf Englisch ist
     * Einfache Heuristik: Wenn mehr als 70% der Wörter englische Wörter sind
     */
    private boolean isEnglishText(String text) {
        if (text == null || text.trim().isEmpty()) return false;

        // Bekannte englische Wörter (könnte erweitert werden)
        String[] englishWords = {
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does",
            "did", "will", "would", "could", "should", "may", "might", "must", "can", "shall"
        };

        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("\\s+");

        int englishWordCount = 0;
        for (String word : words) {
            // Entferne Satzzeichen für bessere Erkennung
            word = word.replaceAll("[^a-zA-Z]", "");
            if (word.length() > 0) {
                for (String englishWord : englishWords) {
                    if (word.equals(englishWord)) {
                        englishWordCount++;
                        break;
                    }
                }
            }
        }


        return words.length > 0 && (double) englishWordCount / words.length > 0.3;
    }
}
