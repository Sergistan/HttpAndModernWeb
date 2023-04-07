package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server {
    private static final Map<String, Handler> handlersMap = new ConcurrentHashMap<>();
    private ServerSocket serverSocket;
    private Socket socket;

    ExecutorService executorService = Executors.newFixedThreadPool(64);

    final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js", "/default-get.html");


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

                    String requestMethod = parts[0];
                    String requestUrl = parts[1];

                    String[] split = requestUrl.split("\\?");
                    String requestUrlIgnoreQuery = split[0];

                    Handler handler = searchHandler(requestMethod, requestUrlIgnoreQuery);

                    Request request = null;

                    if (requestMethod.equals("GET") && requestUrl.startsWith("/start")) {
                        request = new Request(requestMethod, requestUrl, getQueryParams(requestUrl), new HashMap<>(), null);
                    }

                    if (requestMethod.equals("POST") && requestUrl.startsWith("/default-get")) {
                        request = new Request(requestMethod, new HashMap<>(), in, getPostParam(in));
                    }

                    if (handler != null) {
                        handler.handle(request, out);
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

    public List<NameValuePair> getQueryParams(String url) {
        try {
            return URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, List<String>> getPostParam(BufferedReader in) {
        HashMap<String, List<String>> postBodyMap = new HashMap<>();
        String line = in.lines().collect(Collectors.joining("\n"));
        int i = line.indexOf("\n\n");
        String rawBody = line.substring(i);
        String trim = rawBody.trim();
        String[] split1 = trim.split("&");
        for (String s : split1) {
            List<String> list = new ArrayList<>();
            String[] split2 = s.split("=");
            if (postBodyMap.containsKey(split2[0])) {
                List<String> stringList = postBodyMap.get(split2[0]);
                stringList.add(split2[1]);
            } else {
                list.add(split2[1]);
                postBodyMap.put(split2[0], list);
            }
        }
        return postBodyMap;
    }

}




