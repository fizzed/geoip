package com.fizzed.geoip;

import com.fizzed.crux.util.TimeDuration;

import java.nio.file.Path;

public class GeoipConfig {

    private Boolean developer;
    private Path staticDataFile;
    private String editionId;
    private String licenseKey;
    private Integer port;
    private TimeDuration downloadEveryInterval;
    private Path stubbedLocationsFile;

    public GeoipConfig() {
        // defaults
        this.developer = false;
        this.editionId = "GeoLite2-Country";
        this.port = 18888;
        this.downloadEveryInterval = TimeDuration.hours(1);
    }

    public Boolean getDeveloper() {
        return developer;
    }

    public GeoipConfig setDeveloper(Boolean developer) {
        this.developer = developer;
        return this;
    }

    public Path getStaticDataFile() {
        return staticDataFile;
    }

    public GeoipConfig setStaticDataFile(Path staticDataFile) {
        this.staticDataFile = staticDataFile;
        return this;
    }

    public String getEditionId() {
        return editionId;
    }

    public GeoipConfig setEditionId(String editionId) {
        this.editionId = editionId;
        return this;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public GeoipConfig setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public GeoipConfig setPort(Integer port) {
        this.port = port;
        return this;
    }

    public TimeDuration getDownloadEveryInterval() {
        return downloadEveryInterval;
    }

    public GeoipConfig setDownloadEveryInterval(TimeDuration downloadEveryInterval) {
        this.downloadEveryInterval = downloadEveryInterval;
        return this;
    }

    public Path getStubbedLocationsFile() {
        return stubbedLocationsFile;
    }

    public GeoipConfig setStubbedLocationsFile(Path stubbedLocationsFile) {
        this.stubbedLocationsFile = stubbedLocationsFile;
        return this;
    }

}