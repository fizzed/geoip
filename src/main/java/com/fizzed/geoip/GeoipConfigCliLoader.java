package com.fizzed.geoip;

import com.fizzed.crux.util.TimeDuration;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Optional.ofNullable;

public class GeoipConfigCliLoader {

    final private ArgumentParser parser;
    final private String[] args;

    public GeoipConfigCliLoader(String[] args) {
        this.parser = ArgumentParsers.newFor("geoip-server").build()
            .description("Microservice for Maxmind ip lookups");

        this.args = args;

        this.parser.addArgument("--static-data-file")
            .dest("staticDataFile")
            .type(String.class)
            .help("Static maxmind database (disabled automatic downloading)");
        this.parser.addArgument("--edition-id")
            .dest("editionId")
            .type(String.class)
            //.setDefault("GeoLite2-Country")
            .help("Edition id of maxmind database. Defaults to GeoLite2-Country.");
        this.parser.addArgument("--license-key")
            .dest("licenseKey")
            .type(String.class)
            .help("Your maxmind license key");
        this.parser.addArgument("--port")
            .dest("port")
            .type(String.class)
            //.setDefault("18888")
            .help("Port to bind server to. Defaults to 18888.");
        this.parser.addArgument("--download-every-interval")
            .dest("downloadEveryInterval")
            .type(String.class)
            //.setDefault("1h")
            .help("Time duration between tries to download fresh copy of Maxmind database. Defaults to 1h.");
        this.parser.addArgument("--developer")
            .dest("developer")
            .type(Boolean.class)
            .help("Developer mode (w/ default stubbed locations OR you can also provide them). Defaults to false.");
        this.parser.addArgument("--stubbed-locations-file")
            .dest("stubbedLocationsFile")
            .type(String.class)
            .help("File containing stubbed location results");
    }

    public ArgumentParser getParser() {
        return parser;
    }

    public void load(GeoipConfig config) throws Exception {
        final Namespace res = this.parser.parseArgs(args);

        ofNullable(res.getBoolean("developer"))
            .ifPresent(v -> config.setDeveloper(v));

        ofNullable(res.getString("port"))
            .map(v -> Integer.valueOf(v))
            .ifPresent(v -> config.setPort(v));

        ofNullable(res.getString("editionId"))
            .ifPresent(v -> config.setEditionId(v));

        ofNullable(res.getString("licenseKey"))
            .ifPresent(v -> config.setLicenseKey(v));

        ofNullable(res.getString("staticDataFile"))
            .map(v -> Paths.get(v))
            .ifPresent(v -> config.setStaticDataFile(v));

        ofNullable(res.getString("downloadEveryInterval"))
            .map(v -> TimeDuration.parse(v))
            .ifPresent(v -> config.setDownloadEveryInterval(v));

        ofNullable(res.getString("stubbedLocationsFile"))
            .map(v -> Paths.get(v))
            .ifPresent(v -> config.setStubbedLocationsFile(v));
    }

}