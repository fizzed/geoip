package com.fizzed.geoip.mmdb;

import com.fizzed.crux.util.TimeDuration;
import com.fizzed.geoip.models.IpLocation;
import com.maxmind.db.Metadata;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.*;
import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

public class Maxmind {
    static private final Logger log = LoggerFactory.getLogger(Maxmind.class);

    private Path dataDirectory;
    private String licenseKey;
    private String editionId;
    private boolean tryAllEditions;
    private Path staticDataFile;
    private Boolean developer;
    private List<IpLocation> stubbedLocations;
    private TimeDuration downloadInitialDelay;
    private TimeDuration downloadEveryInterval;

    // state
    private MaxmindStrategy strategy;
    private Path dataFile;
    private Path etagFile;
    private String etag;
    private String failureMessage;
    private Long failureAt;
    private MaxmindDownloader downloader;
    final private ReentrantReadWriteLock dataLock;
    final private ReentrantReadWriteLock.ReadLock dataReadLock;
    final private ReentrantReadWriteLock.WriteLock dataWriteLock;
    private DatabaseReader reader;
    private Metadata metadata;
    private MaxmindEdition edition;

    public Maxmind() {
        this.developer = false;
        this.downloadInitialDelay = TimeDuration.seconds(5);
        this.downloadEveryInterval = TimeDuration.hours(1);
        this.dataLock = new ReentrantReadWriteLock();
        this.dataReadLock = this.dataLock.readLock();
        this.dataWriteLock = this.dataLock.writeLock();
        final String _temp = System.getProperty("java.io.tmpdir");
        final Path tempDir = Paths.get(_temp);
        this.dataDirectory = tempDir.resolve("geoip-data");
    }

    public Boolean getDeveloper() {
        return developer;
    }

    public Maxmind setDeveloper(Boolean developer) {
        this.developer = developer;
        return this;
    }

    public List<IpLocation> getStubbedLocations() {
        return stubbedLocations;
    }

    public Maxmind setStubbedLocations(List<IpLocation> stubbedLocations) {
        this.stubbedLocations = stubbedLocations;
        return this;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Maxmind setDataDirectory(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        return this;
    }

    public Path getStaticDataFile() {
        return staticDataFile;
    }

    public Maxmind setStaticDataFile(Path staticDataFile) {
        this.staticDataFile = staticDataFile;

        return this;
    }

    public boolean isTryAllEditions() {
        return tryAllEditions;
    }

    public Maxmind setTryAllEditions(boolean tryAllEditions) {
        this.tryAllEditions = tryAllEditions;
        return this;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public Maxmind setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
        return this;
    }

    public String getEditionId() {
        return editionId;
    }

    public Maxmind setEditionId(String editionId) {
        this.editionId = editionId;
        return this;
    }

    public TimeDuration getDownloadEveryInterval() {
        return downloadEveryInterval;
    }

    public Maxmind setDownloadEveryInterval(TimeDuration downloadEveryInterval) {
        this.downloadEveryInterval = downloadEveryInterval;
        return this;
    }

    public String getEtag() {
        return this.etag;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public Long getFailureAt() {
        return failureAt;
    }

    public Path getDataFile() {
        return dataFile;
    }

    public Path getEtagFile() {
        return etagFile;
    }

    public MaxmindStrategy getStrategy() {
        return strategy;
    }

    public String getType() {
        return ofNullable(this.metadata)
            .map(v -> v.getDatabaseType())
            .orElse(null);
    }

    public Instant getBuiltAt() {
        return ofNullable(this.metadata)
            .map(v -> v.getBuildDate())
            .map(v -> v.toInstant())
            .orElse(null);
    }

    public Instant getDataUpdatedAt() {
        if (this.dataFile == null) {
            return null;
        }

        try {
            FileTime ft = Files.getLastModifiedTime(this.dataFile);
            return ft.toInstant();
        }
        catch (Exception e) {
            return null;
        }
    }

    public Maxmind start() throws IOException, InterruptedException {

        if (this.developer != null && this.developer) {

        }
        else if (this.staticDataFile != null) {

        }

        log.info("Maxmind starting...");
        log.info(" editionId: {}", this.editionId);

        if (this.developer != null && this.developer) {
            this.strategy = MaxmindStrategy.DEVELOPER;
            log.info(" strategy: {}", this.strategy);

            // build stubbed calls for localhost?
            if (this.stubbedLocations == null) {
                this.stubbedLocations = new ArrayList<>();
                for (String ip : asList("127.0.0.1", "::1", "0:0:0:0:0:0:0:1")) {
                    final IPAddressString address = new IPAddressString(ip);
                    final IPAddress hostAddress = address.getHostAddress();

                    this.stubbedLocations.add(new IpLocation()
                        .setIp(ip)
                        .setCanonicalIp(hostAddress.toCanonicalString())
                        .setNormalizedIp(address.toNormalizedString())
                        .setVersion(address.isIPv6() ? 6 : 4)
                        .setCountryCode("US")
                        .setCountryName("United States")
                        .setRegionCode("MI")
                        .setRegionName("Michigan")
                        .setCityName("Royal Oak")
                        .setPostalCode("48067")
                        .setTz("America/Detroit"));
                }
            }

            log.info(" stubbedLocations:");
            this.stubbedLocations.forEach(sl -> {
                log.info("  {}: {}, {}, {}", sl.getIp(), sl.getCountryCode(), sl.getRegionCode(), sl.getCityName());
            });
        }
        else if (this.staticDataFile == null) {
            this.strategy = MaxmindStrategy.AUTOMATIC;
            log.info(" strategy: {}", this.strategy);

            // does the data directory exist?
            if (this.dataDirectory != null) {
                Files.createDirectories(this.dataDirectory);
            }

            // automatic download mode
            this.downloader = new MaxmindDownloader(this, this.downloadInitialDelay, this.downloadEveryInterval);

            this.dataFile = this.dataDirectory.resolve("current.mmdb");
            this.etagFile = this.dataDirectory.resolve("current.mmdb.etag");
            boolean dataFileExists = Files.exists(this.dataFile);
            boolean etagFileExists = Files.exists(this.etagFile);

            log.info(" downloadEveryInterval: {}", this.downloadEveryInterval);
            log.info(" dataFile: {} (exists: {})", this.dataFile, dataFileExists);
            log.info(" etagFile: {} (exists: {})", this.etagFile, etagFileExists);

            if (dataFileExists && etagFileExists) {
                // we can load the database immediately
                this.etag = Files.readString(this.etagFile).trim();

                log.info(" etag: {}", this.etag);

                this.loadReader(this.dataFile);

                log.info(" opened database :-)");
            } else {
                log.info("Data is not present locally. Will download now...");

                this.downloader.downloadAndRotate();
            }

            this.downloader.start();
        }
        else {
            this.strategy = MaxmindStrategy.STATIC;
            log.info(" strategy: {}", this.strategy);

            log.info("Loading static data file {}", this.staticDataFile);

            this.loadReader(this.staticDataFile);
        }

        return this;
    }

    private void loadReader(Path dataFile) throws IOException {
        this.reader = new DatabaseReader.Builder(dataFile.toFile())
            //.withCache(new CHMCache())
            .build();

        this.dataFile = dataFile;
        this.metadata = this.reader.getMetadata();
        this.failureMessage = null;
        this.failureAt = null;

        final String _edition = ofNullable(this.metadata)
            .map(v -> v.getDatabaseType())
            .map(v -> v.toLowerCase())
            .orElse("");

        if (_edition.endsWith("city")) {
            this.edition = MaxmindEdition.CITY;
        }
        else {
            this.edition = MaxmindEdition.COUNTRY;
        }
    }

    public void failed(String failureMessage) {
        this.failureMessage = failureMessage;
        if (this.failureAt == null) {
            this.failureAt = System.currentTimeMillis();
        }
    }

    public void rotate(Path newDataFile, String newEtag) throws IOException, InterruptedException {
        log.info("Rotating current with new data...");
        log.info(" etag: {} -> {}", this.etag, newEtag);
        log.info(" dataFile: {} -> {}", newDataFile, this.dataFile);

        if (!this.dataWriteLock.tryLock(30, TimeUnit.SECONDS)) {
            throw new IOException("Unable to acquire write lock");
        }

        try {
            // close the current database reader
            final DatabaseReader oldReader = this.reader;

            if (oldReader != null) {
                oldReader.close();
            }

            // move the database to become the current
            Files.move(newDataFile, this.dataFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            // write out the etag
            Files.write(this.etagFile, asList(newEtag), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            this.etag = newEtag;

            this.loadReader(this.dataFile);

            log.info("Database rotated :-)");
        }
        finally {
            this.dataWriteLock.unlock();
        }
    }

    public Maxmind stop() throws IOException {
        if (this.downloader != null) {
            this.downloader.shutdown();
        }

        if (this.reader != null) {
            this.reader.close();
        }

        this.downloader = null;
        this.reader = null;

        return this;
    }

    public IpLocation lookup(String ip) throws IOException, InterruptedException {

        final IPAddressString address = new IPAddressString(ip);

        try {
            address.validate();
        } catch (AddressStringException e) {
            throw new IOException(e.getMessage(), e);
        }

        final IPAddress hostAddress = address.getHostAddress();
        final InetAddress inetAddress = address.getAddress().toInetAddress();

        //final InetAddress address = InetAddress.getByName(ip);

        if (!this.dataReadLock.tryLock(5, TimeUnit.SECONDS)) {
            throw new IOException("Unable to acquire read lock");
        }

        try {
            // if stubbed locations are provided, use those first
            if (this.stubbedLocations != null) {
                return this.stubbedLocations.stream()
                    .filter(v -> v.getIp().equals(ip))
                    .findFirst()
                    .orElse(null);
            }

            if (this.reader == null) {
                throw new IOException("Database is not available");
            }

            //log.info("Querying ip {}", ip);

            Location location = null;
            City city = null;
            Subdivision subdivision = null;
            Postal postal = null;
            Country country = null;

            try {


                if (tryAllEditions || this.edition == MaxmindEdition.CITY) {
                    Optional<CityResponse> cityResponse = this.reader.tryCity(inetAddress);
                    if (cityResponse.isPresent()) {
                        location = cityResponse.get().getLocation();
                        city = cityResponse.get().getCity();
                        subdivision = cityResponse.get().getMostSpecificSubdivision();
                        postal = cityResponse.get().getPostal();
                        country = cityResponse.get().getCountry();
                    }
                }

                if (tryAllEditions || this.edition == MaxmindEdition.COUNTRY) {
                    Optional<CountryResponse> countryResponse = this.reader.tryCountry(inetAddress);
                    if (countryResponse.isPresent()) {
                        country = countryResponse.get().getCountry();
                    }
                }
            } catch (Exception e) {
                throw new IOException(e);
            }

            // was anything found?
            if (country == null) {
                return null;
            }


            return new IpLocation()
                .setIp(ip)
                .setCanonicalIp(hostAddress.toCanonicalString())
                .setNormalizedIp(address.toNormalizedString())
                .setVersion(address.isIPv6() ? 6 : 4)
                .setCountryCode(ofNullable(country).map(v -> v.getIsoCode()).orElse(null))
                .setCountryName(ofNullable(country).map(v -> v.getName()).orElse(null))
                .setCityName(ofNullable(city).map(v -> v.getName()).orElse(null))
                .setRegionCode(ofNullable(subdivision).map(v -> v.getIsoCode()).orElse(null))
                .setRegionName(ofNullable(subdivision).map(v -> v.getName()).orElse(null))
                .setPostalCode(ofNullable(postal).map(v -> v.getCode()).orElse(null))
                .setLat(ofNullable(location).map(v -> v.getLatitude()).orElse(null))
                .setLng(ofNullable(location).map(v -> v.getLongitude()).orElse(null))
                .setTz(ofNullable(location).map(v -> v.getTimeZone()).orElse(null));
        }
        finally {
            this.dataReadLock.unlock();
        }
    }

}