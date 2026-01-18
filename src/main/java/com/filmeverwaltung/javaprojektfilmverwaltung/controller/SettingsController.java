package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.Config;
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
    private CheckBox chkDarkMode;

    @FXML
    private Button btnSaveSettings;

    @FXML
    private ComboBox<Language> cmbLanguage;


    @FXML
    private Label lblLoading;



    @FXML
    private void initialize() {

        // 1️⃣ Config laden
        Config aktuelleConfig = Config.load();

        // 2️⃣ Sprache-ComboBox befüllen
        cmbLanguage.getItems().setAll(Language.values());

        // 3️⃣ Sprache aus Config setzen
        cmbLanguage.getSelectionModel()
                .select(LanguageUtil.getLanguage());

        // 4️⃣ Dark Mode aus Config setzen
        chkDarkMode.setSelected(aktuelleConfig.app.darkMode);

        // 5️⃣ Dark Mode anwenden
        applyDarkMode(aktuelleConfig.app.darkMode);

        // 6️⃣ Loading-Label aus
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


        boolean darkModeEnabled = chkDarkMode.isSelected();
        applyDarkMode(darkModeEnabled);

        Config aktuell = Config.load();
        aktuell.app.darkMode=chkDarkMode.isSelected();
        aktuell.save();
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

    private void applyDarkMode(boolean darkMode) {

        var scene = chkDarkMode.getScene();
        if (scene == null) return;

        scene.getStylesheets().clear();

        if (darkMode) {
            scene.getStylesheets().add(
                    getClass().getResource("/css/dark.css").toExternalForm()
            );
        } else {
            scene.getStylesheets().add(
                    getClass().getResource("/css/light.css").toExternalForm()
            );
        }
    }


}




