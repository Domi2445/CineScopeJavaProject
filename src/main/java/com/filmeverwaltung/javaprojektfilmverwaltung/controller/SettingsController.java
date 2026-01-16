package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Language;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private Button btnSaveSettings;

    @FXML
    private ComboBox<Language> cmbLanguage;


    @FXML
    private CheckBox chkStatistics;

    @FXML
    private CheckBox chkDarkMode;

    @FXML
    private Label lblLoading;



    @FXML
    private void initialize()
    {
        cmbLanguage.getItems().setAll(Language.values());


        // Zeige Loading-Label
        lblLoading.setVisible(true);

        // TODO: Hier könnten Einstellungen geladen werden
        // Für jetzt verstecke das Loading-Label sofort
        lblLoading.setVisible(false);

    }


}

