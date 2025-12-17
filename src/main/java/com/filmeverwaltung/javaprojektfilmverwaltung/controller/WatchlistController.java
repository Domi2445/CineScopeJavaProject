package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class WatchlistController
{
    @FXML
    private TableView<?> tableWatchlist;

    @FXML
    private TableColumn<?, ?> colTitle;

    @FXML
    private TableColumn<?, ?> colYear;

    @FXML
    private TableColumn<?, ?> colRating;

    @FXML
    private Button btnExport;

    @FXML
    private void initialize() {
        // Später: Daten laden
    }
}
