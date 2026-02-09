package com.enadd.util;

import java.util.logging.Logger;


/**
 * Java version compatibility checker
 */
public final class JavaVersionChecker {

    private static final int MIN_JAVA_VERSION = 21;
    private static final int MAX_TESTED_VERSION = 25;

    // Prevent instantiation
    private JavaVersionChecker() {}

    /**
     * Check Java version compatibility
     */
    public static boolean checkCompatibility(Logger logger) {
        try {
            JavaVersionInfo versionInfo = getJavaVersionInfo();

            if (versionInfo.majorVersion < MIN_JAVA_VERSION) {
                logger.severe("Unsupported Java version: " + versionInfo.fullVersion);
                logger.severe("Minimum required: Java " + MIN_JAVA_VERSION);
                logger.severe("Please upgrade your Java installation");
                return false;
            }

            if (versionInfo.majorVersion > MAX_TESTED_VERSION) {
                logger.warning("Running on untested Java version: " + versionInfo.fullVersion);
                logger.warning("This plugin has been tested up to Java " + MAX_TESTED_VERSION);
                logger.warning("Compatibility is not guaranteed but should work");
            }

            // Log compatibility status
            logCompatibilityStatus(logger, versionInfo);

            return true;

        } catch (Exception e) {
            logger.severe("Failed to check Java version compatibility: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get detailed Java version information
     */
    public static JavaVersionInfo getJavaVersionInfo() {
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        String javaVmName = System.getProperty("java.vm.name");
        String javaVmVersion = System.getProperty("java.vm.version");

        int majorVersion = parseMajorVersion(javaVersion);

        return new JavaVersionInfo(javaVersion, majorVersion, javaVendor, javaVmName, javaVmVersion);
    }

    /**
     * Parse major version from version string
     */
    private static int parseMajorVersion(String version) {
        try {
            if (version == null || version.trim().isEmpty()) {
                return 0;
            }

            // New format: 21.0.1 -> 21
            String[] parts = version.split("\\.");
            if (parts.length > 0 && parts[0] != null && !parts[0].trim().isEmpty()) {
                return Integer.parseInt(parts[0].trim());
            }
            return 0;
        } catch (Exception e) {
            // Fallback: try to extract first number
            try {
                if (version != null && !version.isEmpty()) {
                    String cleaned = version.replaceAll("[^0-9].*", "");
                    if (!cleaned.isEmpty()) {
                        return Integer.parseInt(cleaned);
                    }
                }
                return 0;
            } catch (Exception fallbackError) {
                return 0; // Unknown version
            }
        }
    }

    /**
     * Log compatibility status
     */
    private static void logCompatibilityStatus(Logger logger, JavaVersionInfo versionInfo) {
        logger.info("Java Version Compatibility Check:");
        logger.info("  Version: " + versionInfo.fullVersion);
        logger.info("  Vendor: " + versionInfo.vendor);
        logger.info("  VM: " + versionInfo.vmName + " " + versionInfo.vmVersion);

        String status = getCompatibilityStatus(versionInfo.majorVersion);
        logger.info("  Status: " + status);

        // Provide specific recommendations
        if (versionInfo.majorVersion == 21) {
            logger.info("  ✅ Perfect compatibility - Recommended version");
        } else if (versionInfo.majorVersion >= 22 && versionInfo.majorVersion <= 25) {
            logger.info("  ✅ Forward compatible - Should work without issues");
        } else if (versionInfo.majorVersion > 25) {
            logger.info("  ⚠️ Untested version - May work but not guaranteed");
        }
    }

    /**
     * Get compatibility status string
     */
    private static String getCompatibilityStatus(int majorVersion) {
        if (majorVersion == 21) {
            return "Fully Supported (Primary Target)";
        } else if (majorVersion >= 22 && majorVersion <= 25) {
            return "Forward Compatible";
        } else if (majorVersion > 25) {
            return "Untested (Likely Compatible)";
        } else {
            return "Unsupported";
        }
    }

    /**
     * Check if running on preview/early access version
     */
    public static boolean isPreviewVersion() {
        String version = System.getProperty("java.version");
        return version.contains("ea") || version.contains("preview") || version.contains("beta");
    }

    /**
     * Get recommended Java version message
     */
    public static String getRecommendedVersionMessage() {
        return "For best compatibility, use Java 21 (LTS). " +
               "Java 22-25 are also supported. " +
               "Minimum required: Java " + MIN_JAVA_VERSION;
    }

    /**
     * Java version information container
     */
    public static class JavaVersionInfo {
        public final String fullVersion;
        public final int majorVersion;
        public final String vendor;
        public final String vmName;
        public final String vmVersion;

        public JavaVersionInfo(String fullVersion, int majorVersion, String vendor, String vmName, String vmVersion) {
            this.fullVersion = fullVersion;
            this.majorVersion = majorVersion;
            this.vendor = vendor;
            this.vmName = vmName;
            this.vmVersion = vmVersion;
        }

        @Override
        public String toString() {
            return String.format("Java %d (%s) by %s", majorVersion, fullVersion, vendor);
        }
    }
}
