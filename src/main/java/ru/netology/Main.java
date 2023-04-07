package ru.netology;

import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();

        server.addHandler("GET", "/start", (request, responseStream) -> {

            final var filePath = Path.of(".", "public", "/index.html");
            final var mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);

            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
            System.out.println(request.getQueryParams());
        });

        server.addHandler("POST", "/default-get.html", (request, responseStream) -> {
            final var filePath = Path.of(".", "public", "/default-get.html");
            final var mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);

            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\n\n" + request.getPostParams()

            ).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();
            System.out.println(request.getPostParams());
        });

        server.listenToServer();
    }
}
