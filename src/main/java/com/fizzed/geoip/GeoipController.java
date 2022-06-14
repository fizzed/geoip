package com.fizzed.geoip;

import com.fizzed.geoip.mmdb.Maxmind;
import com.fizzed.geoip.models.IpLocation;
import com.fizzed.mhttpd.Request;
import com.fizzed.mhttpd.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.fizzed.geoip.utils.Json.OBJECT_MAPPER;

public class GeoipController {
    static private final Logger log = LoggerFactory.getLogger(GeoipController.class);

    private final Maxmind maxmind;

    public GeoipController(Maxmind maxmind) {
        this.maxmind = maxmind;
    }

    public Response dashboard(Request request) throws IOException {
        return new Response()
            .setBody("" +
                "Welcome to GeoIP<br/>" +
                "<br/>" +
                "Strategy: " + maxmind.getStrategy() + "<br/>" +
                "Edition ID: " + maxmind.getEditionId() + "<br/>" +
                "<br/>" +
                "Database Etag: " + maxmind.getEtag() + "<br/>" +
                "Database Edition ID: " + maxmind.getType() + "<br/>" +
                "Database Built: " + maxmind.getBuiltAt() + "<br/>" +
                "Database File: " + maxmind.getDataFile() + "<br/>" +
                "Database Refreshed: " + maxmind.getDataUpdatedAt() + "<br/>" +
                "<br/>" +
                "<a href='/api/v1/management/health'>/api/v1/management/health</a><br/>" +
                "<a href='/api/v1/ips/127.0.0.1'>/api/v1/ips/127.0.0.1</a><br/>" +
                "<a href='/api/v1/ips/24.192.251.1'>/api/v1/ips/24.192.251.1</a><br/>" +
                "<a href='/api/v1/ips/2600:1702:1e30:b870:a125:5be2:12b1:9c96'>/api/v1/ips/2600:1702:1e30:b870:a125:5be2:12b1:9c96</a><br/>" +
                "<a href='/api/v1/ips/212.82.92.200'>/api/v1/ips/212.82.92.200</a>");
    }

    public Response api_v1_health_get(Request request) throws IOException {
        /*
        api/v1/management/health
        {
            "name": "Cogs",
            "state": "OK",
            "message": "Awesome, we are good!",
            "created_at": "2022-06-14T02:12:04.594+0000"
        }
        */
        String state = "OK";
        int statusCode = 250;
        String message = "Awesome, we are good!";

        if (maxmind.getFailureMessage() != null) {
            state = "DOWN";
            statusCode = 550;
            message = maxmind.getFailureMessage();
        }

        final byte[] body = new JSONObject()
            .put("name", "GeoIP")
            .put("state", state)
            .put("message", message)
            .toString(2)
            .getBytes(StandardCharsets.UTF_8);

        return new Response()
            .setHeader("Content-Type", "application/json")
            .setStatusCode(statusCode)
            .setBody(body);
    }

    public Response api_v1_ips_get(Request request) throws IOException {
        // extract out the ip to lookup
        final int lastSlashPos = request.getPath().lastIndexOf('/');
        final String ip = request.getPath().substring(lastSlashPos+1);

        IpLocation ipLocation;
        try {
            ipLocation = this.maxmind.lookup(ip);
        }
        catch (Exception e) {
            log.error("Failure during maxmind lookup", e);
            return new Response()
                .setStatusCode(400)
                .setJsonError(e.getMessage());
        }

        if (ipLocation == null) {
            return new Response()
                .setStatusCode(400)
                .setJsonError("Ip address " + ip + " not found");
        }

        return new Response()
            .setHeader("Content-Type", "application/json")
            .setBody(OBJECT_MAPPER.writeValueAsBytes(ipLocation));
    }

}