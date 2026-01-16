package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.db.UserRepository;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.User;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.PasswordUtil;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginController
{
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblError;

    @FXML
    private Button btnLogin;

    private final UserRepository userRepo = new UserRepository();

    @FXML
    private void initialize()
    {
        lblError.setVisible(false);
    }


    @FXML
    private void onRegisterClick()
    {
        handleRegister();
    }


    @FXML
    private void handleLogin()
    {
        lblError.setVisible(false);

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty())
        {
            showError("Bitte Benutzername und Passwort eingeben.");
            return;
        }

        try
        {
            User user = userRepo.findByUsername(username);

            if (user == null)
            {
                showError("Ungültige Zugangsdaten.");
                return;
            }

            // Überprüfe ob der Account aktiv ist
            if (!user.isActive())
            {
                showError("Dieser Account wurde deaktiviert.");
                return;
            }

            // Überprüfe das Passwort
            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash()))
            {
                showError("Ungültige Zugangsdaten.");
                return;
            }

            // Update LAST_LOGIN
            updateLastLogin(user.getUserId());

            // Speichere Session
            SessionManager.getInstance().login(user.getUserId(), user.getUsername());

            // Login erfolgreich - navigiere zur Hauptanwendung
            navigateToMainApp();
        } catch (SQLException e)
        {
            LOGGER.log(Level.SEVERE, "Datenbankfehler beim Login", e);
            showError("Datenbankfehler: " + e.getMessage());
        }
    }

    private void updateLastLogin(int userId)
    {
        try (Connection c = com.filmeverwaltung.javaprojektfilmverwaltung.db.DatabaseManager.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE users SET LAST_LOGIN = ? WHERE USER_ID = ?"))
        {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e)
        {
            // Log, aber blockiere den Login nicht
            System.err.println("Fehler beim Aktualisieren von LAST_LOGIN: " + e.getMessage());
        }
    }

    private void navigateToMainApp()
    {
        try
        {
            // Finde den contentArea parent über die root BorderPane
            Parent currentRoot = txtUsername.getScene() != null ?
                txtUsername.getScene().getRoot() :
                findRootPane();

            if (currentRoot == null)
            {
                showError("Fehler: Root-Layout nicht gefunden.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/search.fxml"));
            Parent searchView = loader.load();

            StackPane contentArea = (StackPane) currentRoot.lookup("#contentArea");
            if (contentArea != null)
            {
                contentArea.getChildren().setAll(searchView);
            }

            // Aktualisiere das Login-Button-Label
            Button btnLoginLogout = (Button) currentRoot.lookup("#btnLoginLogout");
            if (btnLoginLogout != null)
            {
                btnLoginLogout.setText("👤 Abmelden");
                btnLoginLogout.setVisible(false); // Verstecke den Button nach dem Login
                Button btnLogout = (Button) currentRoot.lookup("#btnLogout");
                if (btnLogout != null) {
                    btnLogout.setVisible(true); // Zeige den Logout-Button
                }

            }
        } catch (IOException e)
        {
            showError("Fehler beim Laden der Hauptanwendung.");
            LOGGER.log(Level.SEVERE, "Fehler beim Laden der Hauptanwendung", e);
        }
    }

    private Parent findRootPane()
    {
        // Versuche über den Parent des txtUsername zu gehen
        javafx.scene.Node node = txtUsername;
        while (node != null)
        {
            node = node.getParent();
            if (node instanceof javafx.scene.layout.BorderPane)
            {
                return (Parent) node;
            }
        }
        return null;
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
            Parent currentRoot = txtUsername.getScene() != null ?
                txtUsername.getScene().getRoot() :
                findRootPane();

            if (currentRoot == null)
            {
                showError("Fehler: Root-Layout nicht gefunden.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registration.fxml"));
            Parent registrationView = loader.load();

            // Finde den contentArea (StackPane) im root-Layout
            StackPane contentArea = (StackPane) currentRoot.lookup("#contentArea");
            if (contentArea != null)
            {
                contentArea.getChildren().setAll(registrationView);
            }
        } catch (IOException e)
        {
            showError("Fehler beim Laden der Registrierung.");
            LOGGER.log(Level.SEVERE, "Fehler beim Laden der Registrierung", e);
        }
    }


}
