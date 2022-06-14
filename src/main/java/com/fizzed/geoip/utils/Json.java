package com.fizzed.geoip.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Json {

    static public final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

}