package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.db.UserRepository;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.User;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil;
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


            updateLastLogin(user.getUserId());


            SessionManager.getInstance().login(user.getUserId(), user.getUsername());


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

            System.err.println("Fehler beim Aktualisieren von LAST_LOGIN: " + e.getMessage());
        }
    }

    private void navigateToMainApp()
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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/search.fxml"), LanguageUtil.getBundle());
            Parent searchView = loader.load();

            StackPane contentArea = (StackPane) currentRoot.lookup("#contentArea");
            if (contentArea != null)
            {
                contentArea.getChildren().setAll(searchView);
            }


            Button btnLoginLogout = (Button) currentRoot.lookup("#btnLoginLogout");
            if (btnLoginLogout != null)
            {
                java.util.ResourceBundle bundle = com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil.getBundle();
                String logoutLabel = bundle.containsKey("button.logout") ? bundle.getString("button.logout") : "Abmelden";
                btnLoginLogout.setText(logoutLabel);
            }
        } catch (IOException e)
        {
            showError("Fehler beim Laden der Hauptanwendung.");
            LOGGER.log(Level.SEVERE, "Fehler beim Laden der Hauptanwendung", e);
        }
    }

    private Parent findRootPane()
    {

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

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registration.fxml"), LanguageUtil.getBundle());
            Parent registrationView = loader.load();


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
