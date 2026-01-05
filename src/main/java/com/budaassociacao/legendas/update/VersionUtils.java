package com.budaassociacao.legendas.update;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for version-related operations.
 */
public class VersionUtils {

    private static final String VERSION_PROPERTIES_PATH = "/version.properties";

    /**
     * Get the current application version from version.properties.
     */
    public static String getCurrentVersion() {
        try (InputStream in = VersionUtils.class.getResourceAsStream(VERSION_PROPERTIES_PATH)) {
            if (in == null) {
                System.err.println("[VersionUtils] version.properties not found");
                return "1.0.0";
            }
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("application.version", "1.0.0").trim();
        } catch (IOException e) {
            System.err.println("[VersionUtils] Could not load version: " + e.getMessage());
            return "1.0.0";
        }
    }

    /**
     * Compare two version strings.
     * @return true if latest is newer than current
     */
    public static boolean isNewerVersion(String latest, String current) {
        if (latest == null || latest.isBlank() || current == null || current.isBlank()) {
            return false;
        }

        String[] lv = latest.split("\\.");
        String[] cv = current.split("\\.");

        try {
            for (int i = 0; i < Math.max(lv.length, cv.length); i++) {
                int l = i < lv.length ? Integer.parseInt(lv[i].trim()) : 0;
                int c = i < cv.length ? Integer.parseInt(cv[i].trim()) : 0;
                if (l > c) return true;
                if (l < c) return false;
            }
        } catch (NumberFormatException e) {
            System.err.println("[VersionUtils] Invalid version format: " + latest + " or " + current);
            return false;
        }

        return false;
    }

    /**
     * Parse version from tag name (strips v or v. prefix).
     */
    public static String parseVersionFromTag(String tagName) {
        if (tagName == null) return null;
        return tagName.startsWith("v.") ? tagName.replace("v.", "") :
               tagName.startsWith("v") ? tagName.replace("v", "") : tagName;
    }
}
