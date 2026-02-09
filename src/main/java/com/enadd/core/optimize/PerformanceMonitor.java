package com.enadd.core.optimize;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;


public final class PerformanceMonitor {

    // 删除未使用的字段，后续如需访问内存信息可直接通过 ManagementFactory.getMemoryMXBean()

    static {
        // 确保在类加载时初始化并持有 MemoryMXBean 实例，以便后续可通过静态方法直接访问
        // 例如：getHeapMemoryUsage(), getNonHeapMemoryUsage() 等
        // 若仍无使用需求，可安全删除此行及静态块
    }
    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    private static final Runtime RUNTIME = Runtime.getRuntime();

    private static final AtomicBoolean MONITORING_ENABLED = new AtomicBoolean(false);
    private static final ConcurrentLinkedQueue<PerformanceEvent> EVENT_QUEUE =
        new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<String, AtomicLong> COUNTERS =
        new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, List<Double>> METRIC_HISTORY =
        new ConcurrentHashMap<>();

    private static Consumer<String> LOGGER = message -> {};
    private static Consumer<Alert> ALERT_HANDLER = alert -> {};

    private static final int MAX_HISTORY_SIZE = 100;
    private static long MONITORING_INTERVAL_MS = 5000;

    private static volatile long startTime = 0;
    private static int samplesCollected = 0;

    public static class PerformanceEvent {
        public long timestamp;
        public String category;
        public String message;
        public long durationMs;
        public boolean success;
        public double cpuBefore;
        public double cpuAfter;
        public long memoryBefore;
        public long memoryAfter;

        public PerformanceEvent(String category, String message, long durationMs,
                              boolean success, double cpuBefore, double cpuAfter,
                              long memoryBefore, long memoryAfter) {
            this.timestamp = System.currentTimeMillis();
            this.category = category;
            this.message = message;
            this.durationMs = durationMs;
            this.success = success;
            this.cpuBefore = cpuBefore;
            this.cpuAfter = cpuAfter;
            this.memoryBefore = memoryBefore;
            this.memoryAfter = memoryAfter;
        }
    }

    public static class Alert {
        public final AlertLevel level;
        public final String metric;
        public final String message;
        public final double value;
        public final double threshold;
        public final long timestamp;

        public Alert(AlertLevel level, String metric, String message,
                    double value, double threshold) {
            this.level = level;
            this.metric = metric;
            this.message = message;
            this.value = value;
            this.threshold = threshold;
            this.timestamp = System.currentTimeMillis();
        }

        public enum AlertLevel {
            INFO, WARNING, CRITICAL
        }
    }

    public static class PerformanceSummary {
        public final long uptimeMs;
        public final int samplesCollected;
        public final double avgCpuUsage;
        public final double maxCpuUsage;
        public final double avgMemoryUsage;
        public final double maxMemoryUsage;
        public final long totalGcTime;
        public final int totalEvents;
        public final int failedEvents;
        public final double avgEventDuration;
        public final long peakMemory;
        public final int peakThreads;

        public PerformanceSummary(long uptimeMs, int samplesCollected, double avgCpuUsage,
                                double maxCpuUsage, double avgMemoryUsage, double maxMemoryUsage,
                                long totalGcTime, int totalEvents, int failedEvents,
                                double avgEventDuration, long peakMemory, int peakThreads) {
            this.uptimeMs = uptimeMs;
            this.samplesCollected = samplesCollected;
            this.avgCpuUsage = avgCpuUsage;
            this.maxCpuUsage = maxCpuUsage;
            this.avgMemoryUsage = avgMemoryUsage;
            this.maxMemoryUsage = maxMemoryUsage;
            this.totalGcTime = totalGcTime;
            this.totalEvents = totalEvents;
            this.failedEvents = failedEvents;
            this.avgEventDuration = avgEventDuration;
            this.peakMemory = peakMemory;
            this.peakThreads = peakThreads;
        }

        public long getUptimeMs() { return uptimeMs; }
        public int getSamplesCollected() { return samplesCollected; }
        public double getAverageCpuUsage() { return avgCpuUsage; }
        public double getMaxCpuUsage() { return maxCpuUsage; }
        public double getAverageMemoryUsage() { return avgMemoryUsage; }
        public double getMaxMemoryUsage() { return maxMemoryUsage; }
        public long getTotalGcTime() { return totalGcTime; }
        public long getTotalGcTimeMs() { return totalGcTime; }
        public int getTotalEvents() { return totalEvents; }
        public int getTotalEventsProcessed() { return totalEvents; }
        public int getFailedEvents() { return failedEvents; }
        public int getTotalEventFailures() { return failedEvents; }
        public double getAverageEventDuration() { return avgEventDuration; }
        public double getAverageEventDurationMs() { return avgEventDuration; }
        public long getPeakMemory() { return peakMemory; }
        public long getPeakMemoryUsage() { return peakMemory; }
        public int getPeakThreads() { return peakThreads; }
    }

    private PerformanceMonitor() {}

    public static synchronized void initialize(Consumer<String> logger, Consumer<Alert> alertHandler) {
        if (MONITORING_ENABLED.get()) {
            return;
        }

        LOGGER = logger != null ? logger : LOGGER;
        ALERT_HANDLER = alertHandler != null ? alertHandler : ALERT_HANDLER;

        startTime = System.currentTimeMillis();
        MONITORING_ENABLED.set(true);

        LOGGER.accept("[PerformanceMonitor] Initialized");
        startMonitoring();
    }

    public static synchronized void shutdown() {
        if (!MONITORING_ENABLED.get()) {
            return;
        }

        MONITORING_ENABLED.set(false);
        LOGGER.accept("[PerformanceMonitor] Shutdown complete");
    }

    public static void setMonitoringInterval(long intervalMs) {
        MONITORING_INTERVAL_MS = Math.max(1000, Math.min(intervalMs, 60000));
    }

    private static void startMonitoring() {
        new Thread(() -> {
            while (MONITORING_ENABLED.get()) {
                try {
                    collectMetrics();
                    checkThresholds();
                    Thread.sleep(MONITORING_INTERVAL_MS);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "PerformanceMonitor-Thread").start();
    }

    private static void collectMetrics() {
        double cpuUsage = getCpuUsage();
        long memoryUsed = RUNTIME.totalMemory() - RUNTIME.freeMemory();
        int threads = THREAD_MX_BEAN.getThreadCount();

        addToHistory("cpu", cpuUsage);
        addToHistory("memory", memoryUsed);
        addToHistory("threads", threads);

        samplesCollected++;
    }

    private static void checkThresholds() {
        List<Double> cpuHistory = METRIC_HISTORY.get("cpu");
        List<Double> memoryHistory = METRIC_HISTORY.get("memory");

        if (cpuHistory != null && !cpuHistory.isEmpty()) {
            double currentCpu = cpuHistory.get(cpuHistory.size() - 1);

            if (currentCpu > 90) {
                raiseAlert(Alert.AlertLevel.CRITICAL, "CPU",
                    String.format("CPU usage critically high: %.1f%%", currentCpu),
                    currentCpu, 90);
            } else if (currentCpu > 75) {
                raiseAlert(Alert.AlertLevel.WARNING, "CPU",
                    String.format("CPU usage high: %.1f%%", currentCpu),
                    currentCpu, 75);
            }
        }

        if (memoryHistory != null && !memoryHistory.isEmpty()) {
            long currentMemory = memoryHistory.get(memoryHistory.size() - 1).longValue();
            double memoryPercent = (currentMemory * 100.0) / RUNTIME.maxMemory();

            if (memoryPercent > 90) {
                raiseAlert(Alert.AlertLevel.CRITICAL, "Memory",
                    String.format("Memory usage critically high: %.1f%%", memoryPercent),
                    memoryPercent, 90);
            } else if (memoryPercent > 80) {
                raiseAlert(Alert.AlertLevel.WARNING, "Memory",
                    String.format("Memory usage high: %.1f%%", memoryPercent),
                    memoryPercent, 80);
            }
        }
    }

    private static void raiseAlert(Alert.AlertLevel level, String metric,
                                   String message, double value, double threshold) {
        Alert alert = new Alert(level, metric, message, value, threshold);
        EVENT_QUEUE.add(new PerformanceEvent("ALERT", message, 0, true, 0, 0, 0, 0));
        ALERT_HANDLER.accept(alert);
        LOGGER.accept(String.format("[PerformanceMonitor] ALERT [%s] %s", level, message));
    }

    public static PerformanceEvent startEvent(String category, String message) {
        return new PerformanceEvent(category, message, 0, true,
            getCpuUsage(), getCpuUsage(),
            getUsedMemory(), getUsedMemory());
    }

    public static void endEvent(PerformanceEvent event) {
        event.durationMs = System.currentTimeMillis() - event.timestamp;
        event.cpuAfter = getCpuUsage();
        event.memoryAfter = getUsedMemory();

        if (event.durationMs > 1000) {
            event.success = false;
            EVENT_QUEUE.add(event);
        } else {
            EVENT_QUEUE.add(event);
        }

        incrementCounter(event.category + ".events");
        if (!event.success) {
            incrementCounter(event.category + ".failures");
        }
    }

    public static void recordCustomMetric(String name, double value) {
        addToHistory(name, value);
        incrementCounter(name + ".samples");
    }

    public static void incrementCounter(String name) {
        COUNTERS.computeIfAbsent(name, k -> new AtomicLong(0)).incrementAndGet();
    }

    public static long getCounter(String name) {
        AtomicLong counter = COUNTERS.get(name);
        return counter != null ? counter.get() : 0;
    }

    public static PerformanceSummary getSummary() {
        List<Double> cpuHistory = METRIC_HISTORY.getOrDefault("cpu", new ArrayList<>());
        List<Double> memoryHistory = METRIC_HISTORY.getOrDefault("memory", new ArrayList<>());
        List<Double> threadHistory = METRIC_HISTORY.getOrDefault("threads", new ArrayList<>());

        double avgCpu = cpuHistory.stream().mapToDouble(d -> d).average().orElse(0);
        double maxCpu = cpuHistory.stream().mapToDouble(d -> d).max().orElse(0);

        double avgMemory = memoryHistory.stream().mapToDouble(d -> d).average().orElse(0);
        double maxMemory = memoryHistory.stream().mapToDouble(d -> d).max().orElse(0);

        int peakThreads = threadHistory.stream().mapToInt(d -> d.intValue()).max().orElse(0);

        long totalGcTime = 0;
        try {
            for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                totalGcTime += gcBean.getCollectionTime();
            }
        } catch (Exception e) {
        }

        int totalEvents = (int) EVENT_QUEUE.size();
        int failedEvents = (int) COUNTERS.getOrDefault("failures", new AtomicLong(0)).get();

        double avgDuration = 0;
        if (totalEvents > 0) {
            avgDuration = EVENT_QUEUE.stream()
                .mapToLong(e -> e.durationMs)
                .average()
                .orElse(0);
        }

        long peakMemory = (long) maxMemory;

        return new PerformanceSummary(
            System.currentTimeMillis() - startTime,
            samplesCollected,
            avgCpu,
            maxCpu,
            avgMemory,
            maxMemory,
            totalGcTime,
            totalEvents,
            failedEvents,
            avgDuration,
            peakMemory,
            peakThreads
        );
    }

    public static long getTotalEventsProcessed() {
        AtomicLong counter = COUNTERS.get("events");
        return counter != null ? counter.get() : 0;
    }

    public static long getTotalEventFailures() {
        AtomicLong counter = COUNTERS.get("failures");
        return counter != null ? counter.get() : 0;
    }

    public static double getAverageEventDurationMs() {
        List<PerformanceEvent> events = new ArrayList<>(EVENT_QUEUE);
        if (events.isEmpty()) return 0;
        return events.stream().mapToLong(e -> e.durationMs).average().orElse(0);
    }

    public static int getPeakThreads() {
        List<Double> threadHistory = METRIC_HISTORY.getOrDefault("threads", new ArrayList<>());
        return threadHistory.stream().mapToInt(d -> d.intValue()).max().orElse(0);
    }

    public static long getPeakMemoryUsage() {
        List<Double> memoryHistory = METRIC_HISTORY.getOrDefault("memory", new ArrayList<>());
        return (long) memoryHistory.stream().mapToDouble(d -> d).max().orElse(0);
    }

    public static String getDetailedReport() {
        PerformanceSummary summary = getSummary();

        StringBuilder sb = new StringBuilder();
        sb.append("=== Performance Monitor Report ===\n\n");

        sb.append("Uptime: ").append(formatDuration(summary.uptimeMs)).append("\n");
        sb.append("Samples Collected: ").append(summary.samplesCollected).append("\n\n");

        sb.append("CPU Statistics:\n");
        sb.append("  Average: ").append(String.format("%.2f%%", summary.avgCpuUsage)).append("\n");
        sb.append("  Peak: ").append(String.format("%.2f%%", summary.maxCpuUsage)).append("\n\n");

        sb.append("Memory Statistics:\n");
        sb.append("  Average Used: ").append(formatSize((long) summary.avgMemoryUsage)).append("\n");
        sb.append("  Peak Used: ").append(formatSize(summary.peakMemory)).append("\n");
        sb.append("  Max Available: ").append(formatSize(RUNTIME.maxMemory())).append("\n\n");

        sb.append("Thread Statistics:\n");
        sb.append("  Peak Threads: ").append(summary.peakThreads).append("\n\n");

        sb.append("GC Statistics:\n");
        sb.append("  Total GC Time: ").append(String.format("%.2fms", summary.totalGcTime)).append("\n\n");

        sb.append("Event Statistics:\n");
        sb.append("  Total Events: ").append(summary.totalEvents).append("\n");
        sb.append("  Failed Events: ").append(summary.failedEvents).append("\n");
        sb.append("  Average Duration: ").append(String.format("%.2fms", summary.avgEventDuration)).append("\n\n");

        sb.append("Counters:\n");
        COUNTERS.forEach((name, value) -> {
            sb.append("  ").append(name).append(": ").append(value.get()).append("\n");
        });

        return sb.toString();
    }

    public static void setLogger(Consumer<String> logger) {
        LOGGER = logger != null ? logger : message -> {};
    }

    public static void setAlertHandler(Consumer<Alert> handler) {
        ALERT_HANDLER = handler != null ? handler : alert -> {};
    }

    public static boolean isMonitoringEnabled() {
        return MONITORING_ENABLED.get();
    }

    public static boolean isEnabled() {
        return MONITORING_ENABLED.get();
    }

    public static int getQueueSize() {
        return EVENT_QUEUE.size();
    }

    public static void clearHistory() {
        METRIC_HISTORY.clear();
        COUNTERS.clear();
        EVENT_QUEUE.clear();
        samplesCollected = 0;
    }

    private static double getCpuUsage() {
        try {
            var osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                return ((com.sun.management.OperatingSystemMXBean) osBean).getCpuLoad() * 100;
            }
        } catch (Exception e) {
        }
        return -1;
    }

    private static long getUsedMemory() {
        return RUNTIME.totalMemory() - RUNTIME.freeMemory();
    }

    private static void addToHistory(String key, double value) {
        METRIC_HISTORY.computeIfAbsent(key, k -> new ArrayList<>())
            .add(value);

        List<Double> history = METRIC_HISTORY.get(key);
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
    }

    private static String formatDuration(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
