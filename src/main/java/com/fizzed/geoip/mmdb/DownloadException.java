package com.fizzed.geoip.mmdb;

import java.io.IOException;

public class DownloadException extends IOException {

    public DownloadException(String message) {
        super(message);
    }

    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }

}