package com.fizzed.mhttpd;

import io.netty.handler.codec.http.HttpMethod;

import java.util.regex.Pattern;

public class HttpRoute {

    private final HttpMethod method;
    private final String path;
    private final Pattern pattern;
    private final HttpHandler handler;

    public HttpRoute(final HttpMethod method, final String path, final HttpHandler handler) {
        this.method = method;
        this.path = path;
        this.pattern = Pattern.compile(path);
        this.handler = handler;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public HttpHandler getHandler() {
        return handler;
    }

    public boolean matches(final HttpMethod method, final String path) {
        return this.method == method && this.pattern.matcher(path).matches();
    }

}