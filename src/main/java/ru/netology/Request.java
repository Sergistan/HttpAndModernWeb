package ru.netology;

import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.util.*;

public class Request {

    private String requestMethod;
    private String requestUrl;
    private List<NameValuePair> queryParams;
    private Map<String, String> requestHeaders;
    private BufferedReader requestBody;

    private HashMap <String, List<String>> postParams;

    public Request(String requestMethod, String requestUrl, List<NameValuePair> queryParams, Map<String, String> requestHeaders, BufferedReader requestBody) {
        this.requestMethod = requestMethod;
        this.requestUrl = requestUrl;
        this.queryParams = queryParams;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
    }

    public Request(String requestMethod, Map<String, String> requestHeaders, BufferedReader requestBody, HashMap <String, List<String>> postParams) {
        this.requestMethod = requestMethod;
        this.requestHeaders = requestHeaders;
        this.requestBody = requestBody;
        this.postParams = postParams;
    }

    public HashMap <String, List<String>> getPostParams() {
        return postParams;
    }

    public void setPostParams(HashMap <String, List<String>> postParams) {
        this.postParams = postParams;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
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

    public BufferedReader getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(BufferedReader requestBody) {
        this.requestBody = requestBody;
    }

    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<NameValuePair> queryParams) {
        this.queryParams = queryParams;
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
