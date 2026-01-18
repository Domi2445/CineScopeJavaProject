package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.SearchHistoryHandler;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller für die Verlauf-Ansicht
 */
public class HistoryController
{
    private static final Logger LOGGER = Logger.getLogger(HistoryController.class.getName());

    @FXML
    private Label lblLoading;

    @FXML
    private ListView<String> listHistory;

    @FXML
    private Button btnOpenInSearch;

    @FXML
    private Button btnClearHistory;

    private final SearchHistoryHandler historyHandler = new SearchHistoryHandler();

    @FXML
    private void initialize()
    {
        // Zeige Loading-Label
        lblLoading.setVisible(true);
        loadHistory();

        listHistory.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                String sel = listHistory.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    openInSearch(sel);
                }
            }
        });

        btnOpenInSearch.setOnAction(e -> {
            String sel = listHistory.getSelectionModel().getSelectedItem();
            if (sel != null) openInSearch(sel);
        });

        btnClearHistory.setOnAction(e -> {
            historyHandler.loescheAlle();
            listHistory.getItems().clear();
        });

        // Verstecke Loading-Label
        lblLoading.setVisible(false);
    }

    private void loadHistory() {
        List<String> entries = historyHandler.lesen();
        if (entries != null && !entries.isEmpty()) {
            listHistory.getItems().setAll(entries);
        }
    }

    private void openInSearch(String query) {
        try {
            ResourceBundle bundle = LanguageUtil.getBundle();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/search.fxml"), bundle);
            Parent view = loader.load();

            // Setze Suchtext und starte Suche
            SearchController sc = loader.getController();
            sc.startSearch(query);

            // Ersetze zentralen Inhaltsbereich
            Scene scene = listHistory.getScene();
            if (scene != null) {
                StackPane content = (StackPane) scene.lookup("#contentArea");
                if (content != null) {
                    content.getChildren().setAll(view);
                }
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Fehler beim Laden der Such-Ansicht", ex);
        }
    }
}
