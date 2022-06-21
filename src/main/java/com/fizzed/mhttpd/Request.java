package com.fizzed.mhttpd;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Request {

    private final HttpRequest request;
    private final String localAddress;
    private final String remoteAddress;
    private final QueryStringDecoder queryStringDecoder;

    public Request(String localAddress, String remoteAddress, HttpRequest request) {
        this.request = request;
        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
        this.queryStringDecoder = new QueryStringDecoder(this.getUri());
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public String getMethod() {
        return this.request.method().toString();
    }

    public String getUri() {
        return this.request.uri();
    }

    public String getPath() {
        return this.queryStringDecoder.path();
    }

    public Map<String, List<String>> getQueryParameters() {
        return this.queryStringDecoder.parameters();
    }

    public String getHeader(String name) {
        return this.request.headers().get(name);
    }

    public String getBodyAsString() {
        if (request instanceof FullHttpRequest) {
            return ((FullHttpRequest)request).content().toString(StandardCharsets.UTF_8);
        }
        return null;
    }

    public byte[] getBodyAsBytes() {
        if (request instanceof FullHttpRequest) {
            return ((FullHttpRequest)request).content().array();
        }
        return null;
    }

}