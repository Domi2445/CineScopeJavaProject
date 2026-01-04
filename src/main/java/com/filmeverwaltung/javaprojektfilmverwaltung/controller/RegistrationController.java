package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.db.UserRepository;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.User;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.PasswordUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class RegistrationController
{

    @FXML
    private TextField txtUsername;

    @FXML
    private TextField txtEmail;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtPasswordConfirm;

    @FXML
    private Label lblError;

    @FXML
    private Label lblSuccess;

    private final UserRepository userRepo = new UserRepository();

    public void initialize()
    {
        if (lblError != null) lblError.setVisible(false);
        if (lblSuccess != null) lblSuccess.setVisible(false);
    }

    @FXML
    public void handleRegister(ActionEvent actionEvent)
    {
        if (lblError != null)
        {
            lblError.setVisible(false);
        }
        if (lblSuccess != null)
        {
            lblSuccess.setVisible(false);
        }

        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String passwordConfirm = txtPasswordConfirm.getText();

        if (username.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty())
        {
            showError("Benutzername und Passwort müssen ausgefüllt werden.");
            return;
        }

        if (username.length() < 3)
        {
            showError("Benutzername muss mindestens 3 Zeichen haben.");
            return;
        }

        if (password.length() < 6)
        {
            showError("Passwort muss mindestens 6 Zeichen haben.");
            return;
        }

        if (!password.equals(passwordConfirm))
        {
            showError("Passwörter stimmen nicht überein.");
            return;
        }

        try
        {
            if (userRepo.existsByUsername(username))
            {
                showError("Benutzername ist bereits vergeben.");
                return;
            }

            // Prüfe E-Mail falls angegeben
            if (!email.isBlank())
            {
                // Einfache E-Mail-Validierung
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
                {
                    showError("Ungültige E-Mail-Adresse.");
                    return;
                }

                if (userRepo.existsByEmail(email))
                {
                    showError("Es existiert bereits ein Benutzer mit dieser E-Mail.");
                    return;
                }
            }

            String hashed = PasswordUtil.hashPassword(password);
            User u = new User();
            u.setUsername(username);
            u.setPasswordHash(hashed);
            u.setEmail(email.isBlank() ? null : email);
            u.setCreatedAt(LocalDateTime.now());
            u.setActive(true);

            int id = userRepo.insertUser(u);
            if (id > 0)
            {
                showSuccess("Registrierung erfolgreich. Zurück zum Login...");
                // zurück zum Login - ersetze contentArea
                navigateBackToLogin();
            } else
            {
                showError("Fehler beim Anlegen des Benutzers.");
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
            showError("Datenbankfehler: " + e.getMessage());
        }
    }

    private void navigateBackToLogin()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            StackPane contentArea = (StackPane) txtUsername.getScene().getRoot().lookup("#contentArea");
            if (contentArea != null)
            {
                contentArea.getChildren().setAll(root);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    public void backToLogin(ActionEvent actionEvent)
    {
        navigateBackToLogin();
    }

    private void showError(String msg)
    {
        if (lblError != null)
        {
            lblError.setText(msg);
            lblError.setVisible(true);
        }
    }

    private void showSuccess(String msg)
    {
        if (lblSuccess != null)
        {
            lblSuccess.setText(msg);
            lblSuccess.setVisible(true);
        }
    }
}
