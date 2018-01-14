package com.example.mvmax.speakrecognition.speakrecognition.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Logger;

public final class FFMPEGDownloader {

    private static String url = "https://0110.be/releases/TarsosDSP/TarsosDSP-static-ffmpeg/";

    private static final Logger LOG = Logger.getLogger(FFMPEGDownloader.class.getName());

    private FFMPEGDownloader() {
        final String filename = operatingSystemName() + "_" + processorArchitecture() + "_ffmpeg" + suffix();
        url = url + filename;

        final String tempDirectory = System.getProperty("java.io.tmpdir");
        final String saveTo = new File(tempDirectory, filename).getAbsolutePath();

        if (new File(saveTo).exists()) {
            LOG.info("Found an already download ffmpeg static binary: " + saveTo);
        } else {
            LOG.info("Started downloading an ffmpeg static binary from  " + url);
            downloadExecutable(saveTo);

            if (new File(saveTo).exists()) {
                LOG.info("Downloaded an ffmpeg static binary. Stored at: " + saveTo);
                new File(saveTo).setExecutable(true);
            } else {
                LOG.warning("Unable to find or download an ffmpeg static binary.  " + filename);
            }
        }
    }

    private void downloadExecutable(final String saveTo) {
        try {
            final URL website = new URL(url);
            final ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            final FileOutputStream fos = new FileOutputStream(saveTo);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
        } catch (final MalformedURLException e) {
            e.printStackTrace();
        } catch (final IOException e) {

            e.printStackTrace();
        }
    }

    private String operatingSystemName() {
        final String name;
        final String operatingSystem = System.getProperty("os.name").toLowerCase();
        if (operatingSystem.indexOf("indows") > 0) {
            name = "windows";
        } else if (operatingSystem.indexOf("nux") >= 0) {
            name = "linux";
        } else if (operatingSystem.indexOf("mac") >= 0) {
            name = "mac_os_x";
        } else {
            name = null;
        }
        return name;
    }

    private String processorArchitecture() {
        boolean is64bit;
        if (System.getProperty("os.name").contains("Windows")) {
            is64bit = (System.getenv("ProgramFiles(x86)") != null);
        } else {
            is64bit = (System.getProperty("os.arch").indexOf("64") != -1);
        }
        if (is64bit) {
            return "64_bits";
        } else {
            return "32_bits";
        }
    }

    private String suffix() {
        String suffix = "";
        if (System.getProperty("os.name").contains("Windows")) {
            suffix = ".exe";
        }
        return suffix;
    }

    public static void main(final String... strings) {
        new FFMPEGDownloader();
    }
}
