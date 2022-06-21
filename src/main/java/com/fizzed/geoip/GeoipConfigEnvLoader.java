package com.fizzed.geoip;

import com.fizzed.crux.util.MoreObjects;

import static com.fizzed.crux.util.MoreObjects.isBlank;

public class GeoipConfigEnvLoader {

    public void load(GeoipConfig config) throws Exception {
        final String editionId = System.getenv("GEOIP_EDITION_ID");
        if (!isBlank(editionId)) {
            config.setEditionId(editionId);
        }

        final String licenseKey = System.getenv("GEOIP_LICENSE_KEY");
        if (!isBlank(licenseKey)) {
            config.setLicenseKey(licenseKey);
        }

        final String portStr = System.getenv("GEOIP_PORT");
        if (!isBlank(portStr)) {
            config.setPort(Integer.valueOf(portStr));
        }
    }

}