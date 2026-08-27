package net.enchadd.utils;

final class SafetyModePolicySupport {

    private SafetyModePolicySupport() {
    }

    static String buildStateLabel(boolean safetyModeEnabled, boolean manualEnabled, boolean autoEnabled) {
        if (!safetyModeEnabled) {
            return "DISABLED";
        }
        if (manualEnabled && autoEnabled) {
            return "MANUAL+AUTO";
        }
        if (manualEnabled) {
            return "MANUAL";
        }
        if (autoEnabled) {
            return "AUTO";
        }
        return "OFF";
    }

    static String sanitizeReason(String raw) {
        if (raw == null || raw.isBlank()) {
            return "unknown";
        }
        return raw.replaceAll("[^A-Za-z0-9_+\\-]+", "_");
    }

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
