// Datei: `src/main/java/com/filmeverwaltung/javaprojektfilmverwaltung/controller/SearchController.java`
package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.db.FilmRepository;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Language;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.OmdbService;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LoadingOverlay;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.TranslationUtil;
import com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.SearchHistoryHandler;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchController {

    private static final Logger LOGGER = Logger.getLogger(SearchController.class.getName());

    // FXML UI Components
    @FXML
    private TextField txtSearch;
    @FXML
    private Button btnSearch;
    @FXML
    private ComboBox<String> cmbGenre;
    @FXML
    private ComboBox<String> cmbLanguage;
    @FXML
    private ComboBox<String> cmbMinRating;
    @FXML
    private ComboBox<String> cmbRuntime;
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

    // Services and utilities
    private final OmdbService omdbService = new OmdbService(ApiConfig.OMDB_API_KEY);
    private final FilmRepository filmRepository = new FilmRepository();
    private final LoadingOverlay overlay = new LoadingOverlay();
    private final SearchFilterController filterController = new SearchFilterController();
    private final SearchHistoryHandler historyHandler = new SearchHistoryHandler();

    @FXML
    private void initialize() {
        // Configure table columns
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colYear.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getYear()));
        colRating.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getImdbRating()));
        colWriter.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWriter()));
        colPlot.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlot()));
        colRating.setVisible(false);

        // Configure table row double-click to open detail view
        tableResults.setRowFactory(tv -> {
            TableRow<Filmmodel> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openDetail(row.getItem());
                }
            });
            return row;
        });

        // Initialize Genre ComboBox
        cmbGenre.setItems(FXCollections.observableArrayList(filterController.getAvailableGenres()));
        cmbGenre.getSelectionModel().selectFirst();
        cmbGenre.setOnAction(event -> refilterCurrentTable());

        // Initialize Language ComboBox
        cmbLanguage.setItems(FXCollections.observableArrayList(filterController.getAvailableLanguages()));
        cmbLanguage.getSelectionModel().selectFirst();
        cmbLanguage.setOnAction(event -> refilterCurrentTable());

        // Initialize Minimum Rating ComboBox
        cmbMinRating.setItems(FXCollections.observableArrayList(filterController.getAvailableRatings()));
        cmbMinRating.getSelectionModel().selectFirst();
        cmbMinRating.setOnAction(event -> refilterCurrentTable());

        // Initialize Runtime ComboBox
        cmbRuntime.setItems(FXCollections.observableArrayList(filterController.getAvailableRuntimes()));
        cmbRuntime.getSelectionModel().selectFirst();
        cmbRuntime.setOnAction(event -> refilterCurrentTable());

        // Add Enter key listener to search field
        txtSearch.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSearch();
                event.consume();
            }
        });

        // Initially hide search and filter controls
        setSearchControlsVisibility(false);

        // Load top movies on startup (jetzt aus lokaler Access-DB / Repository)
        loadTopMovies();
    }

    /**
     * Helper method to set visibility and enable state of all search/filter controls
     */
    private void setSearchControlsVisibility(boolean visible) {
        txtSearch.setVisible(visible);
        txtSearch.setDisable(!visible);
        btnSearch.setVisible(visible);
        btnSearch.setDisable(!visible);
        cmbGenre.setVisible(visible);
        cmbGenre.setDisable(!visible);
        cmbLanguage.setVisible(visible);
        cmbLanguage.setDisable(!visible);
        cmbMinRating.setVisible(visible);
        cmbMinRating.setDisable(!visible);
        cmbRuntime.setVisible(visible);
        cmbRuntime.setDisable(!visible);
    }

    /**
     * Helper method to enable/disable all search/filter controls
     */
    private void setSearchControlsEnabled(boolean enabled) {
        txtSearch.setDisable(!enabled);
        btnSearch.setDisable(!enabled);
        cmbGenre.setDisable(!enabled);
        cmbLanguage.setDisable(!enabled);
        cmbMinRating.setDisable(!enabled);
        cmbRuntime.setDisable(!enabled);
    }

    /**
     * Helper method to hide loading overlay
     */
    private void hideLoadingScreen() {
        overlay.hide();
    }

    /**
     * Helper method to re-filter current table results based on filter changes
     */
    private void refilterCurrentTable() {
        if (tableResults.getItems() == null || tableResults.getItems().isEmpty()) {
            return;
        }
        List<Filmmodel> currentResults = new java.util.ArrayList<>(tableResults.getItems());
        updateSearchResults(currentResults);
    }

    /**
     * Load and display top-rated movies from TMDB via FilmRepository
     */
    private void loadTopMovies() {
        tableResults.setVisible(false);
        lblLoading.setVisible(false);
        colWriter.setVisible(false);
        colPlot.setPrefWidth(400);
        setSearchControlsVisibility(false);

        Task<List<Filmmodel>> task = new Task<>() {
            @Override
            protected List<Filmmodel> call() {
                return filmRepository.getTopMovies(20);
            }

        };

        task.setOnSucceeded(e -> {
            List<Filmmodel> topMovies = new java.util.ArrayList<>(task.getValue());

            // Filtere Filme nach aktueller UI-Sprache, aber zeige auch andere Filme wenn zu wenige gefunden werden
            String currentLanguageFilter = LanguageUtil.getCurrentLanguageFilter();
            List<Filmmodel> filteredMovies = topMovies.stream()
                    .filter(film -> film.getLanguage() != null &&
                            film.getLanguage().toLowerCase().contains(currentLanguageFilter.toLowerCase()))
                    .toList();

            // Wenn weniger als 5 Filme in der gewünschten Sprache gefunden wurden, zeige alle Filme
            if (filteredMovies.size() < 5) {
                filteredMovies = topMovies;
            }

            // Format year and rating for all movies
            for (Filmmodel film : topMovies) {
                // Extract year from date string (e.g., "2025-11-05" -> "2025")
                if (film.getYear() != null && !film.getYear().isEmpty()) {
                    String year = film.getYear();
                    if (year.length() >= 4) {
                        film.setYear(year.substring(0, 4));
                    }
                }
                // Round rating to 2 decimal places
                if (film.getImdbRating() != null && !film.getImdbRating().isEmpty() && !"N/A".equalsIgnoreCase(film.getImdbRating())) {
                    try {
                        double rating = Double.parseDouble(film.getImdbRating());
                        film.setImdbRating(String.format("%.2f", rating));
                    } catch (NumberFormatException ex) {
                        // Keep original value if parsing fails
                    }
                }
            }

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

                final List<Filmmodel> finalTopMovies = new java.util.ArrayList<>(topMovies);

                // Load OMDB posters for all top movies (fallback to TMDB if N/A)
                final AtomicInteger pendingPosterTasks = new AtomicInteger(topMovies.size());
                for (Filmmodel film : topMovies) {
                    Task<Void> posterTask = new Task<>() {
                        @Override
                        protected Void call() {
                            try {
                                // Versuche OMDB-Poster zu laden
                                Filmmodel omdbFilm = omdbService.getFilmByTitle(film.getTitle());
                                if (omdbFilm != null && omdbFilm.getPoster() != null && !"N/A".equalsIgnoreCase(omdbFilm.getPoster())) {
                                    film.setPoster(omdbFilm.getPoster());
                                    return null;
                                }
                                // Falls OMDB kein Poster hat, versuche TMDB
                                // TMDB poster_path ist bereits in film gespeichert (falls vorhanden)
                                // Keine weitere Aktion nötig
                            } catch (Exception ex) {
                                LOGGER.log(Level.FINE, "Fehler beim Laden des OMDB-Posters für: " + film.getTitle(), ex);
                            }
                            return null;
                        }
                    };

                    posterTask.setOnSucceeded(ev -> {
                        if (pendingPosterTasks.decrementAndGet() == 0) {
                            // Alle Poster geladen
                            colRating.setVisible(true);
                            tableResults.setItems(FXCollections.observableArrayList(finalTopMovies));
                            tableResults.setVisible(true);
                            setSearchControlsVisibility(true);
                            hideLoadingScreen();
                        }
                    });

                    posterTask.setOnFailed(ev -> {
                        if (pendingPosterTasks.decrementAndGet() == 0) {
                            colRating.setVisible(true);
                            tableResults.setItems(FXCollections.observableArrayList(finalTopMovies));
                            tableResults.setVisible(true);
                            setSearchControlsVisibility(true);
                            hideLoadingScreen();
                        }
                    });

                    new Thread(posterTask).start();
                }
            } else {
                lblLoading.setText("Keine Filme mit gültiger Bewertung gefunden");
                lblLoading.setVisible(true);
                setSearchControlsVisibility(true);
                hideLoadingScreen();
            }
        });

        task.setOnFailed(e -> {
            LOGGER.log(Level.SEVERE, "Fehler beim Laden der beliebtesten Filme", task.getException());
            hideLoadingScreen();
            lblLoading.setVisible(false);
            tableResults.setVisible(true);
            setSearchControlsVisibility(true);
        });

        new Thread(task).start();
    }

    /**
     * Handle search button click or Enter key press
     */
    @FXML
    private void handleSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) return;

        colRating.setVisible(false);
        tableResults.setVisible(false);
        lblLoading.setVisible(false);
        setSearchControlsEnabled(false);

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


            if (!list.isEmpty()) {
                try {
                    historyHandler.fuegeEintragHinzu(query);
                } catch (Exception ex) {
                    LOGGER.log(Level.FINE, "Fehler beim Speichern in Suchverlauf", ex);
                }
            }



            if (list.isEmpty()) {
                hideLoadingScreen();
                lblLoading.setText("Keine Filme gefunden");
                lblLoading.setVisible(true);
                tableResults.setVisible(false);
                setSearchControlsEnabled(true);
                return;
            }

            final AtomicInteger pendingTasks = new AtomicInteger(0);

            for (Filmmodel film : list) {
                if ((film.getWriter() == null || film.getWriter().isEmpty() || "N/A".equalsIgnoreCase(film.getWriter())) ||
                        (film.getImdbRating() == null || film.getImdbRating().isEmpty() || "N/A".equalsIgnoreCase(film.getImdbRating()))) {

                    pendingTasks.incrementAndGet();

                    Task<Void> detailsTask = new Task<>() {
                        @Override
                        protected Void call() {
                            try {
                                if (film.getImdbID() != null && !film.getImdbID().isEmpty()) {
                                    Filmmodel fullFilm = omdbService.getFilmById(film.getImdbID());
                                    if (fullFilm != null) {
                                        if (fullFilm.getImdbRating() != null && !"N/A".equalsIgnoreCase(fullFilm.getImdbRating())) {
                                            // Round rating to 2 decimal places
                                            try {
                                                double rating = Double.parseDouble(fullFilm.getImdbRating());
                                                film.setImdbRating(String.format("%.2f", rating));
                                            } catch (NumberFormatException ex) {
                                                film.setImdbRating(fullFilm.getImdbRating());
                                            }
                                        }
                                        if (fullFilm.getWriter() != null && !"N/A".equalsIgnoreCase(fullFilm.getWriter())) {
                                            film.setWriter(fullFilm.getWriter());
                                        }
                                        // Poster von OMDB (Fallback zu existierendem TMDB-Poster)
                                        if (fullFilm.getPoster() != null && !"N/A".equalsIgnoreCase(fullFilm.getPoster())) {
                                            film.setPoster(fullFilm.getPoster());
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                LOGGER.log(Level.WARNING, "Fehler beim Laden der Filmdetails", ex);
                            }
                            return null;
                        }
                    };

                    List<Filmmodel> finalList = list;
                    detailsTask.setOnSucceeded(ev -> {
                        if (pendingTasks.decrementAndGet() == 0) {
                            updateSearchResults(finalList);
                        }
                    });

                    new Thread(detailsTask).start();
                } else {

                    pendingTasks.incrementAndGet();
                    Task<Void> posterTask = new Task<>() {
                        @Override
                        protected Void call() {
                            try {
                                if (film.getImdbID() != null && !film.getImdbID().isEmpty()) {
                                    Filmmodel omdbFilm = omdbService.getFilmById(film.getImdbID());
                                    if (omdbFilm != null && omdbFilm.getPoster() != null && !"N/A".equalsIgnoreCase(omdbFilm.getPoster())) {
                                        film.setPoster(omdbFilm.getPoster());
                                    }
                                }
                            } catch (Exception ex) {
                                LOGGER.log(Level.FINE, "Fehler beim Laden des OMDB-Posters", ex);
                            }
                            return null;
                        }
                    };

                    List<Filmmodel> finalList2 = list;
                    posterTask.setOnSucceeded(ev -> {
                        if (pendingTasks.decrementAndGet() == 0) {
                            updateSearchResults(finalList2);
                        }
                    });

                    List<Filmmodel> finalList1 = list;
                    posterTask.setOnFailed(ev -> {
                        if (pendingTasks.decrementAndGet() == 0) {
                            updateSearchResults(finalList1);
                        }
                    });

                    new Thread(posterTask).start();
                }
            }

            if (pendingTasks.get() == 0) {
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
            setSearchControlsEnabled(true);
        });

        new Thread(task).start();
    }

    /**
     * Update search results with all active filters applied
     */
    private void updateSearchResults(List<Filmmodel> list) {
        // Get selected filter values
        final String selectedGenre = cmbGenre.getSelectionModel().getSelectedItem();
        final String selectedLanguage = cmbLanguage.getSelectionModel().getSelectedItem();
        final String selectedMinRating = cmbMinRating.getSelectionModel().getSelectedItem();
        final String selectedRuntime = cmbRuntime.getSelectionModel().getSelectedItem();

        // Apply all filters in sequence
        List<Filmmodel> filteredList = list.stream()
                .filter(f -> filterController.filterByGenre(f.getGenre(), selectedGenre))
                .filter(f -> filterController.filterByLanguage(f.getLanguage(), selectedLanguage))
                .filter(f -> filterController.filterByMinRating(f.getImdbRating(), selectedMinRating))
                .filter(f -> filterController.filterByRuntime(f.getRuntimeInMinutes(), selectedRuntime))
                .toList();

        boolean hasActiveFilters =
                (selectedGenre != null && !"Alle Genres".equalsIgnoreCase(selectedGenre)) ||
                        (selectedLanguage != null && !"Alle Sprachen".equalsIgnoreCase(selectedLanguage)) ||
                        (selectedMinRating != null && !"Alle Bewertungen".equalsIgnoreCase(selectedMinRating)) ||
                        (selectedRuntime != null && !"Alle Laufzeiten".equalsIgnoreCase(selectedRuntime));

        if (filteredList.isEmpty() && hasActiveFilters) {
            hideLoadingScreen();
            lblLoading.setText("Keine Ergebnisse für die gewählten Filter");
            lblLoading.setVisible(true);
            tableResults.setVisible(false);
        } else {
            tableResults.setItems(FXCollections.observableArrayList(filteredList));
            hideLoadingScreen();
            lblLoading.setVisible(false);
            tableResults.setVisible(true);
        }

        setSearchControlsEnabled(true);
    }

    /**
     * Open detail dialog for selected film
     */
    private void openDetail(Filmmodel film) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/detail.fxml"), LanguageUtil.getBundle());
            Scene scene = new Scene(loader.load());

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(tableResults.getScene().getWindow());
            dialog.setTitle(film.getTitle());
            dialog.setScene(scene);

            // Setze maximale Höhe und Breite, damit die ScrollPane funktioniert
            dialog.setMaxHeight(900);
            dialog.setMaxWidth(1400);
            dialog.setMinHeight(600);
            dialog.setMinWidth(1000);

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

    /**
     * Public method to start a search with a given query (used by RootController)
     */
    public void startSearch(String query) {
        if (query == null) return;
        txtSearch.setText(query);
        handleSearch();
    }
}
