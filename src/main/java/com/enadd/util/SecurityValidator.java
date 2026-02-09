package com.enadd.util;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Security validation utilities to prevent common vulnerabilities
 *
 * 提供全面的输入验证和清理功能，防止：
 * - 路径遍历攻击
 * - 注入攻击
 * - 格式字符串攻击
 * - 资源耗尽攻击
 *
 * 线程安全：所有方法都是线程安全的
 *
 * @author EnchAdd Team
 * @version 2.0
 */
public final class SecurityValidator {
    private static final Logger LOGGER = Logger.getLogger(SecurityValidator.class.getName());

    // 配置常量
    private static final int MAX_FILENAME_LENGTH = 50;
    private static final int MAX_CONFIG_KEY_LENGTH = 100;
    private static final int MAX_ENCHANTMENT_KEY_LENGTH = 50;
    private static final int MIN_PLAYER_NAME_LENGTH = 1;
    private static final int MAX_PLAYER_NAME_LENGTH = 16;
    private static final int MAX_STRING_LENGTH = 1000;

    // 统计信息
    private static final AtomicLong validationCount = new AtomicLong(0);
    private static final AtomicLong validationFailures = new AtomicLong(0);
    private static final AtomicLong sanitizationCount = new AtomicLong(0);
    private static final ConcurrentHashMap<String, AtomicLong> threatTypes = new ConcurrentHashMap<>();

    // Allowed language codes (whitelist)
    // Bug修复34: 使用不可变集合
    private static final Set<String> ALLOWED_LANGUAGES;

    // Safe filename pattern (alphanumeric, underscore, hyphen, dot)
    private static final Pattern SAFE_FILENAME_PATTERN;

    // Safe key pattern (alphanumeric, underscore, dot, hyphen)
    private static final Pattern SAFE_KEY_PATTERN;

    // Safe player name pattern
    private static final Pattern SAFE_PLAYER_NAME_PATTERN;

    // Safe enchantment key pattern (cached for performance)
    private static final Pattern SAFE_ENCHANTMENT_KEY_PATTERN;

    // Safe characters pattern (cached for performance)
    private static final Pattern SAFE_CHARS_PATTERN;

    // Path traversal pattern
    private static final Pattern PATH_TRAVERSAL_PATTERN;

    // Dangerous characters pattern
    private static final Pattern DANGEROUS_CHARS_PATTERN;

    // UUID pattern
    private static final Pattern UUID_PATTERN;

    // Max path depth
    private static final int MAX_PATH_DEPTH = 10;

    // Allowed file extensions
    private static final Set<String> ALLOWED_EXTENSIONS;

    // Reserved keywords
    private static final Set<String> RESERVED_KEYWORDS;

    // Bug修复2-5: 安全的静态初始化
    static {
        try {
            // 初始化语言白名单
            Set<String> languages = new HashSet<>();
            languages.add("en");
            languages.add("zh");
            languages.add("zh_tw");
            languages.add("de");
            languages.add("pt");
            languages.add("ms");
            languages.add("fr");
            ALLOWED_LANGUAGES = Collections.unmodifiableSet(languages);

            // 初始化扩展名白名单
            Set<String> extensions = new HashSet<>();
            extensions.add(".yml");
            extensions.add(".yaml");
            extensions.add(".json");
            extensions.add(".txt");
            extensions.add(".properties");
            ALLOWED_EXTENSIONS = Collections.unmodifiableSet(extensions);

            // 初始化保留关键字
            Set<String> keywords = new HashSet<>();
            keywords.add("null");
            keywords.add("undefined");
            keywords.add("system");
            keywords.add("admin");
            keywords.add("root");
            keywords.add("config");
            RESERVED_KEYWORDS = Collections.unmodifiableSet(keywords);

            // 编译正则表达式
            SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
            SAFE_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
            SAFE_PLAYER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{" +
                MIN_PLAYER_NAME_LENGTH + "," + MAX_PLAYER_NAME_LENGTH + "}$");
            SAFE_ENCHANTMENT_KEY_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");
            SAFE_CHARS_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s.,!?:;()\\[\\]{}\"'-]*$");
            PATH_TRAVERSAL_PATTERN = Pattern.compile("\\.\\.|/|\\\\");
            DANGEROUS_CHARS_PATTERN = Pattern.compile("[<>\"'%;()&+]");
            UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

            LOGGER.info("SecurityValidator initialized successfully");
        } catch (PatternSyntaxException e) {
            LOGGER.log(Level.SEVERE, "Failed to compile security patterns", e);
            throw new ExceptionInInitializerError(e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize SecurityValidator", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    // Prevent instantiation
    private SecurityValidator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validate language code against whitelist
     * Bug修复6: 添加异常处理和统计
     */
    public static boolean isValidLanguage(String language) {
        validationCount.incrementAndGet();

        try {
            // Bug修复7: 添加null和空检查
            if (language == null || language.trim().isEmpty()) {
                recordThreat("null_language");
                validationFailures.incrementAndGet();
                return false;
            }

            // Bug修复20: 添加长度限制防止DoS
            if (language.length() > 10) {
                recordThreat("oversized_language");
                validationFailures.incrementAndGet();
                return false;
            }

            String sanitized = language.trim().toLowerCase();
            boolean valid = ALLOWED_LANGUAGES.contains(sanitized);

            if (!valid) {
                recordThreat("invalid_language");
                validationFailures.incrementAndGet();
            }

            return valid;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validating language: " + language, e);
            validationFailures.incrementAndGet();
            return false;
        }
    }

    /**
     * Sanitize language code to safe value
     * Bug修复8: 添加异常处理
     */
    public static String sanitizeLanguage(String language) {
        sanitizationCount.incrementAndGet();

        try {
            if (isValidLanguage(language)) {
                return language.trim().toLowerCase();
            }
            LOGGER.warning("Invalid language sanitized to default: " + language);
            return "en"; // Default fallback
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error sanitizing language", e);
            return "en";
        }
    }

    /**
     * Validate filename to prevent path traversal
     * Bug修复9-10: 添加异常处理和威胁检测
     */
    public static boolean isValidFilename(String filename) {
        validationCount.incrementAndGet();

        try {
            // Bug修复11: null检查
            if (filename == null || filename.trim().isEmpty()) {
                recordThreat("null_filename");
                validationFailures.incrementAndGet();
                return false;
            }

            String sanitized = filename.trim();

            // Bug修复20: 长度限制
            if (sanitized.length() > MAX_FILENAME_LENGTH) {
                recordThreat("oversized_filename");
                validationFailures.incrementAndGet();
                return false;
            }

            // Bug修复26: 路径遍历检测
            if (PATH_TRAVERSAL_PATTERN.matcher(sanitized).find()) {
                recordThreat("path_traversal_attempt");
                validationFailures.incrementAndGet();
                LOGGER.warning("Path traversal attempt detected: " + sanitized);
                return false;
            }

            // Check for path traversal attempts
            if (sanitized.contains("..") ||
                sanitized.contains("/") ||
                sanitized.contains("\\") ||
                sanitized.startsWith(".")) {
                recordThreat("path_traversal");
                validationFailures.incrementAndGet();
                return false;
            }

            // Bug修复27: 危险字符检测
            if (DANGEROUS_CHARS_PATTERN.matcher(sanitized).find()) {
                recordThreat("dangerous_chars_in_filename");
                validationFailures.incrementAndGet();
                return false;
            }

            // Check against safe pattern
            boolean valid = SAFE_FILENAME_PATTERN.matcher(sanitized).matches();

            if (!valid) {
                recordThreat("invalid_filename_pattern");
                validationFailures.incrementAndGet();
            }

            return valid;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validating filename: " + filename, e);
            validationFailures.incrementAndGet();
            return false;
        }
    }

    /**
     * Sanitize filename to safe value
     * Bug修复12: 添加异常处理和验证
     */
    public static String sanitizeFilename(String filename) {
        sanitizationCount.incrementAndGet();

        try {
            if (filename == null || filename.trim().isEmpty()) {
                return "default";
            }

            // Bug修复20: 长度限制
            String input = filename.trim();
            if (input.length() > MAX_FILENAME_LENGTH * 2) {
                input = input.substring(0, MAX_FILENAME_LENGTH * 2);
            }

            String sanitized = input
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_");

            // Ensure it doesn't start with dot or underscore
            if (sanitized.startsWith(".") || sanitized.startsWith("_")) {
                sanitized = "file_" + sanitized;
            }

            // Limit length
            if (sanitized.length() > MAX_FILENAME_LENGTH) {
                sanitized = sanitized.substring(0, MAX_FILENAME_LENGTH);
            }

            String result = sanitized.isEmpty() ? "default" : sanitized;

            if (!result.equals(filename)) {
                LOGGER.fine("Filename sanitized: " + filename + " -> " + result);
            }

            return result;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error sanitizing filename", e);
            return "default";
        }
    }

    /**
     * Validate configuration key
     * Bug修复13: 添加异常处理
     */
    public static boolean isValidConfigKey(String key) {
        validationCount.incrementAndGet();

        try {
            if (key == null || key.trim().isEmpty()) {
                recordThreat("null_config_key");
                validationFailures.incrementAndGet();
                return false;
            }

            String sanitized = key.trim();

            // Check length
            if (sanitized.length() > MAX_CONFIG_KEY_LENGTH) {
                recordThreat("oversized_config_key");
                validationFailures.incrementAndGet();
                return false;
            }

            // Check for reserved keywords
            if (RESERVED_KEYWORDS.contains(sanitized.toLowerCase())) {
                recordThreat("reserved_keyword");
                validationFailures.incrementAndGet();
                return false;
            }

            // Check against safe pattern
            boolean valid = SAFE_KEY_PATTERN.matcher(sanitized).matches();

            if (!valid) {
                recordThreat("invalid_config_key");
                validationFailures.incrementAndGet();
            }

            return valid;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validating config key: " + key, e);
            validationFailures.incrementAndGet();
            return false;
        }
    }

    /**
     * Sanitize configuration key
     */
    public static String sanitizeConfigKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return "unknown";
        }

        String sanitized = key.trim()
            .toLowerCase()
            .replaceAll("[^a-zA-Z0-9._-]", "_")
            .replaceAll("_{2,}", "_");

        // Limit length
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }

        return sanitized.isEmpty() ? "unknown" : sanitized;
    }

    /**
     * Validate player name
     */
    public static boolean isValidPlayerName(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }

        String sanitized = playerName.trim();
        return SAFE_PLAYER_NAME_PATTERN.matcher(sanitized).matches();
    }

    /**
     * Validate file path to prevent directory traversal
     * Bug修复14: 添加完整的路径验证和异常处理
     */
    public static boolean isValidFilePath(File baseDir, String requestedPath) {
        validationCount.incrementAndGet();

        try {
            // Bug修复18: null检查
            if (baseDir == null || requestedPath == null) {
                recordThreat("null_file_path");
                validationFailures.incrementAndGet();
                return false;
            }

            // Bug修复20: 长度限制
            if (requestedPath.length() > 500) {
                recordThreat("oversized_path");
                validationFailures.incrementAndGet();
                return false;
            }

            // Bug修复26: 路径遍历检测
            if (PATH_TRAVERSAL_PATTERN.matcher(requestedPath).find()) {
                recordThreat("path_traversal_in_path");
                validationFailures.incrementAndGet();
                LOGGER.warning("Path traversal attempt in file path: " + requestedPath);
                return false;
            }

            // 检查路径深度
            int depth = requestedPath.split("[/\\\\]").length;
            if (depth > MAX_PATH_DEPTH) {
                recordThreat("excessive_path_depth");
                validationFailures.incrementAndGet();
                return false;
            }

            Path basePath = baseDir.toPath().normalize().toAbsolutePath();
            Path requestedFile = basePath.resolve(requestedPath).normalize().toAbsolutePath();

            // Ensure the requested file is within the base directory
            boolean valid = requestedFile.startsWith(basePath);

            if (!valid) {
                recordThreat("path_escape_attempt");
                validationFailures.incrementAndGet();
                LOGGER.warning("Path escape attempt: " + requestedPath);
            }

            return valid;

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validating file path: " + requestedPath, e);
            validationFailures.incrementAndGet();
            return false;
        }
    }

    /**
     * Validate string length
     * Bug修复19: 添加异常处理
     */
    public static boolean isValidLength(String value, int maxLength) {
        try {
            return value != null && value.length() <= maxLength && maxLength > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate numeric range
     * Bug修复19: 添加异常处理和NaN检查
     */
    public static boolean isValidRange(int value, int min, int max) {
        try {
            return min <= max && value >= min && value <= max;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate numeric range
     * Bug修复19: 添加异常处理和NaN/Infinity检查
     */
    public static boolean isValidRange(double value, double min, double max) {
        try {
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return false;
            }
            if (Double.isNaN(min) || Double.isInfinite(min)) {
                return false;
            }
            if (Double.isNaN(max) || Double.isInfinite(max)) {
                return false;
            }
            return min <= max && value >= min && value <= max;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate port number
     * Bug修复19: 添加异常处理
     */
    public static boolean isValidPort(int port) {
        try {
            return port >= 1 && port <= 65535;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate percentage value
     * Bug修复19: 添加异常处理和NaN检查
     */
    public static boolean isValidPercentage(double percentage) {
        try {
            if (Double.isNaN(percentage) || Double.isInfinite(percentage)) {
                return false;
            }
            return percentage >= 0.0 && percentage <= 100.0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate probability value
     * Bug修复19: 添加异常处理和NaN检查
     */
    public static boolean isValidProbability(double probability) {
        try {
            if (Double.isNaN(probability) || Double.isInfinite(probability)) {
                return false;
            }
            return probability >= 0.0 && probability <= 1.0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sanitize string to prevent injection
     */
    public static String sanitizeString(String input) {
        if (input == null) {
            return "";
        }

        return input.trim()
            .replaceAll("[<>\"'&]", "") // Remove potential HTML/XML chars
            .replaceAll("\\p{Cntrl}", ""); // Remove control characters
    }

    /**
     * Validate enchantment key format
     */
    public static boolean isValidEnchantmentKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        String sanitized = key.trim().toLowerCase();

        // Check length
        if (sanitized.length() > 50) {
            return false;
        }

        // Check format: only lowercase letters, numbers, and underscores
        return Pattern.matches("^[a-z][a-z0-9_]*$", sanitized);
    }

    /**
     * Sanitize enchantment key
     */
    public static String sanitizeEnchantmentKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return "unknown_enchantment";
        }

        String sanitized = key.trim()
            .toLowerCase()
            .replaceAll("[^a-z0-9_]", "_")
            .replaceAll("_{2,}", "_");

        // Ensure it starts with a letter
        if (!sanitized.matches("^[a-z].*")) {
            sanitized = "enchant_" + sanitized;
        }

        // Limit length
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }

        return sanitized.isEmpty() ? "unknown_enchantment" : sanitized;
    }

    /**
     * Validate positive integer
     */
    public static int validatePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive: " + value);
        }
        return value;
    }

    /**
     * Validate positive double
     */
    public static double validatePositive(double value, String name) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0) {
            throw new IllegalArgumentException(name + " must be positive: " + value);
        }
        return value;
    }

    /**
     * Validate UUID format
     * Bug修复16: 使用预编译Pattern提升性能
     */
    public static boolean isValidUUID(String uuid) {
        validationCount.incrementAndGet();

        try {
            if (uuid == null || uuid.trim().isEmpty()) {
                recordThreat("null_uuid");
                validationFailures.incrementAndGet();
                return false;
            }

            String sanitized = uuid.trim();

            // Bug修复20: 长度检查
            if (sanitized.length() != 36) {
                validationFailures.incrementAndGet();
                return false;
            }

            // 使用预编译Pattern
            boolean valid = UUID_PATTERN.matcher(sanitized).matches();

            if (!valid) {
                recordThreat("invalid_uuid");
                validationFailures.incrementAndGet();
            }

            return valid;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error validating UUID: " + uuid, e);
            validationFailures.incrementAndGet();
            return false;
        }
    }

    /**
     * Check if string contains only safe characters
     * Bug修复17: 使用预编译Pattern
     */
    public static boolean containsOnlySafeChars(String input) {
        validationCount.incrementAndGet();

        try {
            if (input == null) {
                validationFailures.incrementAndGet();
                return false;
            }

            // Bug修复20: 长度限制
            if (input.length() > MAX_STRING_LENGTH) {
                recordThreat("oversized_string");
                validationFailures.incrementAndGet();
                return false;
            }

            boolean valid = SAFE_CHARS_PATTERN.matcher(input).matches();

            if (!valid) {
                recordThreat("unsafe_chars");
                validationFailures.incrementAndGet();
            }

            return valid;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking safe chars", e);
            validationFailures.incrementAndGet();
            return false;
        }
    }

    /**
     * 记录威胁类型
     * Bug修复26: 添加威胁统计
     */
    private static void recordThreat(String threatType) {
        try {
            threatTypes.computeIfAbsent(threatType, k -> new AtomicLong(0)).incrementAndGet();
        } catch (Exception e) {
            // 忽略统计错误
        }
    }

    /**
     * 获取验证统计信息
     * Bug修复25: 添加统计方法
     */
    public static ValidationStats getStats() {
        return new ValidationStats(
            validationCount.get(),
            validationFailures.get(),
            sanitizationCount.get(),
            new ConcurrentHashMap<>(threatTypes)
        );
    }

    /**
     * 重置统计信息
     */
    public static void resetStats() {
        validationCount.set(0);
        validationFailures.set(0);
        sanitizationCount.set(0);
        threatTypes.clear();
    }

    /**
     * 验证统计信息类
     */
    public static class ValidationStats {
        public final long totalValidations;
        public final long totalFailures;
        public final long totalSanitizations;
        public final ConcurrentHashMap<String, AtomicLong> threatCounts;

        public ValidationStats(long validations, long failures, long sanitizations,
                             ConcurrentHashMap<String, AtomicLong> threats) {
            this.totalValidations = validations;
            this.totalFailures = failures;
            this.totalSanitizations = sanitizations;
            this.threatCounts = threats;
        }

        public double getFailureRate() {
            return totalValidations > 0 ? (double) totalFailures / totalValidations : 0.0;
        }

        @Override
        public String toString() {
            return String.format(
                "ValidationStats{validations=%d, failures=%d (%.2f%%), sanitizations=%d, threats=%d}",
                totalValidations, totalFailures, getFailureRate() * 100,
                totalSanitizations, threatCounts.size()
            );
        }
    }
}
