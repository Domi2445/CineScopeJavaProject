package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class SearchController {

    @FXML
    private TextField txtSearch;

//    @FXML
//    private Button btnSearch;

//    @FXML
//    private TableView<?> tableResults;
//
//    @FXML
//    private TableColumn<?, ?> colTitle;
//
//    @FXML
//    private TableColumn<?, ?> colYear;
//
//    @FXML
//    private TableColumn<?, ?> colType;

    @FXML
    private void initialize() {
        // Später: Spalten konfigurieren
    }

    @FXML
    private void handleSearch() {
        String query = txtSearch.getText().trim();
        if (query.isEmpty()) {
            return;
        }
    }
}