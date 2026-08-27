package net.enchadd.utils;

final class RuntimeErrorTrackerLogSupport {

    private static final int MAX_SOURCE_LENGTH = 96;

    private RuntimeErrorTrackerLogSupport() {
    }

    static String format(String source, long total) {
        return "[EnchADD] runtime error recorded from " + sanitizeSource(source) + " total=" + total;
    }

    static String sanitizeSource(String source) {
        if (source == null || source.isBlank()) {
            return "unknown";
        }
        String sanitized = source.replaceAll("[^A-Za-z0-9_.:+\\-]+", "_");
        if (sanitized.length() <= MAX_SOURCE_LENGTH) {
            return sanitized;
        }
        return sanitized.substring(0, MAX_SOURCE_LENGTH);
    }
}
