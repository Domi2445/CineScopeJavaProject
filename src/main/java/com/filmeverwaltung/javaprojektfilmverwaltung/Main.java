package com.filmeverwaltung.javaprojektfilmverwaltung;

import com.filmeverwaltung.javaprojektfilmverwaltung.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;


/**
 * Hauptklasse der JavaFX-Anwendung
 */
public class Main extends Application
{

    /**
     * Startmethode der JavaFX-Anwendung
     *
     * @param stage Primäre Bühne der Anwendung
     * @throws Exception Bei Fehlern beim Laden der FXML-Datei
     */
    @Override
    public void start(Stage stage) throws Exception
    {

        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/root.fxml")));
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Filme Verwaltung");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/app.png"))));
        stage.show();
    }

    /**
     * Wird aufgerufen, wenn die Anwendung beendet wird
     */
    @Override
    public void stop() throws Exception
    {
        super.stop();
        // Schließe die Datenbankverbindung beim Beenden
        DatabaseManager.closeConnection();
    }

    /**
     * Hauptmethode der Anwendung
     *
     * @param args Kommandozeilenargumente
     */
    public static void main(String[] args)
    {

        launch();
    }

}
