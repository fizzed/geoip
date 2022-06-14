package com.fizzed.geoip.mmdb;

import com.fizzed.crux.uri.MutableUri;
import com.fizzed.crux.util.TimeDuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Optional.ofNullable;

public class MaxmindDownloader extends Thread {
    static private final Logger log = LoggerFactory.getLogger(MaxmindDownloader.class);

    final private TimeDuration initialDelay;
    final private TimeDuration executeEvery;
    final private Maxmind maxmind;
    private volatile boolean stopRequested;

    public MaxmindDownloader(
            Maxmind maxmind,
            TimeDuration initialDelay,
            TimeDuration executeEvery) {

        this.setName("MaxmindDownloader");
        this.setDaemon(true);
        this.stopRequested = false;
        this.initialDelay = initialDelay;
        this.executeEvery = executeEvery;
        this.maxmind = maxmind;
    }

    public void shutdown() {
        this.stopRequested = true;
        this.interrupt();
    }

    @Override
    public void run() {
        if (this.initialDelay != null) {
            try {
                Thread.sleep(this.initialDelay.asMillis());
            }
            catch (InterruptedException e) {
                // do nothing
            }
        }

        while (true) {
            // exit if stopped
            if (this.stopRequested) {
                return;
            }

            // try to download the data
            try {
                this.downloadAndRotate();
            }
            catch (DownloadException e) {
                log.error("Permanent failure downloading maxmind data", e);
                maxmind.failed(e.getMessage());
            }
            catch (NotModifiedException e) {
                log.info("Database not modified");
            }
            catch (InterruptedException e) {
                // continue with next iteration
                continue;
            }
            catch (Exception e) {
                log.error("Unable to download maxmind data", e);
            }

            try {
                log.info("Will check for next download in {}...", this.executeEvery);
                Thread.sleep(this.executeEvery.asMillis());
            } catch (InterruptedException e) {
                // continue with next iteration
                continue;
            }
        }
    }

    public void downloadAndRotate() throws IOException, InterruptedException {
        log.info("Will download data file (if its not modified)...");

        final MaxmindDownload download = MaxmindDownloader.download(
            maxmind.getEditionId(),
            maxmind.getLicenseKey(),
            maxmind.getEtag());

        maxmind.rotate(download.getFile(), download.getEtag());
    }

    static public MaxmindDownload download(
            String editionId,
            String licenseKey,
            String etag) throws IOException, InterruptedException {

        // Database to download
        // https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&license_key=YOUR_LICENSE_KEY&suffix=tar.gz

        final URI url = new MutableUri("https://download.maxmind.com/app/geoip_download")
            .query("edition_id", editionId)
            .query("license_key", licenseKey)
            .query("suffix", "tar.gz")
            .toURI();

        log.info("Fetching {} (with etag {})...", url, etag);

        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(url)
            .header("If-None-Match", "\"" + ofNullable(etag).orElse("none") + "\"")
            .timeout(Duration.ofMillis(TimeDuration.seconds(10).asMillis()))
            .build();

        final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.of(10, SECONDS))
            .build();

        final Path tempFile = Files.createTempFile("maxmind-", ".tar.gz");

        try {
            final HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));

            /*System.out.println("Response was " + response.statusCode());
            response.headers().map().forEach((name, values) -> {
                values.forEach(value -> {
                    System.out.println(" " + name + " -> " + value);
                });
            });*/

            switch (response.statusCode()) {
                case 200:
                    // yes, awesome
                    break;
                case 304:
                    throw new NotModifiedException();
                case 404:
                    // either url is invalid or edition does not exist
                    throw new DownloadException("Download url not found (editionId " + editionId + " could possibly be invalid?)");
                case 401:
                    throw new DownloadException("License key is invalid");
                default:
                    throw new DownloadException("Unexpected http status code " + response.statusCode());
            }

            log.info("Downloaded new data to {}", tempFile);

            // we need the etag for the tarball
            final String newEtag = FileHelper.md5(tempFile);

            log.info("New data has etag {}", newEtag);

            // uncompress out the .mmdb file
            final Path newFile = FileHelper.untarball(tempFile);

            log.info("New data temporarily at {}", newFile);

            return new MaxmindDownload(newFile, newEtag);
        }
        finally {
            // make sure temp file is deleted
            Files.deleteIfExists(tempFile);
        }
    }

}