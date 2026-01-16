package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Einfacher eingebetteter Webserver zum Servieren von HTML-Inhalten
 * Wird verwendet, um YouTube-Trailer-HTML in der WebView anzuzeigen
 * Nutzt Java internen HttpServer (keine externen Abhängigkeiten)
 */
public class LocalWebServer {
    private static final Logger LOGGER = Logger.getLogger(LocalWebServer.class.getName());
    private static final int PORT = 8765;
    private static HttpServer server;
    private static final Map<String, String> htmlContent = new HashMap<>();

    /**
     * Startet den lokalen Webserver
     */
    public static synchronized void start() {
        if (server != null) {
            LOGGER.log(Level.INFO, "Webserver läuft bereits auf Port " + PORT);
            return;
        }

        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Registriere HTTP Handler
            server.createContext("/", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    String path = exchange.getRequestURI().getPath();
                    if (path.startsWith("/")) {
                        path = path.substring(1);
                    }
                    if (path.isEmpty()) {
                        path = "index";
                    }

                    String content = htmlContent.get(path);

                    if (content != null) {
                        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(200, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                        System.out.println("✓ Serviere: " + path);
                    } else {
                        String notFound = "<html><body><h1>404 - Nicht gefunden</h1></body></html>";
                        byte[] bytes = notFound.getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        exchange.sendResponseHeaders(404, bytes.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(bytes);
                        }
                        System.out.println("⚠ Nicht gefunden: " + path);
                    }
                }
            });

            server.setExecutor(null);
            server.start();

            LOGGER.log(Level.INFO, "✓ Lokaler Webserver gestartet auf http://localhost:" + PORT);
            System.out.println("✓ Lokaler Webserver gestartet auf http://localhost:" + PORT);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Starten des Webservers: " + e.getMessage(), e);
            System.err.println("Fehler beim Starten des Webservers: " + e.getMessage());
        }
    }

    /**
     * Stoppt den lokalen Webserver
     */
    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null; // Freigeben, damit ein erneuter Start möglich ist
            LOGGER.log(Level.INFO, "✓ Lokaler Webserver gestoppt");
            System.out.println("✓ Lokaler Webserver gestoppt");
        }
    }

    /**
     * Speichert HTML-Inhalt für eine bestimmte Route
     */
    public static void setContent(String route, String htmlContent) {
        LocalWebServer.htmlContent.put(route, htmlContent);
        LOGGER.log(Level.INFO, "HTML-Inhalt registriert für Route: " + route);
    }

    /**
     * Gibt die URL für eine Route zurück
     */
    public static String getUrl(String route) {
        return "http://localhost:" + PORT + "/" + route;
    }
}
