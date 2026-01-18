package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.WatchlistHandler;
import com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.FavoritesHandler;
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
import javafx.scene.layout.Region;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;

import static javafx.collections.FXCollections.observableArrayList;

public class WatchlistController {

    private static final Logger LOGGER = Logger.getLogger(WatchlistController.class.getName());

    @FXML
    private TableView<Filmmodel> tableWatchlist;

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

    private volatile String lastOpenedId = null;
    private volatile long lastOpenedAt = 0L;
    // Map to keep track of currently open detail dialogs by id
    private final Map<String, Stage> openDialogs = new ConcurrentHashMap<>();

    @FXML
    private void initialize() {

        // Größenauswahl ComboBox initialisieren
        cmbSize.setItems(FXCollections.observableArrayList("Klein", "Standard", "Groß"));
        cmbSize.setValue("Standard");
        cmbSize.setOnAction(event -> anpasseTabellenschriftgroesse());

        // Spalten binden
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("year"));

        // Button-Spalte für "Zu Favoriten hinzufügen"
        colFavorites.setCellFactory(new Callback<TableColumn<Filmmodel, Void>, TableCell<Filmmodel, Void>>() {
            @Override
            public TableCell<Filmmodel, Void> call(TableColumn<Filmmodel, Void> param) {
                return new TableCell<Filmmodel, Void>() {
                    private final Button btnAddToFavorites = new Button("Zu Favoriten");

                    {
                        btnAddToFavorites.setOnAction(event -> {
                            Filmmodel film = getTableView().getItems().get(getIndex());
                            if (film != null && film.getImdbID() != null) {
                                favoritesHandler.fuegeFilmHinzu(film.getImdbID());
                                // Button-Text ändern als Feedback
                                btnAddToFavorites.setText("✓ Hinzugefügt");
                                btnAddToFavorites.setDisable(true);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Filmmodel film = getTableView().getItems().get(getIndex());
                            // Prüfen, ob bereits in Favoriten
                            if (film != null && favoritesHandler.istFavorit(film.getImdbID())) {
                                btnAddToFavorites.setText("✓ Hinzugefügt");
                                btnAddToFavorites.setDisable(true);
                            } else {
                                btnAddToFavorites.setText("Zu Favoriten");
                                btnAddToFavorites.setDisable(false);
                            }
                            setGraphic(btnAddToFavorites);
                        }
                    }
                };
            }
        });




        WatchlistHandler handler = new WatchlistHandler();
        List<String> ids = handler.lesen();
        ObservableList<Filmmodel> filme = observableArrayList();
        tableWatchlist.getColumns().setAll(observableArrayList(colTitle, colYear, colRating, colFavorites));
        tableWatchlist.setItems(filme);
        for (String id : ids) {
            Filmmodel film = omdbService.getFilmById(id);
            if (film != null) {
                filme.add(film);
            }
        }

        tableWatchlist.setItems(filme);



        tableWatchlist.setRowFactory(tv -> {
            TableRow<Filmmodel> row = new TableRow<>();
            // Ensure row is selected on mouse press so selection model is correct
            row.setOnMousePressed(event -> {
                if (!row.isEmpty()) {
                    row.getTableView().getSelectionModel().select(row.getIndex());
                }
            });
            return row;
        });

        // Ein einziger EventFilter fängt Double-Clicks (Capture-Phase) und öffnet Details einmal
        tableWatchlist.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2) {
                Filmmodel sel = tableWatchlist.getSelectionModel().getSelectedItem();
                LOGGER.log(Level.INFO, "TableView eventFilter double-click, selectedItem=" + (sel == null ? "null" : sel.getTitle()));
                if (sel != null) {
                    openDetail(sel);
                    event.consume();
                }
            }
        });
    }

   /**
     * Öffnet das Detail-Fenster für den gegebenen Film.
     * Verhindert Mehrfachöffnungen desselben Films innerhalb kurzer Zeit (1 Sekunde).
     */
    private void openDetail(Filmmodel film)
    {
        String idForDebounce = film == null ? "" : (film.getImdbID() != null ? film.getImdbID() : film.getTitle());
        long now = System.currentTimeMillis();
        if (idForDebounce != null && !idForDebounce.isEmpty()) {
            if (idForDebounce.equals(lastOpenedId) && (now - lastOpenedAt) < 1000) {
                LOGGER.log(Level.INFO, "Ignored duplicate openDetail for id=" + idForDebounce + " (debounced)");
                return;
            }
        }

        LOGGER.log(Level.INFO, "openDetail called with film=" + (film == null ? "null" : film.getTitle()));
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/detail.fxml"), com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil.getBundle());
            // Lade Root und Scene, setze bevorzugte Breite damit der Trailer Platz hat
            Region root = (Region) loader.load();
            Scene scene = new Scene(root);
            Stage dialog = new Stage();
            // Für Watchlist-öffnungen etwas breiter und höher, damit Trailer bequem sichtbar ist
            double preferredWidth = 1100; // Breiter als Standard
            double preferredHeight = 760;
            root.setPrefWidth(preferredWidth);
            root.setPrefHeight(preferredHeight);
            dialog.setWidth(preferredWidth);
            dialog.setHeight(preferredHeight);
            dialog.setResizable(true);

            dialog.initModality(Modality.APPLICATION_MODAL);

            if (tableWatchlist != null && tableWatchlist.getScene() != null) {
                dialog.initOwner(tableWatchlist.getScene().getWindow());
            }

            dialog.setTitle(film.getTitle() == null ? "Details" : film.getTitle());
            dialog.setScene(scene);

            DetailController ctrl = loader.getController();
            ctrl.setDialogStage(dialog);
            ctrl.setFilm(film);

            // Set debounce state
            if (idForDebounce != null && !idForDebounce.isEmpty()) {
                lastOpenedId = idForDebounce;
                lastOpenedAt = System.currentTimeMillis();
            }

            // If a dialog for this id is already open, bring it to front and don't open new one
            if (idForDebounce != null && !idForDebounce.isEmpty()) {
                Stage existing = openDialogs.get(idForDebounce);
                if (existing != null && existing.isShowing()) {
                    LOGGER.log(Level.INFO, "Detail already open for id=" + idForDebounce + " - bringing to front");
                    Platform.runLater(() -> {
                        try {
                            existing.toFront();
                            existing.requestFocus();
                        } catch (Exception ex) {
                            LOGGER.log(Level.FINE, "Error bringing existing dialog to front", ex);
                        }
                    });
                    return;
                }
            }

            dialog.setOnHidden(e -> {
                try {
                    if (idForDebounce != null && idForDebounce.equals(lastOpenedId)) {
                        lastOpenedId = null;
                        lastOpenedAt = 0L;
                    }
                    if (idForDebounce != null) {
                        openDialogs.remove(idForDebounce);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.FINE, "Error clearing debounce state", ex);
                }
            });

            // register dialog
            if (idForDebounce != null && !idForDebounce.isEmpty()) {
                openDialogs.put(idForDebounce, dialog);
            }


            dialog.show();

            // Falls Daten unvollständig → nachladen
            if (film.getPlot() == null || film.getWriter() == null) {
                Task<Filmmodel> task = new Task<>() {
                    @Override
                    protected Filmmodel call() {
                        if (film.getImdbID() != null && !film.getImdbID().isBlank()) {
                            return omdbService.getFilmById(film.getImdbID());
                        }
                        return omdbService.getFilmByTitle(film.getTitle());
                    }
                };

                task.setOnSucceeded(ev -> {
                    Filmmodel full = task.getValue();
                    if (full != null) {
                        Platform.runLater(() -> ctrl.setFilm(full));
                    }
                });

                task.setOnFailed(ev -> task.getException().printStackTrace());

                Thread th = new Thread(task, "omdb-detail-watchlist");
                th.setDaemon(true);
                th.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleDeleteSelected() {
        Filmmodel film = tableWatchlist.getSelectionModel().getSelectedItem();
        if (film == null) {
            return; // Nichts ausgewählt
        }

        // Aus Datei entfernen
        WatchlistHandler handler = new WatchlistHandler();
        handler.entferneFilm(film.getImdbID());

        // Aus Tabelle entfernen
        tableWatchlist.getItems().remove(film);
    }

    private void anpasseTabellenschriftgroesse() {
        String groesse = cmbSize.getValue();
        String style = "";

        switch (groesse) {
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