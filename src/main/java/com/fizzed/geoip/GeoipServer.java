package com.fizzed.geoip;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fizzed.geoip.mmdb.Maxmind;
import com.fizzed.geoip.models.IpLocation;
import com.fizzed.mhttpd.HttpRouter;
import com.fizzed.mhttpd.MicroHttpd;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.fizzed.geoip.utils.Json.OBJECT_MAPPER;

public class GeoipServer {

    static public void main(String[] args) throws Exception {
        final Logger log = LoggerFactory.getLogger(GeoipServer.class);

        final GeoipConfig config = new GeoipConfig();
        final GeoipConfigEnvLoader envConfigLoader = new GeoipConfigEnvLoader();
        final GeoipConfigCliLoader cliConfigLoader = new GeoipConfigCliLoader(args);

        try {
            envConfigLoader.load(config);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            cliConfigLoader.load(config);
        }
        catch (ArgumentParserException e) {
            cliConfigLoader.getParser().handleError(e);
            System.exit(1);
        }

        // were there stubbed locations we need to try to parse?
        List<IpLocation> stubbedLocations = null;
        if (config.getStubbedLocationsFile() != null) {
            try {
                stubbedLocations = OBJECT_MAPPER.readValue(config.getStubbedLocationsFile().toFile(), new TypeReference<List<IpLocation>>(){});
            }
            catch (Exception e) {
                log.error("Unable to parse stubbed locations", e);
                System.exit(1);
            }
        }

        final Maxmind maxmind = new Maxmind()
            .setDeveloper(config.getDeveloper())
            .setStubbedLocations(stubbedLocations)
            .setStaticDataFile(config.getStaticDataFile())
            .setEditionId(config.getEditionId())
            .setLicenseKey(config.getLicenseKey())
            .setDownloadEveryInterval(config.getDownloadEveryInterval())
            .start();

        final GeoipController controller = new GeoipController(maxmind);

        final HttpRouter router = new HttpRouter()
            .GET("/api/v1/management/health", controller::api_v1_health_get)
            .GET("/api/v1/ips/me", controller::api_v1_ips_me)
            .GET("/api/v1/ips/.*", controller::api_v1_ips_get)
            .GET("/", controller::dashboard);

        log.info("Httpd starting...");
        log.info(" port: {}", config.getPort());

        final MicroHttpd httpServer = new MicroHttpd()
            .setPort(config.getPort())
            .setRouter(router)
            .start();

        log.info("Open your web browser and navigate to http://localhost:{}", httpServer.getPort());

        httpServer.join();
    }

}