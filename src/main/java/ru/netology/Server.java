package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Map<String, Handler> handlersMap = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;
    private Socket socket;

    ExecutorService executorService = Executors.newFixedThreadPool(64);

    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");


    private static int parsePort() {
        int port = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/settings.txt"))) {
            String str = br.readLine();
            String[] split = str.split("=");
            port = Integer.parseInt(split[1].trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(Server.parsePort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String url, Handler handler) {
        handlersMap.put(method + " " + url, handler);
    }

    public Handler searchHandler(String method, String url) {
        for (var entry : handlersMap.entrySet()) {
            if (entry.getKey().equals(method + " " + url)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void listenToServer() {
        executorService.execute(() -> {
            while (true) {
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try (
                        final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        final var out = new BufferedOutputStream(socket.getOutputStream());
                ) {

                    final var requestLine = in.readLine();

                    final var parts = requestLine.split(" ");

                    if (parts.length != 3) {
                        continue;
                    }

                    if (searchHandler("GET", "/start") != null){
                        Handler handler = searchHandler("GET", "/start");
                        Request request = new Request("GET", new HashMap<>(), null);
                        handler.handle(request, new BufferedOutputStream(socket.getOutputStream()));
                    }

                    if (searchHandler("POST", "/start") != null){
                        Handler handler = searchHandler("POST", "/start");
                        Request request = new Request("POST", new HashMap<>(), "Hello!");
                        handler.handle(request, new BufferedOutputStream(socket.getOutputStream()));
                    }

                    final var path = parts[1];
                    if (!validPaths.contains(path)) {
                        out.write((
                                "HTTP/1.1 404 Not Found\r\n" +
                                        "Content-Length: 0\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        out.flush();
                        continue;
                    }

                    final var filePath = Path.of(".", "public", path);
                    final var mimeType = Files.probeContentType(filePath);

                    if (path.equals("/classic.html")) {
                        final var template = Files.readString(filePath);
                        final var content = template.replace(
                                "{time}",
                                LocalDateTime.now().toString()
                        ).getBytes();
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + content.length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        out.write(content);
                        out.flush();
                        continue;
                    }

                    final var length = Files.size(filePath);
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, out);
                    out.flush();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
