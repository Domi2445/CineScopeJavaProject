package com.filmeverwaltung.javaprojektfilmverwaltung.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpUtil
{
    private static final HttpClient client = HttpClient.newHttpClient();

    public HttpUtil(){}

    /**
     * Send a GET request to the specified URL.
     *
     * @param url The URL to send the GET request to.
     * @return The response body as a String.
     * @throws IOException          If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     */
    public static String get(String url) throws IOException, InterruptedException, IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * Send a POST request to the specified URL with the given body and content type.
     *
     * @param url         The URL to send the POST request to.
     * @param body        The body of the POST request.
     * @param contentType The content type of the request (e.g., "application/json").
     * @return The response body as a String.
     * @throws IOException          If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     */
    public static String post(String url, String body, String contentType) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }
}
