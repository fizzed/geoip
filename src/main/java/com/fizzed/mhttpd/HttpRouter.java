package com.fizzed.mhttpd;

import io.netty.handler.codec.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;

public class HttpRouter {

    private final List<HttpRoute> routes;

    public HttpRouter() {
        this.routes = new ArrayList<>();
    }

    public HttpRouter GET(String path, HttpHandler handler) {
        return this.add("GET", path, handler);
    }

    public HttpRouter POST(String path, HttpHandler handler) {
        return this.add("POST", path, handler);
    }

    public HttpRouter add(String method, String path, HttpHandler handler) {
        this.routes.add(new HttpRoute(HttpMethod.valueOf(method), path, handler));
        return this;
    }

    public HttpRoute resolve(final HttpMethod method, final String path) {
        for (final HttpRoute route : routes) {
            if (route.matches(method, path)) {
                return route;
            }
        }
        return null;
    }

}