// Datei: `src/main/java/com/filmeverwaltung/javaprojektfilmverwaltung/controller/SearchController.java`
package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.db.FilmRepository;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.OmdbService;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LoadingOverlay;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.TranslationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchController {

    private static final Logger LOGGER = Logger.getLogger(SearchController.class.getName());

    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSearch;
    @FXML
    private ComboBox<String> cmbGenre;
    @FXML
    private Label lblLoading;
    @FXML
    private TableView<Filmmodel> tableResults;
    @FXML
    private TableColumn<Filmmodel, String> colTitle;
    @FXML
    private TableColumn<Filmmodel, String> colYear;
    @FXML
    private TableColumn<Filmmodel, String> colRating;
    @FXML
    private TableColumn<Filmmodel, String> colWriter;
    @FXML
    private TableColumn<Filmmodel, String> colPlot;

    private final OmdbService omdbService = new OmdbService(ApiConfig.OMDB_API_KEY);
    private final FilmRepository filmRepository = new FilmRepository();
    private final LoadingOverlay overlay = new LoadingOverlay();
    private final SearchFilterController filterController = new SearchFilterController();

    @FXML
    private void initialize() {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colYear.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getYear()));
        colRating.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getImdbRating()));
        colWriter.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWriter()));
        colPlot.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlot()));

        colRating.setVisible(false);

        tableResults.setRowFactory(tv -> {
            TableRow<Filmmodel> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openDetail(row.getItem());
                }
            });
            return row;
        });

        // Initialize genre ComboBox
        cmbGenre.setItems(FXCollections.observableArrayList(filterController.getAvailableGenres()));
        cmbGenre.getSelectionModel().selectFirst();
        cmbGenre.setOnAction(event -> {
            // Reapply filter to current results when genre selection changes
            if (!tableResults.getItems().isEmpty()) {
                List<Filmmodel> currentResults = new java.util.ArrayList<>(tableResults.getItems());
                updateSearchResults(currentResults);
            }
        });
        // Add Enter key listener to search field
        txtSearch.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSearch();
                event.consume();
            }
        });

        txtSearch.setVisible(false);
        txtSearch.setDisable(true);
        btnSearch.setVisible(false);
        btnSearch.setDisable(true);
        cmbGenre.setVisible(false);
        cmbGenre.setDisable(true);

        loadTopMovies();
    }

    private void hideLoadingScreen() {
        overlay.hide();
    }

    private void loadTopMovies() {
        tableResults.setVisible(false);
        lblLoading.setVisible(false);
        btnSearch.setDisable(true);
        btnSearch.setVisible(false);
        txtSearch.setDisable(true);
        txtSearch.setVisible(false);
        cmbGenre.setDisable(true);
        cmbGenre.setVisible(false);

        if (tableResults != null && tableResults.getScene() != null) {
            overlay.show(tableResults.getScene().getWindow(), "Lade beliebteste Filme...");
        } else {
            overlay.show(null, "Lade beliebteste Filme...");
        }

        Task<List<Filmmodel>> task = new Task<>() {
            @Override
            protected List<Filmmodel> call() {
                try {
                    return filmRepository.getTopMovies(15);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Fehler beim Laden der beliebtesten Filme", e);
                    return List.of();
                }
            }
        };

        task.setOnSucceeded(e -> {
            List<Filmmodel> topMovies = new java.util.ArrayList<>(task.getValue());
            topMovies.removeIf(film -> film.getImdbRating() == null || film.getImdbRating().isEmpty() || "N/A".equalsIgnoreCase(film.getImdbRating()));

            if (!topMovies.isEmpty()) {
                topMovies.sort((film1, film2) -> {
                    try {
                        double rating1 = Double.parseDouble(film1.getImdbRating());
                        double rating2 = Double.parseDouble(film2.getImdbRating());
                        return Double.compare(rating2, rating1);
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                });

                colRating.setVisible(true);
                tableResults.setItems(FXCollections.observableArrayList(topMovies));
                tableResults.setVisible(true);
            } else {
                lblLoading.setText("Keine Filme mit gültiger Bewertung gefunden");
                lblLoading.setVisible(true);
            }

            txtSearch.setVisible(true);
            txtSearch.setDisable(false);
            btnSearch.setVisible(true);
            btnSearch.setDisable(false);
            cmbGenre.setVisible(true);
            cmbGenre.setDisable(false);
            hideLoadingScreen();
        });

        task.setOnFailed(e -> {
            LOGGER.log(Level.SEVERE, "Fehler beim Laden der beliebtesten Filme", task.getException());
            hideLoadingScreen();
            lblLoading.setVisible(false);
            tableResults.setVisible(true);
            txtSearch.setVisible(true);
            txtSearch.setDisable(false);
            btnSearch.setVisible(true);
            btnSearch.setDisable(false);
            cmbGenre.setVisible(true);
            cmbGenre.setDisable(false);
        });

        new Thread(task).start();
    }

    @FXML
    private void handleSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) return;

        colRating.setVisible(false);
        tableResults.setVisible(false);
        lblLoading.setVisible(false);
        btnSearch.setDisable(true);
        txtSearch.setDisable(true);
        cmbGenre.setDisable(true);

        if (tableResults != null && tableResults.getScene() != null) {
            overlay.show(tableResults.getScene().getWindow(), "Suche wird ausgeführt...");
        } else {
            overlay.show(null, "Suche wird ausgeführt...");
        }

        Task<List<Filmmodel>> task = new Task<>() {
            @Override
            protected List<Filmmodel> call() {
                List<Filmmodel> results = omdbService.searchByTitle(query);
                if (results.isEmpty()) {
                    Filmmodel single = omdbService.getFilmByTitle(query);
                    if (single != null && !"False".equalsIgnoreCase(single.getResponse())) {
                        return List.of(single);
                    }
                }
                return results;
            }
        };

        task.setOnSucceeded(e -> {
            List<Filmmodel> list = new java.util.ArrayList<>(task.getValue());

            if (list.isEmpty()) {
                hideLoadingScreen();
                lblLoading.setText("Keine Filme gefunden");
                lblLoading.setVisible(true);
                tableResults.setVisible(false);
                btnSearch.setDisable(false);
                txtSearch.setDisable(false);
                cmbGenre.setDisable(false);
                return;
            }

            final int[] pendingTasks = {0};

            for (Filmmodel film : list) {
                if ((film.getWriter() == null || film.getWriter().isEmpty() || "N/A".equalsIgnoreCase(film.getWriter())) ||
                        (film.getImdbRating() == null || film.getImdbRating().isEmpty() || "N/A".equalsIgnoreCase(film.getImdbRating()))) {

                    pendingTasks[0]++;

                    Task<Void> detailsTask = new Task<>() {
                        @Override
                        protected Void call() {
                            try {
                                if (film.getImdbID() != null && !film.getImdbID().isEmpty()) {
                                    Filmmodel fullFilm = omdbService.getFilmById(film.getImdbID());
                                    if (fullFilm != null) {
                                        if (fullFilm.getImdbRating() != null && !"N/A".equalsIgnoreCase(fullFilm.getImdbRating())) {
                                            film.setImdbRating(fullFilm.getImdbRating());
                                        }
                                        if (fullFilm.getWriter() != null && !"N/A".equalsIgnoreCase(fullFilm.getWriter())) {
                                            film.setWriter(fullFilm.getWriter());
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Fehler beim Laden der Filmdetails", ex);
                            }
                            return null;
                        }
                    };

                    detailsTask.setOnSucceeded(ev -> {
                        pendingTasks[0]--;
                        if (pendingTasks[0] == 0) {
                            updateSearchResults(list);
                        }
                    });

                    new Thread(detailsTask).start();
                }
            }

            if (pendingTasks[0] == 0) {
                updateSearchResults(list);
            }

            // Wenn Translation API einen Fehler gemeldet hat, zeige einen Alert
            String tmError = TranslationUtil.getLastError();
            if (tmError != null) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Übersetzungsfehler");
                    alert.setHeaderText("MyMemory API Fehler");
                    alert.setContentText(tmError);
                    alert.showAndWait();
                    TranslationUtil.clearLastError();
                });
            }
        });

        task.setOnFailed(e -> {
            LOGGER.log(Level.SEVERE, "Fehler bei der Filmsuche", task.getException());
            hideLoadingScreen();
            lblLoading.setVisible(false);
            tableResults.setVisible(true);
            btnSearch.setDisable(false);
            txtSearch.setDisable(false);
            cmbGenre.setDisable(false);
        });

        new Thread(task).start();
    }

    private void updateSearchResults(List<Filmmodel> list) {
        // Get selected genre filter
        String selectedGenre = cmbGenre.getSelectionModel().getSelectedItem();

        // Filter results by genre
        List<Filmmodel> filteredList = list.stream()
                .filter(film -> {
                    String filmGenre = film.getGenre();
                    return filterController.filterByGenre(filmGenre, selectedGenre);
                })
                .toList();

        // If no results after filtering, show message
        if (filteredList.isEmpty() && selectedGenre != null && !"Alle Genres".equalsIgnoreCase(selectedGenre)) {
            hideLoadingScreen();
            lblLoading.setText("Keine Filme in diesem Genre gefunden");
            lblLoading.setVisible(true);
            tableResults.setVisible(false);
        } else {
            tableResults.setItems(FXCollections.observableArrayList(filteredList));
            hideLoadingScreen();
            lblLoading.setVisible(false);
            tableResults.setVisible(true);
        }

        btnSearch.setDisable(false);
        txtSearch.setDisable(false);
        cmbGenre.setDisable(false);
    }


    private void openDetail(Filmmodel film) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/detail.fxml"));
            Scene scene = new Scene(loader.load());

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(tableResults.getScene().getWindow());
            dialog.setTitle(film.getTitle());
            dialog.setScene(scene);

            DetailController ctrl = loader.getController();
            ctrl.setDialogStage(dialog);
            ctrl.setFilm(film);

            dialog.show();

            if (film.getPlot() == null || film.getWriter() == null ||
                    "N/A".equals(film.getPlot()) || "N/A".equals(film.getWriter())) {

                Task<Filmmodel> loadTask = new Task<>() {
                    @Override
                    protected Filmmodel call() {
                        if (film.getImdbID() != null && !film.getImdbID().isBlank()) {
                            return omdbService.getFilmById(film.getImdbID());
                        }
                        return omdbService.getFilmByTitle(film.getTitle());
                    }
                };

                loadTask.setOnSucceeded(ev -> {
                    Filmmodel fullFilm = loadTask.getValue();
                    if (fullFilm != null) {
                        if (fullFilm.getWriter() != null) film.setWriter(fullFilm.getWriter());
                        if (fullFilm.getPlot() != null) film.setPlot(fullFilm.getPlot());
                        if (fullFilm.getImdbRating() != null) film.setImdbRating(fullFilm.getImdbRating());
                        if (fullFilm.getPoster() != null) film.setPoster(fullFilm.getPoster());

                        ctrl.setFilm(film);

                        Task<Void> saveTask = new Task<>() {
                            @Override
                            protected Void call() {
                                try {
                                    filmRepository.addOrUpdateFilm(film);
                                } catch (java.sql.SQLException e) {
                                    LOGGER.log(Level.WARNING, "Fehler beim Speichern des Films in der Datenbank", e);
                                }
                                return null;
                            }
                        };
                        new Thread(saveTask).start();
                    }

                    // Wenn MyMemory API Fehler hatte, zeige Alert
                    String tmError2 = TranslationUtil.getLastError();
                    if (tmError2 != null) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Übersetzungsfehler");
                            alert.setHeaderText("MyMemory API Fehler");
                            alert.setContentText(tmError2);
                            alert.showAndWait();
                            TranslationUtil.clearLastError();
                        });
                    }
                });

                loadTask.setOnFailed(ev -> LOGGER.log(Level.SEVERE, "Fehler beim Nachladen der Filmdetails", loadTask.getException()));

                new Thread(loadTask).start();
            } else {
                Task<Void> saveTask = new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            filmRepository.addOrUpdateFilm(film);
                        } catch (java.sql.SQLException e) {
                            LOGGER.log(Level.WARNING, "Fehler beim Speichern des Films in der Datenbank", e);
                        }
                        return null;
                    }
                };
                new Thread(saveTask).start();
            }

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Öffnen des Detail-Dialogs", e);
        }
    }
}
