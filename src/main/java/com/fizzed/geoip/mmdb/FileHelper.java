package com.fizzed.geoip.mmdb;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;

public class FileHelper {

    static public String md5(Path file) throws IOException {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new IOException("Unexpected digest error: " + e.getMessage(), e);
        }

        try (InputStream is = Files.newInputStream(file)) {
            // create byte array to read data in chunks
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            // read file data and update in message digest
            while ((bytesCount = is.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        // gt the hash's bytes
        final byte[] bytes = digest.digest();

        // convert it to hexadecimal format
        final StringBuilder sb = new StringBuilder();
        for (int i=0; i< bytes.length ;i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    static public Path untarball(Path file) throws IOException {
        TarArchiveInputStream tis = null;
        try {
            InputStream fis = Files.newInputStream(file);
            // .gz
            GZIPInputStream gzipInputStream = new GZIPInputStream(new BufferedInputStream(fis));
            //.tar.gz
            tis = new TarArchiveInputStream(gzipInputStream);
            TarArchiveEntry tarEntry = null;
            while ((tarEntry = tis.getNextTarEntry()) != null) {
                // we only care about the ".mmdb" file
                if (tarEntry.getName() != null && tarEntry.getName().endsWith(".mmdb")) {
                    // we found our entry
                    final Path tempFile = Files.createTempFile("maxmind-", ".mmdb");
                    try (OutputStream fos = Files.newOutputStream(tempFile)) {
                        IOUtils.copy(tis, fos);
                    }
                    //System.out.println("Copied database to " + tempFile);
                    return tempFile;
                }
            }
        } finally {
            if (tis != null) {
                try {
                    tis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        throw new IOException("Unable to find .mmdb entry in tarball");
    }

}