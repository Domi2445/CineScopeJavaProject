package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.model.Language;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.io.IOException;
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

        cmbLanguage.getSelectionModel()
                .select(LanguageUtil.getLanguage());

        // Zeige Loading-Label
        lblLoading.setVisible(true);

        // TODO: Hier könnten Einstellungen geladen werden
        // Für jetzt verstecke das Loading-Label sofort
        lblLoading.setVisible(false);

    }

    @FXML
    private void handleSaveSettings()
    {

        // Sprache speichern
        Language selectedLanguage =
                cmbLanguage.getSelectionModel().getSelectedItem();

        LanguageUtil.setLanguage(selectedLanguage);
        LOGGER.info("Sprache auf " + selectedLanguage + " gesetzt.");
        // Einstellungen speichern (Statistiken, Dark Mode)
        boolean statsEnabled = chkStatistics.isSelected();
        boolean darkModeEnabled = chkDarkMode.isSelected();

       reloadViews();
    }

    private void reloadViews()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/root.fxml"),
                    LanguageUtil.getBundle()

            );
            Parent view = loader.load();

            lblLoading.getScene().setRoot(view);

        }

        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}




