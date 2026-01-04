package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.FavoritesHandler;
import com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.WatchlistHandler;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.OmdbService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.collections.FXCollections.observableArrayList;

public class WatchlistController
{
    private static final Logger LOGGER = Logger.getLogger(WatchlistController.class.getName());

    @FXML
    private TableView<Filmmodel> tableWatchlist;

    @FXML
    private Label lblLoading;

    @FXML
    private TableColumn<Filmmodel, String> colTitle;

    @FXML
    private TableColumn<Filmmodel, String> colYear;

    @FXML
    private TableColumn<Filmmodel, String> colRating;

    @FXML
    private TableColumn<Filmmodel, Void> colFavorites;

    @FXML
    private Button btnExport;

    @FXML
    private ComboBox<String> cmbSize;

    private final OmdbService omdbService = new OmdbService(ApiConfig.OMDB_API_KEY);
    private final FavoritesHandler favoritesHandler = new FavoritesHandler();

    @FXML
    private void initialize()
    {
        // Zeige Loading-Label
        lblLoading.setVisible(true);

        // Größenauswahl ComboBox initialisieren
        cmbSize.setItems(FXCollections.observableArrayList("Klein", "Standard", "Groß"));
        cmbSize.setValue("Standard");
        cmbSize.setOnAction(event -> anpasseTabellenschriftgroesse());

        // Spalten binden
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));

        // Button-Spalte für "Zu Favoriten hinzufügen"
        colFavorites.setCellFactory(new Callback<TableColumn<Filmmodel, Void>, TableCell<Filmmodel, Void>>()
        {
            @Override
            public TableCell<Filmmodel, Void> call(TableColumn<Filmmodel, Void> param)
            {
                return new TableCell<Filmmodel, Void>()
                {
                    private final Button btnAddToFavorites = new Button("Zu Favoriten");

                    {
                        btnAddToFavorites.setOnAction(event ->
                        {
                            Filmmodel film = getTableView().getItems().get(getIndex());
                            if (film != null && film.getImdbID() != null)
                            {
                                favoritesHandler.fuegeFilmHinzu(film.getImdbID());
                                // Button-Text ändern als Feedback
                                btnAddToFavorites.setText("✓ Hinzugefügt");
                                btnAddToFavorites.setDisable(true);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (empty)
                        {
                            setGraphic(null);
                        } else
                        {
                            Filmmodel film = getTableView().getItems().get(getIndex());
                            // Prüfen, ob bereits in Favoriten
                            if (film != null && favoritesHandler.istFavorit(film.getImdbID()))
                            {
                                btnAddToFavorites.setText("✓ Hinzugefügt");
                                btnAddToFavorites.setDisable(true);
                            } else
                            {
                                btnAddToFavorites.setText("Zu Favoriten");
                                btnAddToFavorites.setDisable(false);
                            }
                            setGraphic(btnAddToFavorites);
                        }
                    }
                };
            }
        });


        // -----------------------------------------
        // WATCHLIST AUS JSON LADEN
        // -----------------------------------------
        WatchlistHandler handler = new WatchlistHandler();
        List<String> ids = handler.lesen();
        ObservableList<Filmmodel> filme = observableArrayList();
        tableWatchlist.getColumns().setAll(observableArrayList(colTitle, colYear, colRating, colFavorites));
        tableWatchlist.setItems(filme);
        for (String id : ids)
        {
            Filmmodel film = omdbService.getFilmById(id);
            if (film != null)
            {
                filme.add(film);
            }
        }

        // Verstecke Loading-Label am Ende
        lblLoading.setVisible(false);


        // -----------------------------------------
        // Doppelklick für Details
        // -----------------------------------------
        tableWatchlist.setRowFactory(tv ->
        {
            TableRow<Filmmodel> row = new TableRow<>();
            row.setOnMouseClicked(event ->
            {
                if (event.getClickCount() == 2 && (!row.isEmpty()))
                {
                    Filmmodel rowData = row.getItem();
                    openDetail(rowData);
                }
            });
            return row;
        });
    }

    // Film-Detailansicht öffnen
    private void openDetail(Filmmodel film)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/detail.fxml"));
            Scene scene = new Scene(loader.load());
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);

            if (tableWatchlist != null && tableWatchlist.getScene() != null)
            {
                dialog.initOwner(tableWatchlist.getScene().getWindow());
            }

            dialog.setTitle(film.getTitle() == null ? "Details" : film.getTitle());
            dialog.setScene(scene);

            DetailController ctrl = loader.getController();
            ctrl.setDialogStage(dialog);
            ctrl.setFilm(film);

            dialog.show();

            // Falls Daten unvollständig → nachladen
            if (film.getPlot() == null || film.getWriter() == null)
            {
                Task<Filmmodel> task = new Task<>()
                {
                    @Override
                    protected Filmmodel call()
                    {
                        if (film.getImdbID() != null && !film.getImdbID().isBlank())
                        {
                            return omdbService.getFilmById(film.getImdbID());
                        }
                        return omdbService.getFilmByTitle(film.getTitle());
                    }
                };

                task.setOnSucceeded(ev ->
                {
                    Filmmodel full = task.getValue();
                    if (full != null)
                    {
                        Platform.runLater(() -> ctrl.setFilm(full));
                    }
                });

                task.setOnFailed(ev ->
                {
                    LOGGER.log(Level.SEVERE, "Fehler beim Nachladen der Filmdetails", task.getException());
                });

                Thread th = new Thread(task, "omdb-detail-watchlist");
                th.setDaemon(true);
                th.start();
            }

        } catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "Fehler beim Öffnen des Detail-Dialogs", e);
        }
    }

    @FXML
    private void handleDeleteSelected()
    {
        Filmmodel film = tableWatchlist.getSelectionModel().getSelectedItem();
        if (film == null)
        {
            return; // Nichts ausgewählt
        }

        // Aus Datei entfernen
        WatchlistHandler handler = new WatchlistHandler();
        handler.entferneFilm(film.getImdbID());

        // Aus Tabelle entfernen
        tableWatchlist.getItems().remove(film);
    }

    private void anpasseTabellenschriftgroesse()
    {
        String groesse = cmbSize.getValue();
        String style = "";

        switch (groesse)
        {
            case "Klein":
                style = "-fx-font-size: 10px;";
                break;
            case "Standard":
                style = "-fx-font-size: 12px;";
                break;
            case "Groß":
                style = "-fx-font-size: 14px;";
                break;
        }

        tableWatchlist.setStyle(style);
    }

}
