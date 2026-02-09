package com.enadd.core.error;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Enhanced error handler with structured logging and error categorization.
 * Provides better debugging and monitoring capabilities.
 */
public final class ErrorHandler {

    private static Plugin plugin;
    private static Logger logger;
    private static boolean initialized = false;

    private static final ConcurrentHashMap<ErrorCategory, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private static long firstErrorTime = 0;
    private static long lastErrorTime = 0;
    private static volatile boolean hasErrors = false;

    // Error categories for better organization
    public enum ErrorCategory {
        INITIALIZATION("Initialization"),
        REGISTRATION("Enchantment Registration"),
        ACHIEVEMENT("Achievement System"),
        CONFIG("Configuration"),
        PERMISSION("Permission"),
        EVENT("Event Handling"),
        API("API Call"),
        UNKNOWN("Unknown");

        private final String displayName;

        ErrorCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Prevent instantiation
    private ErrorHandler() {}

    /**
     * Initialize the error handler with a plugin reference.
     *
     * @param plugin the plugin instance
     */
    public static synchronized void initialize(Plugin plugin) {
        if (initialized) {
            return;
        }

        ErrorHandler.plugin = plugin;
        ErrorHandler.logger = plugin.getLogger();
        ErrorHandler.initialized = true;
    }

    /**
     * Log an error with category and context.
     *
     * @param category the error category
     * @param context the operation context
     * @param message the error message
     * @param throwable the exception (can be null)
     */
    public static void logError(ErrorCategory category, String context, String message, Throwable throwable) {
        if (!initialized) {
            System.err.println("[EnchAdd][" + category.getDisplayName() + "] " + context + ": " + message);
            if (throwable != null) {
                throwable.printStackTrace();
            }
            return;
        }

        String formattedMessage = String.format("[%s][%s] %s",
            category.getDisplayName(), context, message);

        if (throwable != null) {
            logger.log(Level.SEVERE, formattedMessage, throwable);
        } else {
            logger.severe(formattedMessage);
        }

        // Track error metrics (could be extended for monitoring)
        trackError(category);
    }

    /**
     * Log a warning with category and context.
     *
     * @param category the error category
     * @param context the operation context
     * @param message the warning message
     */
    public static void logWarning(ErrorCategory category, String context, String message) {
        if (!initialized) {
            System.err.println("[EnchAdd][WARN][" + category.getDisplayName() + "] " + context + ": " + message);
            return;
        }

        String formattedMessage = String.format("[%s][%s] %s",
            category.getDisplayName(), context, message);
        logger.warning(formattedMessage);
    }

    /**
     * Log an info message.
     *
     * @param context the operation context
     * @param message the info message
     */
    public static void logInfo(String context, String message) {
        if (!initialized) {
            System.out.println("[EnchAdd][INFO][" + context + "] " + message);
            return;
        }

        logger.info(String.format("[%s] %s", context, message));
    }

    /**
     * Log a debug message (only if debug mode is enabled).
     *
     * @param context the operation context
     * @param message the debug message
     */
    public static void logDebug(String context, String message) {
        if (!initialized || !isDebugEnabled()) {
            return;
        }

        logger.fine(String.format("[DEBUG][%s] %s", context, message));
    }

    /**
     * Handle an error with a fallback action.
     *
     * @param category the error category
     * @param context the operation context
     * @param message the error message
     * @param throwable the exception
     * @param fallback the fallback action to execute
     * @return the result of the fallback action, or null if no fallback
     */
    public static <T> T handleWithFallback(
            ErrorCategory category,
            String context,
            String message,
            Throwable throwable,
            T fallback) {

        logError(category, context, message, throwable);
        return fallback;
    }

    /**
     * Handle an error and return a default value.
     *
     * @param category the error category
     * @param context the operation context
     * @param message the error message
     * @param throwable the exception
     * @param defaultValue the default value to return
     * @return the default value
     */
    public static <T> T handleWithDefault(
            ErrorCategory category,
            String context,
            String message,
            Throwable throwable,
            T defaultValue) {

        logError(category, context, message, throwable);
        return defaultValue;
    }

    /**
     * Handle an error silently (suppress logging).
     *
     * @param category the error category
     * @param context the operation context
     * @param fallback the fallback action to execute
     * @return the result of the fallback action, or null if no fallback
     */
    public static <T> T handleSilently(ErrorCategory category, String context, T fallback) {
        return fallback;
    }

    /**
     * Execute an operation with error handling.
     *
     * @param category the error category
     * @param context the operation context
     * @param operation the operation to execute
     * @param fallback the fallback value on error
     * @return the result of the operation or fallback
     */
    public static <T> T execute(
            ErrorCategory category,
            String context,
            ErrorSafeOperation<T> operation,
            T fallback) {

        try {
            return operation.execute();
        } catch (Exception e) {
            logError(category, context, "Operation failed", e);
            return fallback;
        }
    }

    /**
     * Execute an operation with error handling and logging.
     *
     * @param category the error category
     * @param context the operation context
     * @param operation the operation to execute
     * @return the result, or null if operation failed
     */
    public static <T> T executeOrNull(
            ErrorCategory category,
            String context,
            ErrorSafeOperation<T> operation) {

        try {
            return operation.execute();
        } catch (Exception e) {
            logError(category, context, "Operation failed", e);
            return null;
        }
    }

    /**
     * Execute a runnable with error handling.
     *
     * @param category the error category
     * @param context the operation context
     * @param operation the runnable to execute
     */
    public static void executeRunnable(
            ErrorCategory category,
            String context,
            ErrorSafeRunnable operation) {

        try {
            operation.execute();
        } catch (Exception e) {
            logError(category, context, "Runnable execution failed", e);
        }
    }

    /**
     * Check if debug mode is enabled.
     *
     * @return true if debug mode is enabled
     */
    private static boolean isDebugEnabled() {
        if (plugin == null) {
            return false;
        }

        try {
            FileConfiguration config = plugin.getConfig();
            if (config != null) {
                return config.getBoolean("debug-mode", false);
            }
        } catch (Exception e) {
            // Ignore config errors
        }

        return false;
    }

    /**
     * Track error metrics (can be extended for monitoring).
     *
     * @param category the error category
     */
    private static void trackError(ErrorCategory category) {
        errorCounts.computeIfAbsent(category, k -> new AtomicInteger(0)).incrementAndGet();

        long currentTime = System.currentTimeMillis();
        if (firstErrorTime == 0) {
            firstErrorTime = currentTime;
        }
        lastErrorTime = currentTime;
        hasErrors = true;
    }

    /**
     * Get error statistics.
     *
     * @return error statistics as a formatted string
     */
    public static String getErrorStats() {
        if (!hasErrors) {
            return "No errors recorded yet";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Error Statistics:\n");
        sb.append("First Error: ").append(formatTimestamp(firstErrorTime)).append("\n");
        sb.append("Last Error: ").append(formatTimestamp(lastErrorTime)).append("\n");

        for (ErrorCategory category : ErrorCategory.values()) {
            AtomicInteger count = errorCounts.get(category);
            if (count != null && count.get() > 0) {
                sb.append(category.getDisplayName()).append(": ").append(count.get()).append("\n");
            }
        }

        return sb.toString();
    }

    private static String formatTimestamp(long timestamp) {
        if (timestamp == 0) {
            return "N/A";
        }
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.CHINA)
            .format(new java.util.Date(timestamp));
    }

    /**
     * Get total error count.
     *
     * @return total number of errors
     */
    public static int getTotalErrorCount() {
        return errorCounts.values().stream()
            .mapToInt(AtomicInteger::get)
            .sum();
    }

    /**
     * Check if any errors have been recorded.
     *
     * @return true if errors have been recorded
     */
    public static boolean hasErrors() {
        return hasErrors;
    }

    /**
     * Get error count for a specific category.
     *
     * @param category the error category
     * @return the error count
     */
    public static int getErrorCount(ErrorCategory category) {
        AtomicInteger count = errorCounts.get(category);
        return count != null ? count.get() : 0;
    }

    /**
     * Reset error tracking statistics.
     */
    public static synchronized void resetStats() {
        errorCounts.clear();
        firstErrorTime = 0;
        lastErrorTime = 0;
        hasErrors = false;
    }

    /**
     * Functional interface for safe operations.
     *
     * @param <T> the return type
     */
    @FunctionalInterface
    public interface ErrorSafeOperation<T> {
        T execute() throws Exception;
    }

    /**
     * Functional interface for safe runnables.
     */
    @FunctionalInterface
    public interface ErrorSafeRunnable {
        void execute() throws Exception;
    }
}
