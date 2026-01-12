package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.util.logging.Logger;

/**
 * Controller für die Einstellungen-Ansicht
 */
public class SettingsController
{
    private static final Logger LOGGER = Logger.getLogger(SettingsController.class.getName());

    @FXML
    private ComboBox<String> cmbLanguage;


    @FXML
    private CheckBox chkStatistics;

    @FXML
    private CheckBox chkDarkMode;

    @FXML
    private Label lblLoading;


    @FXML
    private void initialize()
    {


        // Zeige Loading-Label
        lblLoading.setVisible(true);

        // TODO: Hier könnten Einstellungen geladen werden
        // Für jetzt verstecke das Loading-Label sofort
        lblLoading.setVisible(false);
    }





}

