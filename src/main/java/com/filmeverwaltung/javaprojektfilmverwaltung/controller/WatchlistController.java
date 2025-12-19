package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class WatchlistController
{
    @FXML
    private TableView<Filmmodel> tableWatchlist;

    @FXML
    private TableColumn<Filmmodel, String> colTitle;

    @FXML
    private TableColumn<Filmmodel, String> colYear;

    @FXML
    private TableColumn<Filmmodel, String> colRating;

    @FXML
    private Button btnExport;

    @FXML
    private void initialize() {
        // CellValueFactory an Filmmodel binden
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("writer")); // falls rating nicht vorhanden, temporär writer anzeigen

        // Beispiel-Daten, damit beim Öffnen etwas sichtbar ist (später durch echte Daten ersetzen)
        Filmmodel f1 = new Filmmodel("Der Pate", "1972", "Mario Puzo", "Mafiasaga");
        f1.setImdbID("tt0068646");
        Filmmodel f2 = new Filmmodel("Inception", "2010", "Christopher Nolan", "Traum-in-Traum");
        f2.setImdbID("tt1375666");

        ObservableList<Filmmodel> sample = FXCollections.observableArrayList(
                f1,
                f2
        );
        tableWatchlist.setItems(sample);

        // Row factory für Doppelklick
        tableWatchlist.setRowFactory(tv -> {
            TableRow<Filmmodel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Filmmodel rowData = row.getItem();
                    openDetail(rowData);
                }
            });
            return row;
        });
    }

    private void openDetail(Filmmodel film) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/detail.fxml"));
            Scene scene = new Scene(loader.load());
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (tableWatchlist != null && tableWatchlist.getScene() != null) {
                dialog.initOwner(tableWatchlist.getScene().getWindow());
            }
            dialog.setTitle(film.getTitle() == null ? "Details" : film.getTitle());
            dialog.setScene(scene);
            DetailController ctrl = loader.getController();
            ctrl.setDialogStage(dialog);
            ctrl.setFilm(film);
            dialog.show();

            // Bei Watchlist brauchen wir keine weitere Ergänzung durch Service, da hier die Daten aus der lokalen Watchlist kommen
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
