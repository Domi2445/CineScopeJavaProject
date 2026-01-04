package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

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
    private void initialize()
    {
        // Zeige Loading-Label
        lblLoading.setVisible(true);

        // TODO: Hier könnten Verlauf-Daten geladen werden
        // Für jetzt verstecke das Loading-Label sofort
        lblLoading.setVisible(false);
    }
}

