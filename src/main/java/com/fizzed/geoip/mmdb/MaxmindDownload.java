package com.fizzed.geoip.mmdb;

import java.nio.file.Path;

public class MaxmindDownload {

    final private Path file;
    final private String etag;

    public MaxmindDownload(Path file, String etag) {
        this.file = file;
        this.etag = etag;
    }

    public Path getFile() {
        return this.file;
    }

    public String getEtag() {
        return this.etag;
    }

}