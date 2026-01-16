package com.filmeverwaltung.javaprojektfilmverwaltung.controller;


import com.filmeverwaltung.javaprojektfilmverwaltung.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Controller für die Root-Ansicht der Anwendung.
 * Verwaltet die Navigation zwischen verschiedenen Ansichten wie Suche, Watchlist, Verlauf und Einstellungen.
 */
public class RootController
{
    private static final Logger LOGGER = Logger.getLogger(RootController.class.getName());

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnLoginLogout;


    @FXML
    private void initialize()
    {
        loadView("/fxml/search.fxml");
        updateLoginButtonText();
    }

    private void updateLoginButtonText()
    {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn())
        {
            btnLoginLogout.setText("👤 Abmelden");
        }
        else
        {
            btnLoginLogout.setText("🔒 Anmelden");
        }
    }

    @FXML
    private void onSearch()
    {
        loadView("/fxml/search.fxml");
    }

    @FXML
    private void onWatchlist()
    {
        loadView("/fxml/watchlist.fxml");
    }

    @FXML
    private void onFavorites()
    {
        loadView("/fxml/favorites.fxml");
    }

    @FXML
    private void onHistory()
    {
        loadView("/fxml/history.fxml");
    }

    @FXML
    private void onSettings()
    {
        loadView("/fxml/settings.fxml");
    }

    private void loadView(String path)
    {
        try
        {
            Parent view = FXMLLoader.load(getClass().getResource(path));
            contentArea.getChildren().setAll(view);
        } catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "Fehler beim Laden der View: " + path, e);
        }
    }

    @FXML
    private void onLoginLogout()
    {
        SessionManager session = SessionManager.getInstance();
        if (session.isLoggedIn())
        {
            // Logout
            session.logout();
            updateLoginButtonText();
            loadView("/fxml/search.fxml");
        }
        else
        {
            // Login
            loadView("/fxml/login.fxml");
        }
    }

    
}
