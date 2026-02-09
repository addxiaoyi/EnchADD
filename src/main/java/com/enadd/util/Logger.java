package com.enadd.util;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 分级日志系统 - 支持 DEBUG/INFO/WARN/ERROR 级别
 */
public final class Logger {

    public enum LogLevel {
        DEBUG(0),   // 调试信息
        INFO(1),    // 正常运行信息
        WARN(2),    // 警告信息
        ERROR(3);   // 错误信息

        private final int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }

        public boolean shouldLog(LogLevel minLevel) {
            return this.level >= minLevel.level;
        }
    }

    private static final class Holder {
        private static Logger INSTANCE;
    }

    private JavaPlugin plugin;
    private LogLevel minLevel = LogLevel.INFO;
    private boolean fileLoggingEnabled = true;
    private boolean consoleLoggingEnabled = true;
    private boolean asyncLogging = true;

    private File logFile;
    private PrintWriter fileWriter;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>(10000);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread logThread;

    // CPU优化: 批量日志写入
    private final List<LogEntry> batchBuffer = Collections.synchronizedList(new ArrayList<>(100));
    private final AtomicInteger batchSize = new AtomicInteger(0);
    private static final int BATCH_THRESHOLD = 50;
    private static final long BATCH_TIMEOUT = 5000;

    private Logger() {}

    public static Logger getInstance() {
        if (Holder.INSTANCE == null) {
            Holder.INSTANCE = new Logger();
        }
        return Holder.INSTANCE;
    }

    /**
     * 初始化日志系统
     */
    public void initialize(JavaPlugin plugin) {
        this.plugin = plugin;

        // 创建日志目录
        File logDir = new File(plugin.getDataFolder(), "logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        // 创建日志文件
        String fileName = "enchadd-" + fileDateFormat.format(new Date()) + ".log";
        logFile = new File(logDir, fileName);

        try {
            fileWriter = new PrintWriter(new FileWriter(logFile, true), true);
        } catch (IOException e) {
            System.err.println("无法创建日志文件: " + e.getMessage());
            fileLoggingEnabled = false;
        }

        // 启动异步日志线程
        if (asyncLogging) {
            startAsyncLogging();
        }

        info("日志系统初始化完成，最小日志级别: " + minLevel.name());
    }

    /**
     * 设置最小日志级别
     */
    public void setMinLevel(LogLevel level) {
        this.minLevel = level;
        info("日志级别已更改为: " + level.name());
    }

    /**
     * 启用/禁用文件日志
     */
    public void setFileLoggingEnabled(boolean enabled) {
        this.fileLoggingEnabled = enabled;
    }

    /**
     * 启用/禁用控制台日志
     */
    public void setConsoleLoggingEnabled(boolean enabled) {
        this.consoleLoggingEnabled = enabled;
    }

    // ==================== 日志方法 ====================

    /**
     * DEBUG级别日志
     */
    public void debug(String message) {
        log(LogLevel.DEBUG, message, null);
    }

    public void debug(String format, Object... args) {
        log(LogLevel.DEBUG, String.format(format, args), null);
    }

    /**
     * INFO级别日志
     */
    public void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    public void info(String format, Object... args) {
        log(LogLevel.INFO, String.format(format, args), null);
    }

    /**
     * WARN级别日志
     */
    public void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    public void warn(String format, Object... args) {
        log(LogLevel.WARN, String.format(format, args), null);
    }

    public void warn(String message, Throwable throwable) {
        log(LogLevel.WARN, message, throwable);
    }

    /**
     * ERROR级别日志
     */
    public void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    public void error(String format, Object... args) {
        log(LogLevel.ERROR, String.format(format, args), null);
    }

    public void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    // ==================== 核心日志方法 ====================

    private void log(LogLevel level, String message, Throwable throwable) {
        // 检查日志级别
        if (!level.shouldLog(minLevel)) {
            return;
        }

        // 构建日志条目
        LogEntry entry = new LogEntry(
            System.currentTimeMillis(),
            level,
            message,
            throwable,
            Thread.currentThread().getName()
        );

        if (asyncLogging && running.get()) {
            // CPU优化: 使用批量缓冲
            batchBuffer.add(entry);
            if (batchSize.incrementAndGet() >= BATCH_THRESHOLD) {
                flushBatch();
            }
        } else {
            // 同步写入
            writeLog(entry);
        }
    }

    private void writeLog(LogEntry entry) {
        String formattedMessage = formatLogEntry(entry);

        // 控制台输出
        if (consoleLoggingEnabled) {
            if (entry.level == LogLevel.ERROR) {
                System.err.println(formattedMessage);
            } else {
                System.out.println(formattedMessage);
            }

            if (entry.throwable != null) {
            }
        }

        // 文件输出
        if (fileLoggingEnabled && fileWriter != null) {
            fileWriter.println(formattedMessage);

            if (entry.throwable != null) {
                entry.throwable.printStackTrace(fileWriter);
            }
        }

        // Bukkit日志
        if (plugin != null) {
            switch (entry.level) {
                case DEBUG:
                    // DEBUG级别不输出到Bukkit
                    break;
                case INFO:
                    plugin.getLogger().info(entry.message);
                    break;
                case WARN:
                    plugin.getLogger().warning(entry.message);
                    break;
                case ERROR:
                    plugin.getLogger().severe(entry.message);
                    break;
            }
        }
    }

    private String formatLogEntry(LogEntry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(dateFormat.format(new Date(entry.timestamp))).append("]");
        sb.append(" [").append(entry.level.name()).append("]");
        sb.append(" [").append(entry.threadName).append("]");
        sb.append(" ").append(entry.message);
        return sb.toString();
    }

    // ==================== 异步日志 ====================

    private void startAsyncLogging() {
        if (running.get()) {
            return;
        }

        running.set(true);
        logThread = new Thread(() -> {
            long lastFlush = System.currentTimeMillis();

            while (running.get() || !logQueue.isEmpty() || !batchBuffer.isEmpty()) {
                try {
                    // 处理队列中的日志
                    LogEntry entry = logQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (entry != null) {
                        batchBuffer.add(entry);
                        batchSize.incrementAndGet();
                    }

                    // CPU优化: 定时刷新或达到阈值
                    long now = System.currentTimeMillis();
                    if (batchSize.get() >= BATCH_THRESHOLD ||
                        (now - lastFlush) >= BATCH_TIMEOUT) {
                        flushBatch();
                        lastFlush = now;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in log thread: " + e.getMessage());
                }
            }

            // 最后刷新
            flushBatch();
        }, "Logger-Thread");

        logThread.setDaemon(true);
        logThread.setPriority(Thread.MIN_PRIORITY); // CPU优化: 最低优先级
        logThread.start();
    }

    // CPU优化: 批量刷新方法
    private void flushBatch() {
        synchronized (batchBuffer) {
            if (batchBuffer.isEmpty()) {
                return;
            }

            for (LogEntry entry : batchBuffer) {
                writeLog(entry);
            }

            batchBuffer.clear();
            batchSize.set(0);

            if (fileWriter != null) {
                fileWriter.flush();
            }
        }
    }

    // ==================== 关闭 ====================

    public void shutdown() {
        running.set(false);

        // CPU优化: 最后刷新批量缓冲
        flushBatch();

        // 处理剩余日志
        while (!logQueue.isEmpty()) {
            LogEntry entry = logQueue.poll();
            if (entry != null) {
                writeLog(entry);
            }
        }

        // 关闭文件
        if (fileWriter != null) {
            fileWriter.flush();
            fileWriter.close();
            fileWriter = null;
        }

        // 等待日志线程结束
        if (logThread != null) {
            try {
                logThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ==================== 数据类 ====================

    private static class LogEntry {
        final long timestamp;
        final LogLevel level;
        final String message;
        final Throwable throwable;
        final String threadName;

        LogEntry(long timestamp, LogLevel level, String message, Throwable throwable, String threadName) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
            this.throwable = throwable;
            this.threadName = threadName;
        }
    }
}
