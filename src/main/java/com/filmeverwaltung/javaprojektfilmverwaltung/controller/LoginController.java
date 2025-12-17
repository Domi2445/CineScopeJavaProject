package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    @FXML
    private void initialize() {
        lblError.setVisible(false);
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Bitte Benutzername und Passwort eingeben.");
            return;
        }

        // später: echte Login-Logik (DB, Hashing, Rollen)
        // showError("Ungültige Zugangsdaten.");  // Beispiel
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: red;");
        lblError.setVisible(true);
    }
}
