package com.filmeverwaltung.javaprojektfilmverwaltung.controller;

import com.filmeverwaltung.javaprojektfilmverwaltung.ApiConfig;
import com.filmeverwaltung.javaprojektfilmverwaltung.Dateihandler.FavoritesHandler;
import com.filmeverwaltung.javaprojektfilmverwaltung.model.Filmmodel;
import com.filmeverwaltung.javaprojektfilmverwaltung.service.OmdbService;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LanguageUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FavoritesController
{
    private static final Logger LOGGER = Logger.getLogger(FavoritesController.class.getName());

    @FXML
    private GridPane gridFavorites;

    @FXML
    private ComboBox<String> cmbImageSize;

    @FXML
    private Label lblLoading;

    private final OmdbService omdbService = new OmdbService(ApiConfig.OMDB_API_KEY);
    private final FavoritesHandler handler = new FavoritesHandler();

    private int imageWidth = 150;
    private int imageHeight = 200;

    @FXML
    public void initialize()
    {

        lblLoading.setVisible(true);


        cmbImageSize.setItems(FXCollections.observableArrayList("Klein", "Standard", "Groß"));
        cmbImageSize.setValue("Standard");
        cmbImageSize.setOnAction(event ->
        {
            String size = cmbImageSize.getValue();
            switch (size)
            {
                case "Klein":
                    imageWidth = 100;
                    imageHeight = 150;
                    break;
                case "Standard":
                    imageWidth = 150;
                    imageHeight = 200;
                    break;
                case "Groß":
                    imageWidth = 200;
                    imageHeight = 300;
                    break;
            }
            // Grid neu laden
            gridFavorites.getChildren().clear();
            loadFavorites();
        });

        loadFavorites();
    }

    private void loadFavorites()
    {

        List<String> ids = handler.lesen();

        int column = 0;
        int row = 0;

        List<Filmmodel> allFilms = new ArrayList<>();
        List<Filmmodel> desiredLanguageFilms = new ArrayList<>();


        for (String id : ids)
        {
            Filmmodel film = omdbService.getFilmById(id);
            if (film != null)
            {

                String currentLanguageFilter = LanguageUtil.getCurrentLanguageFilter();
                boolean isInDesiredLanguage = film.getLanguage() != null &&
                                             film.getLanguage().toLowerCase().contains(currentLanguageFilter.toLowerCase());


                allFilms.add(film);
                if (isInDesiredLanguage) {
                    desiredLanguageFilms.add(film);
                }
            }
        }


        List<Filmmodel> filmsToShow = desiredLanguageFilms.size() >= 3 ? desiredLanguageFilms : allFilms;

        for (Filmmodel film : filmsToShow) {
            addMovieCard(film, column, row);
            column++;
            if (column == 3)
            {
                column = 0;
                row++;
            }
        }


        lblLoading.setVisible(false);
    }

    private void addMovieCard(Filmmodel film, int column, int row)
    {
        VBox imageCard = new VBox(5);
        imageCard.setStyle("-fx-border-color: #ddd; -fx-padding: 10; -fx-alignment: center; -fx-cursor: hand;");


        ImageView imageView = new ImageView();
        imageView.setFitWidth(imageWidth);
        imageView.setFitHeight(imageHeight);
        imageView.setPreserveRatio(true);


        Label titleLabel = new Label(film.getTitle() != null ? film.getTitle() : "Unbekannt");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-alignment: center;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(imageWidth);


        ladePoster(film, imageView);


        Button removeButton = new Button("Entfernen");
        removeButton.setOnAction(e ->
        {

            handler.entferneFilm(film.getImdbID());

            updateGrid();
        });


        imageCard.setOnMouseClicked(event ->
        {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2)
            {
                openDetail(film);
            }
        });

        imageCard.getChildren().addAll(imageView, titleLabel, removeButton);
        gridFavorites.add(imageCard, column, row);
    }

    private void ladePoster(Filmmodel film, ImageView imageView)
    {
        String url = film.getPoster();


        if ((url == null || url.isBlank() || url.equalsIgnoreCase("N/A")) && film.getImdbID() != null)
        {
            url = "https://img.omdbapi.com/?i=" + URLEncoder.encode(film.getImdbID(), StandardCharsets.UTF_8) + "&apikey=" + ApiConfig.OMDB_API_KEY;
        }

        if (url == null || url.isBlank()) return;

        final String posterUrl = url;


        Task<Image> task = new Task<>()
        {
            @Override
            protected Image call() throws Exception
            {
                try (InputStream is = URI.create(posterUrl).toURL().openStream())
                {
                    return new Image(is);
                }
            }
        };

        task.setOnSucceeded(e -> imageView.setImage(task.getValue()));
        task.setOnFailed(e ->
        {
            LOGGER.log(Level.WARNING, "Fehler beim Laden des Posters: " + posterUrl, task.getException());
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void openDetail(Filmmodel film)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/detail.fxml"), LanguageUtil.getBundle());
            Scene scene = new Scene(loader.load());
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);

            if (gridFavorites != null && gridFavorites.getScene() != null)
            {
                dialog.initOwner(gridFavorites.getScene().getWindow());
            }

            dialog.setTitle(film.getTitle() == null ? "Details" : film.getTitle());
            dialog.setScene(scene);

            DetailController ctrl = loader.getController();
            ctrl.setDialogStage(dialog);
            ctrl.setFilm(film);

            dialog.show();


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

                task.setOnFailed(new EventHandler<WorkerStateEvent>()
                {
                    @Override
                    public void handle(WorkerStateEvent ev)
                    {
                        LOGGER.log(Level.SEVERE, "Fehler beim Nachladen der Filmdetails", task.getException());
                    }
                });

                Thread th = new Thread(task, "omdb-detail-favorites");
                th.setDaemon(true);
                th.start();
            }

        } catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "Fehler beim Öffnen des Detail-Dialogs", e);
        }
    }

    /**
     * Aktualisiert das Grid, indem alle Elemente entfernt und neu geladen werden.
     * Wird nach dem Entfernen eines Films aufgerufen, um Lücken zu vermeiden.
     */
    private void updateGrid()
    {
        gridFavorites.getChildren().clear();
        loadFavorites();
    }
}
