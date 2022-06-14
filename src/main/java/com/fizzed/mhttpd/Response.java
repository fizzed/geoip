package com.fizzed.mhttpd;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class Response {

    private int statusCode;
    private Map<String,String> headers;
    private byte[] body;

    public Response() {
        this.statusCode = 200;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Response setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Response setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public Response setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public Response setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    public Response setJsonError(String message) {
        this.body = new JSONObject()
            .put("error", new JSONObject()
                .put("message", message)
            )
            .toString(2)
            .getBytes(StandardCharsets.UTF_8);
        this.setHeader("Content-Type", "application/json");
        return this;
    }

    // helpers

    public Response setHeader(String name, String value) {
        if (this.headers == null) {
            this.headers = new LinkedHashMap<>();
        }
        this.headers.put(name, value);
        return this;
    }

}