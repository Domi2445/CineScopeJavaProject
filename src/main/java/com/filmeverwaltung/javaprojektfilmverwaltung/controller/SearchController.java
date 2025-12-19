package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.OmdbService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SearchController {

    @FXML
    private TextField txtSearch;

    @FXML
    private TableView<Filmmodel> tableResults;

    @FXML
    private TableColumn<Filmmodel, String> colTitle;

    @FXML
    private TableColumn<Filmmodel, String> colYear;

    @FXML
    private TableColumn<Filmmodel, String> colWriter;

    @FXML
    private TableColumn<Filmmodel, String> colPlot;

    private final OmdbService omdbService = new OmdbService("2c918cab");

    @FXML
    private void initialize() {
        colTitle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        colYear.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getYear()));
        colWriter.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWriter()));
        colPlot.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPlot()));

        // Row factory für Doppelklick auf eine Zeile
        tableResults.setRowFactory(tv -> {
            TableRow<Filmmodel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Filmmodel rowData = row.getItem();
                    System.out.println("SearchController.openDetail: clicked film='" + rowData.getTitle() + "', year='" + rowData.getYear() + "', writer='" + rowData.getWriter() + "', plotNull=" + (rowData.getPlot()==null));
                    openDetail(rowData);
                }
            });
            return row;
        });
    }

    @FXML
    private void handleSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            return;
        }

        System.out.println("Starte Suche: " + query);

        // Suche asynchron ausführen, damit die GUI nicht blockiert wird
        Task<List<Filmmodel>> task = new Task<>() {
            @Override
            protected List<Filmmodel> call() {
                // Erst: Suche nach mehreren Treffern (s=)
                List<Filmmodel> results = omdbService.searchByTitle(query);
                // Wenn keine Treffer über Search, versuche genaue Titel-Abfrage (t=)
                if (results.isEmpty()) {
                    Filmmodel single = omdbService.getFilmByTitle(query);
                    if (single != null && !"False".equalsIgnoreCase(single.getResponse())) {
                        return List.of(single);
                    }
                }
                return results;
            }
        };

        task.setOnSucceeded(ev -> {
            List<Filmmodel> res = task.getValue();
            Platform.runLater(() -> {
                tableResults.getItems().clear();
                if (res != null && !res.isEmpty()) {
                    tableResults.getItems().addAll(res);
                    System.out.println("Treffer: " + res.size());
                } else {
                    System.out.println("Keine Treffer für: " + query);
                }
            });
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
        });

        Thread th = new Thread(task, "omdb-search");
        th.setDaemon(true);
        th.start();
    }

    private void openDetail(Filmmodel film) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/detail.fxml"));
            Scene scene = new Scene(loader.load());
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            // Setze Owner, damit das Dialog modal zum Hauptfenster ist
            if (tableResults != null && tableResults.getScene() != null) {
                dialog.initOwner(tableResults.getScene().getWindow());
            }
            dialog.setTitle(film.getTitle() == null ? "Details" : film.getTitle());
            dialog.setScene(scene);
            DetailController ctrl = loader.getController();
            ctrl.setDialogStage(dialog);
            // Zeige vorhandene (teilweise) Daten sofort
            ctrl.setFilm(film);

            // Öffne das Dialog nicht blockierend, damit asynchron nachgeladen werden kann
            dialog.show();

            // Wenn Plot/Writer leer, lade komplette Details asynchron und aktualisiere die View
            if (film.getPlot() == null || film.getWriter() == null) {
                Task<Filmmodel> task = new Task<>() {
                    @Override
                    protected Filmmodel call() {
                        return omdbService.getFilmByTitle(film.getTitle());
                    }
                };

                task.setOnSucceeded(ev -> {
                    Filmmodel full = task.getValue();
                    if (full != null) {
                        Platform.runLater(() -> ctrl.setFilm(full));
                    }
                });

                task.setOnFailed(ev -> {
                    Throwable ex = task.getException();
                    ex.printStackTrace();
                });

                Thread th = new Thread(task, "omdb-detail");
                th.setDaemon(true);
                th.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}