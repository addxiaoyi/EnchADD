package com.enadd.core.security;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


public final class ConfigValidator {

    private static final Pattern ENCHANTMENT_KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");

    private static final Set<String> SUPPORTED_LANGUAGES = new HashSet<>();
    private static final Set<String> SUPPORTED_RARITIES = new HashSet<>();
    private static final Set<String> SUPPORTED_CATEGORIES = new HashSet<>();

    static {
        SUPPORTED_LANGUAGES.add("en");
        SUPPORTED_LANGUAGES.add("zh");
        SUPPORTED_LANGUAGES.add("zh_tw");
        SUPPORTED_LANGUAGES.add("de");
        SUPPORTED_LANGUAGES.add("pt");
        SUPPORTED_LANGUAGES.add("ms");
        SUPPORTED_LANGUAGES.add("fr");
        SUPPORTED_LANGUAGES.add("es");
        SUPPORTED_LANGUAGES.add("ja");
        SUPPORTED_LANGUAGES.add("ko");

        SUPPORTED_RARITIES.add("common");
        SUPPORTED_RARITIES.add("uncommon");
        SUPPORTED_RARITIES.add("rare");
        SUPPORTED_RARITIES.add("very_rare");

        SUPPORTED_CATEGORIES.add("weapon");
        SUPPORTED_CATEGORIES.add("digger");
        SUPPORTED_CATEGORIES.add("armor");
        SUPPORTED_CATEGORIES.add("armor_head");
        SUPPORTED_CATEGORIES.add("armor_chest");
        SUPPORTED_CATEGORIES.add("armor_legs");
        SUPPORTED_CATEGORIES.add("armor_feet");
        SUPPORTED_CATEGORIES.add("bow");
        SUPPORTED_CATEGORIES.add("trident");
        SUPPORTED_CATEGORIES.add("crossbow");
        SUPPORTED_CATEGORIES.add("fishing_rod");
        SUPPORTED_CATEGORIES.add("wearable");
        SUPPORTED_CATEGORIES.add("breakable");
        SUPPORTED_CATEGORIES.add("vanishable");
    }

    private ConfigValidator() {}

    public static String validateLanguage(String language) {
        if (language == null || language.trim().isEmpty()) {
            return "en";
        }

        String sanitized = language.trim().toLowerCase().replaceAll("[^a-z_]", "");

        if (SUPPORTED_LANGUAGES.contains(sanitized)) {
            return sanitized;
        }

        return "en";
    }

    public static String validateEnchantmentKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }

        String sanitized = key.trim().toLowerCase().replaceAll("[^a-z0-9_]", "_");

        if (sanitized.isEmpty() || !Character.isLetter(sanitized.charAt(0))) {
            sanitized = "ench_" + sanitized;
        }

        if (ENCHANTMENT_KEY_PATTERN.matcher(sanitized).matches()) {
            return sanitized;
        }

        return null;
    }

    public static String validateRarity(String rarity) {
        if (rarity == null || rarity.trim().isEmpty()) {
            return "common";
        }

        String sanitized = rarity.trim().toLowerCase().replaceAll("[^a-z_]", "");

        if (SUPPORTED_RARITIES.contains(sanitized)) {
            return sanitized;
        }

        return "common";
    }

    public static String validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "breakable";
        }

        String sanitized = category.trim().toLowerCase().replaceAll("[^a-z_]", "");

        if (SUPPORTED_CATEGORIES.contains(sanitized)) {
            return sanitized;
        }

        return "breakable";
    }

    public static int validateRange(int value, int min, int max, int defaultValue) {
        if (value < min || value > max) {
            return defaultValue;
        }
        return value;
    }

    public static String validateSafeString(String value, boolean allowEmpty) {
        if (value == null) {
            return allowEmpty ? "" : null;
        }

        String trimmed = value.trim();

        if (!allowEmpty && trimmed.isEmpty()) {
            return null;
        }

        if (containsDangerousPattern(trimmed)) {
            return null;
        }

        return trimmed;
    }

    private static boolean containsDangerousPattern(String value) {
        if (value.contains("..") || value.contains("/") || value.contains("\\")) {
            return true;
        }

        if (value.contains("\0")) {
            return true;
        }

        String lower = value.toLowerCase();
        return lower.contains("<script") ||
               lower.contains("${") ||
               lower.contains("#{") ||
               lower.contains("${env");
    }

    public static boolean validateBoolean(String value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        String lower = value.trim().toLowerCase();

        if (lower.equals("true") || lower.equals("yes") || lower.equals("1")) {
            return true;
        } else if (lower.equals("false") || lower.equals("no") || lower.equals("0")) {
            return false;
        }

        return defaultValue;
    }

    public static Set<String> validateDisabledEnchantments(Set<String> disabled) {
        Set<String> validated = new HashSet<>();

        if (disabled == null) {
            return validated;
        }

        for (String key : disabled) {
            String validatedKey = validateEnchantmentKey(key);
            if (validatedKey != null) {
                validated.add(validatedKey);
            }
        }

        return validated;
    }

    public static boolean isWhitelisted(String value, Set<String> whitelist) {
        return value != null && whitelist.contains(value.trim().toLowerCase());
    }

    public static Set<String> getSupportedLanguages() {
        return Collections.unmodifiableSet(SUPPORTED_LANGUAGES);
    }

    public static Set<String> getSupportedRarities() {
        return Collections.unmodifiableSet(SUPPORTED_RARITIES);
    }

    public static Set<String> getSupportedCategories() {
        return Collections.unmodifiableSet(SUPPORTED_CATEGORIES);
    }

    public static Path validateFilePath(String path, Path baseDir) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        try {
            Path requested = Paths.get(path);
            Path resolved = baseDir.resolve(requested).normalize();

            if (resolved.startsWith(baseDir) && !resolved.equals(baseDir)) {
                return resolved;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String validateIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return null;
        }

        String trimmed = ip.trim();
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        if (trimmed.matches(ipv4Pattern) || trimmed.equals("localhost")) {
            return trimmed;
        }

        return null;
    }

    public static int validatePort(int port) {
        if (port >= 1 && port <= 65535) {
            return port;
        }
        return -1;
    }

    public static String sanitizeForDisplay(String input) {
        if (input == null) {
            return "";
        }

        return input.replaceAll("[\\x00-\\x1F\\x7F]", "")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    public static String validatePlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String trimmed = name.trim();

        if (trimmed.length() < 3 || trimmed.length() > 16) {
            return null;
        }

        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            return null;
        }

        return trimmed;
    }

    public static boolean checkLengthLimit(String value, int maxLength) {
        return value != null && value.length() <= maxLength;
    }

    public static String validateColorCode(String color) {
        if (color == null || color.trim().isEmpty()) {
            return null;
        }

        String trimmed = color.trim();

        if (trimmed.matches("^#[0-9A-Fa-f]{6}$") || trimmed.matches("^[0-9A-Fa-f]{6}$")) {
            return trimmed.startsWith("#") ? trimmed : "#" + trimmed;
        }

        return null;
    }
}
