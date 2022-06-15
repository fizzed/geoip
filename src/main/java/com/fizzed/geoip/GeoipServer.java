package com.fizzed.geoip;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fizzed.crux.util.TimeDuration;
import com.fizzed.geoip.models.IpLocation;
import com.fizzed.mhttpd.HttpRouter;
import com.fizzed.mhttpd.MicroHttpd;
import com.fizzed.geoip.mmdb.Maxmind;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.fizzed.geoip.utils.Json.OBJECT_MAPPER;
import static java.util.Optional.ofNullable;

public class GeoipServer {

    static public void main(String[] args) throws Exception {
        final Logger log = LoggerFactory.getLogger(GeoipServer.class);

        ArgumentParser parser = ArgumentParsers.newFor("geoip-server").build()
            .description("Microservice for Maxmind ip lookups");
        parser.addArgument("--static-data-file")
            .dest("staticDataFile")
            .type(String.class)
            .help("Static maxmind database (disabled automatic downloading)");
        parser.addArgument("--try-all-editions")
            .dest("tryAllEditions")
            .type(Boolean.class)
            .setDefault(Boolean.FALSE)
            .help("ONLY for testing purposes");
        parser.addArgument("--edition-id")
            .dest("editionId")
            .type(String.class)
            .setDefault("GeoLite2-Country")
            .help("Edition id of maxmind database (e.g. GeoLite2-Country)");
        parser.addArgument("--license-key")
            .dest("licenseKey")
            .type(String.class)
            .help("Your maxmind license key");
        parser.addArgument("--port")
            .dest("port")
            .type(Integer.class)
            .setDefault(18888)
            .help("Port to bind server to");
        parser.addArgument("--download-every-interval")
            .dest("downloadEveryInterval")
            .type(String.class)
            .setDefault("1h")
            .help("Time duration between tries to download fresh copy of Maxmind database");
        parser.addArgument("--developer")
            .dest("developer")
            .type(Boolean.class)
            .help("Developer mode (w/ default stubbed locations OR you can also provide them)");
        parser.addArgument("--stubbed-locations-file")
            .dest("stubbedLocationsFile")
            .type(String.class)
            .help("File containing stubbed location results");

        Boolean developer = null;
        Boolean tryAllEditions = null;
        Path staticDataFile = null;
        String editionId = null;
        String licenseKey = null;
        Integer port = null;
        TimeDuration downloadEveryInterval = null;
        Path stubbedLocationsFile = null;
        try {
            Namespace res = parser.parseArgs(args);
            tryAllEditions = res.getBoolean("tryAllEditions");
            staticDataFile = ofNullable(res.getString("staticDataFile"))
                .map(v -> Paths.get(v))
                .orElse(null);
            editionId = res.getString("editionId");
            licenseKey = res.getString("licenseKey");
            port = res.getInt("port");
            downloadEveryInterval = TimeDuration.parse(res.get("downloadEveryInterval"));
            developer = res.getBoolean("developer");
            stubbedLocationsFile = ofNullable(res.getString("stubbedLocationsFile"))
                .map(v -> Paths.get(v))
                .orElse(null);
        }
        catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        // were there stubbed locations we need to try to parse?
        List<IpLocation> stubbedLocations = null;
        if (stubbedLocationsFile != null) {
            try {
                stubbedLocations = OBJECT_MAPPER.readValue(stubbedLocationsFile.toFile(), new TypeReference<List<IpLocation>>(){});
            }
            catch (Exception e) {
                log.error("Unable to parse stubbed locations", e);
                System.exit(1);
            }
        }


        final Maxmind maxmind = new Maxmind()
            .setDeveloper(developer)
            .setStubbedLocations(stubbedLocations)
            .setTryAllEditions(tryAllEditions)
            .setStaticDataFile(staticDataFile)
            .setEditionId(editionId)
            .setLicenseKey(licenseKey)
            .setDownloadEveryInterval(downloadEveryInterval)
            .start();

        final GeoipController controller = new GeoipController(maxmind);

        final HttpRouter router = new HttpRouter()
            .GET("/api/v1/management/health", controller::api_v1_health_get)
            .GET("/api/v1/ips/.*", controller::api_v1_ips_get)
            .GET("/", controller::dashboard);

        log.info("Httpd starting...");
        log.info(" port: {}", port);

        final MicroHttpd httpServer = new MicroHttpd()
            .setPort(port)
            .setRouter(router)
            .start();

        log.info("Open your web browser and navigate to http://localhost:{}", httpServer.getPort());

        httpServer.join();
    }

}