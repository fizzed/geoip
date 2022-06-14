package com.fizzed.geoip.mmdb;

import com.maxmind.db.MaxMindDbConstructor;
import com.maxmind.db.MaxMindDbParameter;
import com.maxmind.geoip2.record.*;

public class MaxmindResult {

    private final Country country;
    private final City city;
    private final Subdivision subdivision;
    private final Postal postal;
    private final Location location;

    @MaxMindDbConstructor
    public MaxmindResult(
            Location location,
            City city,
            Subdivision subdivision,
            Postal postal,
            Country country) {

        this.location = location;
        this.city = city;
        this.subdivision = subdivision;
        this.postal = postal;
        this.country = country;
    }

    public Subdivision getSubdivision() {
        return subdivision;
    }

    public Postal getPostal() {
        return postal;
    }

    public Location getLocation() {
        return location;
    }

    public City getCity() {
        return city;
    }

    public Country getCountry() {
        return this.country;
    }

}