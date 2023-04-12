package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Map<String, Handler> handlersMap = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;
    private Socket socket;

    ExecutorService executorService = Executors.newFixedThreadPool(64);

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
                    Request request = Request.parseRequest(in);

                    Handler handler = searchHandler(request.getMethod(), request.getPath());

                    if (handler != null) {
                        handler.handle(request, out);
                    } else {
                        out.write((
                                "HTTP/1.1 404 Not Found\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}




