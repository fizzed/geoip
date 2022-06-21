package com.fizzed.geoip.mmdb;

import com.fizzed.geoip.models.IpLocation;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MaxmindTest {

    @Test
    public void lookupCountryEdition() throws Exception {
        final Maxmind maxmind = new Maxmind()
            .setStaticDataFile(Paths.get("data/GeoLite2-Country.mmdb"))
            .start();

        final IpLocation loc1 = maxmind.lookup("24.192.251.1");

        assertThat(loc1.getCountryCode(), is("US"));
        assertThat(loc1.getVersion(), is(4));

        final IpLocation loc2 = maxmind.lookup("2603:c020:400c:8e00:0000:0000:0000:00d2");

        assertThat(loc2.getCountryCode(), is("US"));
        assertThat(loc2.getVersion(), is(6));
    }

    @Test
    public void lookupCityEdition() throws Exception {
        final Maxmind maxmind = new Maxmind()
            .setStaticDataFile(Paths.get("data/GeoLite2-City.mmdb"))
            .start();

        final IpLocation loc1 = maxmind.lookup("24.192.251.1");

        assertThat(loc1.getCountryCode(), is("US"));
        assertThat(loc1.getVersion(), is(4));

        final IpLocation loc2 = maxmind.lookup("2603:c020:400c:8e00:0000:0000:0000:00d2");

        assertThat(loc2.getCountryCode(), is("US"));
        assertThat(loc2.getVersion(), is(6));
    }

}