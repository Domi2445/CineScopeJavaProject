package com.filmeverwaltung.javaprojektfilmverwaltung;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class Main extends Application
{
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/root.fxml"));
        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Filmverwaltung");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
