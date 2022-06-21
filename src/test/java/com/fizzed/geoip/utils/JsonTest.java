package com.fizzed.geoip.utils;

import com.fizzed.geoip.models.IpLocation;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JsonTest {

    @Test
    public void serialize() throws Exception {
        IpLocation loc1 = new IpLocation()
            .setIp("127.0.0.1");

        final String json = Json.OBJECT_MAPPER.writeValueAsString(loc1);
        final IpLocation loc2 = Json.OBJECT_MAPPER.readValue(json, IpLocation.class);

        assertThat(loc2.getIp(), is("127.0.0.1"));
    }

}