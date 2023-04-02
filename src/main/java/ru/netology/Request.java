package ru.netology;

import java.util.Map;
import java.util.Objects;

public class Request {

    private String requestMethod;
    private Map<String, String> requestHeaders;
    private String requestBody;

    public Request(String requestMethod, Map<String, String> requestHeaders, String requestBody) {
        this.requestMethod = requestMethod;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(requestMethod, request.requestMethod) &&
                Objects.equals(requestHeaders, request.requestHeaders) &&
                Objects.equals(requestBody, request.requestBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestMethod, requestHeaders, requestBody);
    }
}