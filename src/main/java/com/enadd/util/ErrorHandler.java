package com.enadd.util;

import org.bukkit.plugin.Plugin;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;



/**
 * Comprehensive error handling and logging system
 * Prevents error spam and provides detailed debugging information
 */
public final class ErrorHandler {

    private static Logger logger;
    private static final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> lastErrorTime = new ConcurrentHashMap<>();
    private static final long ERROR_COOLDOWN_MS = 5000; // 5 seconds
    private static final int MAX_ERROR_COUNT = 10;

    // Prevent instantiation
    private ErrorHandler() {}

    public static void initialize(Plugin plugin) {
        // Bug #447: 添加plugin参数验证
        if (plugin == null) {
            System.err.println("ErrorHandler.initialize: plugin为null，使用System.err作为后备");
            logger = null;
            return;
        }

        try {
            logger = plugin.getLogger();
            // Bug #448: 验证logger是否成功获取
            if (logger == null) {
                System.err.println("ErrorHandler.initialize: 无法获取logger，使用System.err作为后备");
            } else {
                logger.info("ErrorHandler已初始化");
            }
        } catch (Exception e) {
            // Bug #449: 添加异常处理
            System.err.println("ErrorHandler.initialize失败: " + e.getMessage());
            logger = null;
        }
    }

    /**
     * Handle exceptions safely with logging
     */
    @SuppressWarnings("LoggerStringConcat")
    public static void handleException(Logger fallbackLogger, String context, Exception e) {
        // Bug #450: 验证参数
        if (context == null) {
            context = "Unknown";
        }

        if (e == null) {
            if (logger != null) {
                logger.warning("handleException called with null exception");
            }
            return;
        }

        try {
            Logger activeLogger = logger != null ? logger : fallbackLogger;

            if (activeLogger != null) {
                String errorKey = context + ":" + e.getClass().getSimpleName();

                if (shouldLogError(errorKey)) {
                    activeLogger.severe("Error in " + context + ": " + e.getMessage());

                    // Log stack trace for debugging
                    // Bug #451: 添加null检查
                    if (e.getCause() != null) {
                        String causeMsg = e.getCause().getMessage();
                        activeLogger.severe("Caused by: " + (causeMsg != null ? causeMsg : "Unknown cause"));
                    }

                    // Log full stack trace for critical errors
                    String stackTrace = getStackTrace(e);
                    if (stackTrace != null && !stackTrace.isEmpty()) {
                        activeLogger.severe("Stack trace: " + stackTrace);
                    }

                    incrementErrorCount(errorKey);
                }
            }

            // Print to console as fallback
            System.err.println("EnchAdd Error in " + context + ": " + e.getMessage());
        } catch (Exception ex) {
            // Bug #452: 防止错误处理本身出错
            System.err.println("Error in error handler: " + ex.getMessage());
        }
    }

    /**
     * Handle exceptions with custom message
     */
    public static void handleException(Logger fallbackLogger, String context, String customMessage, Exception e) {
        // Bug #453: 验证参数
        if (context == null) context = "Unknown";
        if (customMessage == null) customMessage = "Error occurred";
        if (e == null) {
            if (logger != null) {
                logger.warning("handleException called with null exception");
            }
            return;
        }

        try {
            Logger activeLogger = logger != null ? logger : fallbackLogger;

            if (activeLogger != null) {
                String errorKey = context + ":" + e.getClass().getSimpleName();

                if (shouldLogError(errorKey)) {
                    activeLogger.severe(customMessage + " (Context: " + context + ")");
                    String eMsg = e.getMessage();
                    activeLogger.severe("Technical details: " + (eMsg != null ? eMsg : "No message"));

                    String stackTrace = getStackTrace(e);
                    if (stackTrace != null && !stackTrace.isEmpty()) {
                        activeLogger.severe("Stack trace: " + stackTrace);
                    }

                    incrementErrorCount(errorKey);
                }
            }

            // Print to console as fallback
            System.err.println("EnchAdd Error: " + customMessage);
        } catch (Exception ex) {
            // Bug #454: 防止错误处理本身出错
            System.err.println("Error in error handler: " + ex.getMessage());
        }
    }

    /**
     * Handle critical errors that should stop plugin operation
     */
    public static void handleCriticalError(String context, Throwable throwable) {
        // Bug #455: 验证参数
        if (context == null) context = "Unknown";
        if (throwable == null) {
            if (logger != null) {
                logger.warning("handleCriticalError called with null throwable");
            }
            return;
        }

        try {
            if (logger != null) {
                String errorKey = context + ":CRITICAL:" + throwable.getClass().getSimpleName();

                if (shouldLogError(errorKey)) {
                    String msg = throwable.getMessage();
                    logger.severe("CRITICAL ERROR in " + context + ": " + (msg != null ? msg : "No message"));

                    String stackTrace = getStackTrace(throwable);
                    if (stackTrace != null && !stackTrace.isEmpty()) {
                        logger.severe("Stack trace: " + stackTrace);
                    }

                    logger.severe("This error may cause plugin malfunction. Please report this issue.");

                    incrementErrorCount(errorKey);
                }
            } else {
                // Bug #456: 添加后备日志
                System.err.println("CRITICAL ERROR in " + context + ": " + throwable.getMessage());
            }
        } catch (Exception e) {
            // Bug #457: 防止错误处理本身出错
            System.err.println("Error in critical error handler: " + e.getMessage());
        }
    }

    /**
     * Handle null pointer errors with context
     */
    public static void handleNullPointer(String context, String nullField) {
        // Bug #458: 验证参数
        if (context == null) context = "Unknown";
        if (nullField == null) nullField = "Unknown field";

        try {
            if (logger != null) {
                String errorKey = "null:" + context + ":" + nullField;

                if (shouldLogError(errorKey)) {
                    logger.warning("Null pointer in " + context + ": " + nullField + " is null");
                    logger.warning("This may indicate a plugin initialization issue");

                    incrementErrorCount(errorKey);
                }
            } else {
                // Bug #459: 添加后备日志
                System.err.println("Null pointer in " + context + ": " + nullField);
            }
        } catch (Exception e) {
            // Bug #460: 防止错误处理本身出错
            System.err.println("Error in null pointer handler: " + e.getMessage());
        }
    }

    /**
     * Log warning safely
     */
    public static void logWarning(Logger fallbackLogger, String message) {
        // Bug #461: 验证message参数
        if (message == null) {
            message = "Warning: null message";
        }

        try {
            Logger activeLogger = logger != null ? logger : fallbackLogger;

            if (activeLogger != null) {
                activeLogger.warning(message);
            } else {
                System.out.println("EnchAdd Warning: " + message);
            }
        } catch (Exception e) {
            // Bug #462: 防止错误处理本身出错
            System.err.println("Error in warning logger: " + e.getMessage());
        }
    }

    /**
     * Log info safely
     */
    public static void logInfo(String context, String message) {
        // Bug #463: 验证参数
        if (context == null) context = "Unknown";
        if (message == null) message = "Info: null message";

        try {
            if (logger != null) {
                logger.info("[" + context + "] " + message);
            } else {
                // Bug #464: 添加后备日志
                System.out.println("[" + context + "] " + message);
            }
        } catch (Exception e) {
            // Bug #465: 防止错误处理本身出错
            System.err.println("Error in info logger: " + e.getMessage());
        }
    }

    /**
     * Validate input parameters
     */
    public static void validateNotNull(Object obj, String paramName) {
        // Bug #466: 验证paramName参数
        if (paramName == null) {
            paramName = "Parameter";
        }

        if (obj == null) {
            throw new IllegalArgumentException(paramName + " cannot be null");
        }
    }

    /**
     * Validate string parameters
     */
    public static void validateNotEmpty(String str, String paramName) {
        // Bug #467: 验证paramName参数
        if (paramName == null) {
            paramName = "Parameter";
        }

        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + " cannot be null or empty");
        }
    }

    /**
     * Check if error should be logged (prevents spam)
     */
    private static boolean shouldLogError(String errorKey) {
        // Bug #468: 验证errorKey参数
        if (errorKey == null || errorKey.isEmpty()) {
            return true; // 允许记录未知错误
        }

        try {
            long currentTime = System.currentTimeMillis();

            // Check if we've hit the error limit
            AtomicLong count = errorCounts.get(errorKey);
            if (count != null && count.get() >= MAX_ERROR_COUNT) {
                return false;
            }

            // Check cooldown
            Long lastTime = lastErrorTime.get(errorKey);
            if (lastTime != null && (currentTime - lastTime) < ERROR_COOLDOWN_MS) {
                return false;
            }

            lastErrorTime.put(errorKey, currentTime);
            return true;
        } catch (Exception e) {
            // Bug #469: 防止检查本身出错
            return true; // 出错时允许记录
        }
    }

    /**
     * Increment error count for tracking
     */
    private static void incrementErrorCount(String errorKey) {
        // Bug #470: 验证errorKey参数
        if (errorKey == null || errorKey.isEmpty()) {
            return;
        }

        try {
            errorCounts.computeIfAbsent(errorKey, k -> new AtomicLong(0)).incrementAndGet();
        } catch (Exception e) {
            // Bug #471: 防止计数本身出错
            System.err.println("Error incrementing error count: " + e.getMessage());
        }
    }

    /**
     * Get full stack trace as string
     */
    private static String getStackTrace(Throwable throwable) {
        if (throwable == null) return "No stack trace available";

        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            String result = sw.toString();

            // Bug #472: 关闭资源
            try {
                pw.close();
                sw.close();
            } catch (Exception e) {
                // 忽略关闭错误
            }

            return result;
        } catch (Exception e) {
            return "Failed to get stack trace: " + e.getMessage();
        }
    }

    /**
     * Shutdown cleanup
     */
    public static void shutdown() {
        try {
            if (logger != null && !errorCounts.isEmpty()) {
                logger.info("Error Statistics Summary:");
                errorCounts.forEach((key, count) -> {
                    // Bug #473: 添加null检查
                    if (key != null && count != null) {
                        logger.info("  " + key + ": " + count.get() + " occurrences");
                    }
                });
            }

            // Bug #474: 安全清理
            if (errorCounts != null) {
                errorCounts.clear();
            }
            if (lastErrorTime != null) {
                lastErrorTime.clear();
            }
            logger = null;
        } catch (Exception e) {
            // Silent cleanup
            System.err.println("Error during ErrorHandler shutdown: " + e.getMessage());
        }
    }

    // Bug #475: 添加获取错误统计的方法
    public static int getErrorCount(String errorKey) {
        if (errorKey == null || errorKey.isEmpty()) {
            return 0;
        }

        try {
            AtomicLong count = errorCounts.get(errorKey);
            return count != null ? (int) count.get() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // Bug #476: 添加重置错误计数的方法
    public static void resetErrorCount(String errorKey) {
        if (errorKey == null || errorKey.isEmpty()) {
            return;
        }

        try {
            errorCounts.remove(errorKey);
            lastErrorTime.remove(errorKey);
        } catch (Exception e) {
            System.err.println("Error resetting error count: " + e.getMessage());
        }
    }

    // Bug #477: 添加清除所有错误计数的方法
    public static void clearAllErrors() {
        try {
            if (errorCounts != null) {
                errorCounts.clear();
            }
            if (lastErrorTime != null) {
                lastErrorTime.clear();
            }
        } catch (Exception e) {
            System.err.println("Error clearing all errors: " + e.getMessage());
        }
    }
}
