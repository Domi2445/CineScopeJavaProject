package com.filmeverwaltung.javaprojektfilmverwaltung;

import com.filmeverwaltung.javaprojektfilmverwaltung.db.DatabaseManager;
import com.filmeverwaltung.javaprojektfilmverwaltung.util.LocalWebServer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.LogManager;


/**
 * Hauptklasse der JavaFX-Anwendung
 */
public class Main extends Application
{
    /**
     * Custom PrintStream, der Ausgaben in die Log-Datei schreibt
     */
    static class LoggingPrintStream extends PrintStream {
        private final PrintStream fileStream;

        public LoggingPrintStream(PrintStream fileStream) {
            super(fileStream);
            this.fileStream = fileStream;
        }

        @Override
        public void println(String x) {
            fileStream.println(x);
            fileStream.flush();
        }

        @Override
        public void print(String x) {
            fileStream.print(x);
            fileStream.flush();
        }
    }

    // Statischer Block - wird VOR main() ausgeführt
    static {
        try {
            // Erstelle logs-Verzeichnis, falls es nicht existiert
            Files.createDirectories(Paths.get("logs"));

            // Leite System.out und System.err SO FRÜH WIE MÖGLICH in Log-Datei um
            FileOutputStream logFile = new FileOutputStream("logs/cinescape_system.log", true);
            PrintStream logPrintStream = new PrintStream(logFile, true);
            System.setOut(new LoggingPrintStream(logPrintStream));
            System.setErr(new LoggingPrintStream(logPrintStream));
        } catch (IOException e) {
            // Falls Fehler beim Umleiten, auf stderr ausgeben (noch nicht umgeleitet)
            e.printStackTrace();
        }
    }

    /**
     * Startmethode der JavaFX-Anwendung
     *
     * @param stage Primäre Bühne der Anwendung
     * @throws Exception Bei Fehlern beim Laden der FXML-Datei
     */
    @Override
    public void start(Stage stage) throws Exception
    {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages");
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/fxml/root.fxml")), bundle);
        Parent root = loader.load();

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
        // Stoppe den eingebetteten Webserver, falls er läuft
        LocalWebServer.stop();
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
        // Erstelle logs-Verzeichnis, falls es nicht existiert
        try {
            Files.createDirectories(Paths.get("logs"));
        } catch (IOException e) {
            System.err.println("Konnte logs-Verzeichnis nicht erstellen: " + e.getMessage());
        }

        // Konfiguriere Java Logging aus logging.properties
        try {
            String configPath = Main.class.getResource("/logging.properties").getPath();
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            System.err.println("Fehler beim Laden der Logging-Konfiguration: " + e.getMessage());
        }

        // Leite System.out und System.err in Log-Datei um
        try {
            FileOutputStream logFile = new FileOutputStream("logs/cinescape_system.log", true);
            PrintStream logPrintStream = new PrintStream(logFile, true);
            System.setOut(new LoggingPrintStream(logPrintStream));
            System.setErr(new LoggingPrintStream(logPrintStream));
        } catch (IOException e) {
            System.err.println("Fehler beim Umleiten von stdout/stderr: " + e.getMessage());
        }


        launch();
    }

}






