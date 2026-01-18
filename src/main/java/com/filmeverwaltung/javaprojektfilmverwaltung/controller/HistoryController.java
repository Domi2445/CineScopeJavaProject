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

        lblLoading.setVisible(true);



        lblLoading.setVisible(false);
    }
}

