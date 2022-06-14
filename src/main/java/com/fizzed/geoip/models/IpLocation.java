package com.fizzed.geoip.models;

public class IpLocation {

    private String ip;
    private String countryCode;
    private String countryName;
    private String regionCode;
    private String regionName;
    private String cityName;
    private String postalCode;
    private Double lat;
    private Double lng;
    private String tz;

    public String getIp() {
        return ip;
    }

    public IpLocation setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public IpLocation setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    public String getCountryName() {
        return countryName;
    }

    public IpLocation setCountryName(String countryName) {
        this.countryName = countryName;
        return this;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public IpLocation setRegionCode(String regionCode) {
        this.regionCode = regionCode;
        return this;
    }

    public String getRegionName() {
        return regionName;
    }

    public IpLocation setRegionName(String regionName) {
        this.regionName = regionName;
        return this;
    }

    public String getCityName() {
        return cityName;
    }

    public IpLocation setCityName(String cityName) {
        this.cityName = cityName;
        return this;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public IpLocation setPostalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public Double getLat() {
        return lat;
    }

    public IpLocation setLat(Double lat) {
        this.lat = lat;
        return this;
    }

    public Double getLng() {
        return lng;
    }

    public IpLocation setLng(Double lng) {
        this.lng = lng;
        return this;
    }

    public String getTz() {
        return tz;
    }

    public IpLocation setTz(String tz) {
        this.tz = tz;
        return this;
    }

}