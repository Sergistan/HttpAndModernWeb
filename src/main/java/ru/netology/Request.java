package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Request {

    private String method;
    private String path;
    private List<NameValuePair> queryParams;
    private Map<String, String> headers;
    private BufferedReader body;

    private HashMap<List<NameValuePair>, List<NameValuePair>> postParams;

    public Request(String method,
                   String path,
                   List<NameValuePair> queryParams,
                   Map<String, String> headers,
                   BufferedReader body,
                   HashMap<List<NameValuePair>, List<NameValuePair>> postParams) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = body;
        this.postParams = postParams;
    }

    public HashMap<List<NameValuePair>, List<NameValuePair>> getPostParams() {
        return postParams;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public BufferedReader getBody() {
        return body;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(method, request.method) &&
                Objects.equals(headers, request.headers) &&
                Objects.equals(body, request.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, headers, body);
    }

    public static Request parseRequest(BufferedReader in) {
        final String requestLine;
        try {
            requestLine = in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            throw new RuntimeException("Request line should be contains 3 elements");
        }

        String method = parts[0];
        String url = parts[1];

        HashMap<List<NameValuePair>, List<NameValuePair>> postParams = null;

        if (!Objects.equals(method, "GET"))
            postParams = getPostParam(in);

        URI uri;

        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return new Request(method, uri.getPath(), getQueryParams(url), null, in, postParams);

        //TODO: не хватает заголовков и некорректное тело запроса
    }



    private static List<NameValuePair> getQueryParams(String url) {
        try {
            return URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static HashMap<List<NameValuePair>, List<NameValuePair>> getPostParam(BufferedReader in) {
        HashMap<List<NameValuePair>, List<NameValuePair>> postBodyMap = new HashMap<>();
        String request = in.lines().collect(Collectors.joining("\n"));
        int startRawBody = request.indexOf("\n\n");
        String rawBody = request.substring(startRawBody);
        String trimRawBody = rawBody.trim();
        String[] split1 = trimRawBody.split("&");

        for (String s : split1) {
            String[] split2 = s.split("=");
            List<NameValuePair> parseKey = URLEncodedUtils.parse(split2[0], Charset.defaultCharset());
            List<NameValuePair> parseValue = null;
            if (split2.length > 1)
                parseValue = URLEncodedUtils.parse(split2[1], Charset.defaultCharset());
            if (postBodyMap.containsKey(parseKey)) {
                List<NameValuePair> existList = postBodyMap.get(parseKey);
                existList.add(parseValue.get(0));
            } else {
                postBodyMap.put(parseKey, parseValue);
            }
        }
        return postBodyMap;
    }
}
