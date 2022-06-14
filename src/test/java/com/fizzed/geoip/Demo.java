package com.fizzed.geoip;

import java.util.ArrayList;
import java.util.List;

public class Demo {

    static public void main(String[] args) throws Exception {
        List<String> arguments = new ArrayList<>();

        //arguments.add("--help");
        arguments.add("--static-data-file=data/GeoLite2-City.mmdb");
        arguments.add("--edition-id=GeoLite2-City");
//        arguments.add("--edition-id=GeoLite2-Country");
//        arguments.add("--license-key=invalid");
//        arguments.add("--port=9400");
//        arguments.add("--download-every-interval=6h");
//        arguments.add("--developer=true");

        GeoipServer.main(arguments.toArray(new String[]{}));
    }

}