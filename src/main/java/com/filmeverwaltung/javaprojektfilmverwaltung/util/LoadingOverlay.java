package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Einfacher, wiederverwendbarer Lade-Dialog.
 */
public class LoadingOverlay
{
    private Stage stage;
    private Label messageLabel;

    /**
     * Zeigt den Lade-Dialog mit einer Nachricht an.
     *
     * @param owner   optionales Owner-Fenster (kann null sein)
     * @param message Nachricht, die angezeigt werden soll
     */
    public void show(Window owner, String message)
    {
        Platform.runLater(() ->
        {
            if (stage == null)
            {
                messageLabel = new Label(message);
                ProgressIndicator indicator = new ProgressIndicator();
                VBox box = new VBox(12, indicator, messageLabel);
                box.setAlignment(javafx.geometry.Pos.CENTER);
                box.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 6; -fx-background-radius: 6;");

                stage = new Stage(StageStyle.UNDECORATED);
                stage.initModality(Modality.APPLICATION_MODAL);
                if (owner != null)
                {
                    stage.initOwner(owner);
                }
                stage.setScene(new Scene(box));
            }
            else if (messageLabel != null)
            {
                messageLabel.setText(message);
            }

            stage.centerOnScreen();
            stage.show();
        });
    }

    /**
     * Aktualisiert die angezeigte Nachricht.
     */
    public void updateMessage(String message)
    {
        Platform.runLater(() ->
        {
            if (messageLabel != null)
            {
                messageLabel.setText(message);
            }
        });
    }

    /**
     * Verbirgt den Lade-Dialog, falls angezeigt.
     */
    public void hide()
    {
        Platform.runLater(() ->
        {
            if (stage != null)
            {
                stage.hide();
            }
        });
    }
}

