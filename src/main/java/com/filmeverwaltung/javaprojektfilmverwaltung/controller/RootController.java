package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;


/**
 * Controller für die Root-Ansicht der Anwendung.
 * Verwaltet die Navigation zwischen verschiedenen Ansichten wie Suche, Watchlist, Verlauf und Einstellungen.
 */
public class RootController
{

    @FXML
    private StackPane contentArea;

    @FXML
    private void initialize()
    {
        loadView("/fxml/search.fxml");
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
            e.printStackTrace();
        }
    }

    public void onLogin(ActionEvent actionEvent)
    {
        loadView("/fxml/login.fxml");
    }
}
