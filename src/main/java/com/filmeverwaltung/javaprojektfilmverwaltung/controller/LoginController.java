package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController
{

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    @FXML
    private void initialize()
    {
        lblError.setVisible(false);
    }

    @FXML
    private Button btnLogin;

    @FXML
    private void onRegisterClick()
    {
        handleRegister();
    }


    @FXML
    private void handleLogin()
    {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty())
        {
            showError("Bitte Benutzername und Passwort eingeben.");
            return;
        }

        // später: echte Login-Logik (DB, Hashing, Rollen)
        // showError("Ungültige Zugangsdaten.");  // Beispiel
    }

    private void showError(String message)
    {
        lblError.setText(message);
        lblError.setStyle("-fx-text-fill: red;");
        lblError.setVisible(true);
    }


    private void handleRegister()
    {
        // Registrierung-Logik hier implementieren
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Fehler beim Laden der Registrierung.");
            e.printStackTrace();
        }
    }


}
