package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.db.FilmRepository;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.OmdbService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchController
{
    private static final Logger LOGGER = Logger.getLogger(SearchController.class.getName());

    @FXML
    private TextField txtSearch;
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

    /**
     * Initialisiert die Suchansicht und setzt die Tabellenkonfiguration.
     *
     * @Param void
     * @Return void
     *
     */
    @FXML
    private void initialize()
    {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colYear.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getYear()));
        colRating.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getImdbRating()));
        colWriter.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWriter()));
        colPlot.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlot()));

        // Rating-Spalte standardmäßig verstecken (wird nur bei beliebtesten Filmen angezeigt)
        colRating.setVisible(false);

        tableResults.setRowFactory(tv ->
        {
            TableRow<Filmmodel> row = new TableRow<>();
            row.setOnMouseClicked(e ->
            {
                if (e.getClickCount() == 2 && !row.isEmpty())
                {
                    openDetail(row.getItem());
                }
            });
            return row;
        });

        // Lade beliebteste Filme beim Start
        loadTopMovies();
    }

    /**
     * Lädt die 10 beliebtesten Filme und zeigt diese an
     */
    private void loadTopMovies()
    {
        // Zeige "Laden..." und verstecke Tabelle
        lblLoading.setVisible(true);
        tableResults.setVisible(false);

        Task<List<Filmmodel>> task = new Task<>()
        {
            @Override
            protected List<Filmmodel> call()
            {
                try
                {
                    return filmRepository.getTopMovies(15);
                }
                catch (Exception e)
                {
                    LOGGER.log(Level.WARNING, "Fehler beim Laden der beliebtesten Filme", e);
                    return List.of();
                }
            }
        };

        task.setOnSucceeded(e ->
        {
            List<Filmmodel> topMovies = new java.util.ArrayList<>(task.getValue());

            // Filtere Filme mit "N/A" oder fehlender Bewertung
            topMovies.removeIf(film -> film.getImdbRating() == null || film.getImdbRating().isEmpty() || "N/A".equalsIgnoreCase(film.getImdbRating()));

            if (!topMovies.isEmpty())
            {
                // Sortiere nach Rating absteigend (beste zuerst!)
                topMovies.sort((film1, film2) ->
                {
                    try
                    {
                        double rating1 = Double.parseDouble(film1.getImdbRating());
                        double rating2 = Double.parseDouble(film2.getImdbRating());
                        return Double.compare(rating2, rating1); // Absteigend: beste zuerst
                    }
                    catch (NumberFormatException ex)
                    {
                        return 0;
                    }
                });

                // Zeige Rating-Spalte für beliebteste Filme
                colRating.setVisible(true);
                tableResults.setItems(FXCollections.observableArrayList(topMovies));

                // Verstecke "Laden..." und zeige Tabelle
                lblLoading.setVisible(false);
                tableResults.setVisible(true);
            }
            else
            {
                // Keine gültigen Filme gefunden
                lblLoading.setText("Keine Filme mit gültiger Bewertung gefunden");
                lblLoading.setVisible(true);
            }
        });

        task.setOnFailed(e ->
        {
            LOGGER.log(Level.SEVERE, "Fehler beim Laden der beliebtesten Filme", task.getException());
            // Verstecke "Laden..." auch bei Fehler
            lblLoading.setVisible(false);
            tableResults.setVisible(true);
        });

        new Thread(task).start();
    }

    /**
     * Führt eine Suche nach Filmen basierend auf dem eingegebenen Titel durch.
     *
     * @Param void
     * @Return void
     *
     */
    @FXML
    private void handleSearch()
    {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) return;

        // Verstecke Rating-Spalte für Suche
        colRating.setVisible(false);

        // Zeige "Laden..." und verstecke Tabelle
        lblLoading.setVisible(true);
        tableResults.setVisible(false);

        Task<List<Filmmodel>> task = new Task<>()
        {
            @Override
            protected List<Filmmodel> call()
            {
                List<Filmmodel> results = omdbService.searchByTitle(query);
                if (results.isEmpty())
                {
                    Filmmodel single = omdbService.getFilmByTitle(query);
                    if (single != null && !"False".equalsIgnoreCase(single.getResponse()))
                    {
                        return List.of(single);
                    }
                }
                return results;
            }
        };

        task.setOnSucceeded(e ->
        {
            List<Filmmodel> list = new java.util.ArrayList<>(task.getValue());

            System.out.println("========== SUCHE: API Ergebnisse erhalten ==========");
            System.out.println("Anzahl Filme: " + list.size());

            if (list.isEmpty())
            {
                lblLoading.setText("Keine Filme gefunden");
                lblLoading.setVisible(true);
                tableResults.setVisible(false);
                return;
            }

            // Zähler für asynchrone Tasks
            final int[] pendingTasks = {0};



            // Lade für jeden Film die vollständigen Details mit Rating nach (asynchron)
            for (Filmmodel film : list)
            {
                // Prüfe ob Writer oder Rating fehlt
                if ((film.getWriter() == null || film.getWriter().isEmpty() || "N/A".equalsIgnoreCase(film.getWriter())) ||
                    (film.getImdbRating() == null || film.getImdbRating().isEmpty() || "N/A".equalsIgnoreCase(film.getImdbRating())))
                {
                    pendingTasks[0]++;

                    Task<Void> detailsTask = new Task<>()
                    {
                        @Override
                        protected Void call()
                        {
                            try
                            {
                                if (film.getImdbID() != null && !film.getImdbID().isEmpty())
                                {
                                    Filmmodel fullFilm = omdbService.getFilmById(film.getImdbID());
                                    if (fullFilm != null)
                                    {
                                        // Aktualisiere Rating
                                        if (fullFilm.getImdbRating() != null && !"N/A".equalsIgnoreCase(fullFilm.getImdbRating()))
                                        {
                                            film.setImdbRating(fullFilm.getImdbRating());
                                        }
                                        // Aktualisiere Writer
                                        if (fullFilm.getWriter() != null && !"N/A".equalsIgnoreCase(fullFilm.getWriter()))
                                        {
                                            film.setWriter(fullFilm.getWriter());
                                        }
                                    }
                                }
                            }
                            catch (Exception ex)
                            {
                                System.err.println("  ✗ Fehler beim Laden: " + ex.getMessage());
                                LOGGER.log(Level.WARNING, "Fehler beim Laden der Filmdetails", ex);
                            }
                            return null;
                        }
                    };

                    detailsTask.setOnSucceeded(ev -> {
                        pendingTasks[0]--;
                        System.out.println("  ✓ Details-Task fertig (pendingTasks = " + pendingTasks[0] + ")");
                        if (pendingTasks[0] == 0)
                        {
                            // Alle Details geladen - jetzt Tabelle aktualisieren
                            updateSearchResults(list);
                        }
                    });

                    new Thread(detailsTask).start();
                }
            }



            // Falls keine Tasks ausstehen - sofort anzeigen
            if (pendingTasks[0] == 0)
            {

                updateSearchResults(list);
            }
        });

        task.setOnFailed(e ->
        {
            LOGGER.log(Level.SEVERE, "Fehler bei der Filmsuche", task.getException());
            // Verstecke "Laden..." auch bei Fehler
            lblLoading.setVisible(false);
            tableResults.setVisible(true);
        });

        new Thread(task).start();
    }

    /**
     * Aktualisiert die Suchergebnisse in der Tabelle
     * Zeigt Filme OHNE zu sortieren (nur in Startansicht sortieren!)
     */
    private void updateSearchResults(List<Filmmodel> list)
    {

        for (int i = 0; i < list.size(); i++)
        {
            Filmmodel film = list.get(i);

        }

        tableResults.setItems(FXCollections.observableArrayList(list));

        // Verstecke "Laden..." und zeige Tabelle
        lblLoading.setVisible(false);
        tableResults.setVisible(true);
    }

    /**
     * Öffnet das Detailfenster für den ausgewählten Film.
     *
     * @Param film Der Film, dessen Details angezeigt werden sollen.
     * @Return void
     *
     */
    private void openDetail(Filmmodel film)
    {
        try
        {
            // Öffne Detail-Fenster SOFORT (vor Datenbankzugriff)
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

            // Falls Details fehlen → ZUERST nachladen, DANN in DB speichern
            if (film.getPlot() == null || film.getWriter() == null ||
                "N/A".equals(film.getPlot()) || "N/A".equals(film.getWriter()))
            {
                Task<Filmmodel> loadTask = new Task<>()
                {
                    @Override
                    protected Filmmodel call()
                    {
                        if (film.getImdbID() != null && !film.getImdbID().isBlank())
                        {
                            return omdbService.getFilmById(film.getImdbID());
                        }
                        return omdbService.getFilmByTitle(film.getTitle());
                    }
                };

                loadTask.setOnSucceeded(ev ->
                {
                    Filmmodel fullFilm = loadTask.getValue();
                    if (fullFilm != null)
                    {
                        // Aktualisiere das ursprüngliche Film-Objekt mit den Details
                        if (fullFilm.getWriter() != null) film.setWriter(fullFilm.getWriter());
                        if (fullFilm.getPlot() != null) film.setPlot(fullFilm.getPlot());
                        if (fullFilm.getImdbRating() != null) film.setImdbRating(fullFilm.getImdbRating());
                        if (fullFilm.getPoster() != null) film.setPoster(fullFilm.getPoster());

                        // Aktualisiere die Anzeige im DetailController
                        ctrl.setFilm(film);

                        // Speichere den vollständigen Film in der Datenbank (asynchron)
                        Task<Void> saveTask = new Task<>()
                        {
                            @Override
                            protected Void call() throws Exception
                            {
                                try
                                {
                                    filmRepository.addOrUpdateFilm(film);
                                }
                                catch (java.sql.SQLException e)
                                {
                                    LOGGER.log(Level.WARNING, "Fehler beim Speichern des Films in der Datenbank", e);
                                }
                                return null;
                            }
                        };
                        new Thread(saveTask).start();
                    }
                });

                loadTask.setOnFailed(ev ->
                {
                    LOGGER.log(Level.SEVERE, "Fehler beim Nachladen der Filmdetails", loadTask.getException());
                });

                new Thread(loadTask).start();
            }
            else
            {
                // Film hat bereits alle Details → direkt in DB speichern
                Task<Void> saveTask = new Task<>()
                {
                    @Override
                    protected Void call() throws Exception
                    {
                        try
                        {
                            filmRepository.addOrUpdateFilm(film);
                        }
                        catch (java.sql.SQLException e)
                        {
                            LOGGER.log(Level.WARNING, "Fehler beim Speichern des Films in der Datenbank", e);
                        }
                        return null;
                    }
                };
                new Thread(saveTask).start();
            }

        } catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "Fehler beim Öffnen des Detail-Dialogs", e);
        }
    }
}
