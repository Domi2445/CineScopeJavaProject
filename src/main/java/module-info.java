module com.filmeverwaltung.javaprojektfilmverwaltung
{
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires com.google.gson;
    requires annotations;
    requires java.logging;

    opens com.filmeverwaltung.javaprojektfilmverwaltung to javafx.fxml;
    opens com.filmeverwaltung.javaprojektfilmverwaltung.controller to javafx.fxml;
    opens com.filmeverwaltung.javaprojektfilmverwaltung.model to com.google.gson, javafx.base;


    exports com.filmeverwaltung.javaprojektfilmverwaltung;
}