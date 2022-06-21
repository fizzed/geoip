package com.fizzed.geoip;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class GeoipConfigCliLoaderTest {

    @Test
    void load() throws Exception {
        GeoipConfig config;
        GeoipConfigCliLoader loader;

        config = new GeoipConfig();
        loader = new GeoipConfigCliLoader(new String[] { "--port", "80", "--developer", "true" });

        loader.load(config);

        assertThat(config.getPort(), is(80));
        assertThat(config.getDeveloper(), is(true));
    }

}