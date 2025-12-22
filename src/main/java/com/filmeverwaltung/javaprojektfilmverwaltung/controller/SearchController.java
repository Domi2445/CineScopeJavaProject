package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.OmdbService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SearchController {

    @FXML private TextField txtSearch;
    @FXML private TableView<Filmmodel> tableResults;
    @FXML private TableColumn<Filmmodel, String> colTitle;
    @FXML private TableColumn<Filmmodel, String> colYear;
    @FXML private TableColumn<Filmmodel, String> colWriter;
    @FXML private TableColumn<Filmmodel, String> colPlot;

    private final OmdbService omdbService = new OmdbService(ApiConfig.OMDB_API_KEY);

    @FXML
    private void initialize() {
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colYear.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getYear()));
        colWriter.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWriter()));
        colPlot.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlot()));

        tableResults.setRowFactory(tv -> {
            TableRow<Filmmodel> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openDetail(row.getItem());
                }
            });
            return row;
        });
    }

    @FXML
    private void handleSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) return;

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
            List<Filmmodel> list = task.getValue();
            tableResults.setItems(FXCollections.observableArrayList(list));
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
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

            // Falls Details fehlen → nachladen
            if (film.getPlot() == null || film.getWriter() == null) {
                Task<Filmmodel> loadTask = new Task<>() {
                    @Override
                    protected Filmmodel call() {
                        if (film.getImdbID() != null && !film.getImdbID().isBlank()) {
                            return omdbService.getFilmById(film.getImdbID());
                        }
                        return omdbService.getFilmByTitle(film.getTitle());
                    }
                };

                loadTask.setOnSucceeded(ev -> ctrl.setFilm(loadTask.getValue()));
                loadTask.setOnFailed(ev -> loadTask.getException().printStackTrace());

                new Thread(loadTask).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
