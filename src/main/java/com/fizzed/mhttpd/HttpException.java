package com.fizzed.mhttpd;

import java.io.IOException;

public class HttpException extends IOException {

    private final int statusCode;

    public HttpException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

}