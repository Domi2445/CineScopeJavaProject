package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

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
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registration.fxml"));
            Parent root = loader.load();

            // Finde den contentArea (StackPane) im root-Layout
            StackPane contentArea = (StackPane) txtUsername.getScene().getRoot().lookup("#contentArea");
            if (contentArea != null)
            {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e)
        {
            showError("Fehler beim Laden der Registrierung.");
            e.printStackTrace();
        }
    }


}
