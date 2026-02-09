package com.enadd.core.optimize;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class PerformanceReporter {
    private static final Logger LOGGER = Logger.getLogger(PerformanceReporter.class.getName());

    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);
    private static final AtomicLong REPORT_COUNT = new AtomicLong(0);

    private static ScheduledExecutorService reportScheduler;
    private static volatile int reportIntervalSeconds = 300;
    private static volatile boolean autoGenerateReports = false;

    private static PerformanceMetrics baseline;
    private static final List<PerformanceSnapshot> snapshots = new CopyOnWriteArrayList<>();
    private static final List<OptimizationAction> appliedActions = new CopyOnWriteArrayList<>();

    private static final int MAX_SNAPSHOTS = 100;

    private PerformanceReporter() {}

    public static synchronized void initialize() {
        if (ENABLED.get()) {
            return;
        }

        reportScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PerformanceReporter-Scheduler");
            t.setDaemon(true);
            return t;
        });

        if (autoGenerateReports) {
            reportScheduler.scheduleAtFixedRate(
                PerformanceReporter::generateAndLogReport,
                reportIntervalSeconds,
                reportIntervalSeconds,
                TimeUnit.SECONDS
            );
        }

        baseline = collectMetrics();

        ENABLED.set(true);
        LOGGER.info("PerformanceReporter initialized");
    }

    public static PerformanceMetrics collectMetrics() {
        PerformanceMetrics metrics = new PerformanceMetrics();

        metrics.timestamp = System.currentTimeMillis();

        if (PerformanceMonitor.isEnabled()) {
            PerformanceMonitor.PerformanceSummary pmSummary = PerformanceMonitor.getSummary();
            metrics.cpuUsage = pmSummary.getAverageCpuUsage();
            metrics.memoryUsage = pmSummary.getAverageMemoryUsage();
            metrics.peakMemory = pmSummary.getPeakMemoryUsage();
            metrics.gcTime = pmSummary.getTotalGcTimeMs();
            metrics.eventProcessed = pmSummary.getTotalEventsProcessed();
            metrics.eventFailures = pmSummary.getTotalEventFailures();
            metrics.avgEventDuration = pmSummary.getAverageEventDurationMs();
        }

        if (AutoScaler.isEnabled()) {
            AutoScaler.ScalingReport scalingReport = AutoScaler.getScalingReport();
            if (scalingReport != null) {
                metrics.threadPoolSize = scalingReport.getTargetThreadCount();
                metrics.tickInterval = scalingReport.getTargetTickInterval();
                metrics.serverRole = scalingReport.getProfile() != null ?
                    scalingReport.getProfile().role.name() : "UNKNOWN";
                metrics.estimatedCapacity = scalingReport.getProfile() != null ?
                    scalingReport.getProfile().playerCapacity : 0;
            }
        }

        if (EventOptimizer.isEnabled()) {
            EventOptimizer.EventOptimizerReport eoReport = EventOptimizer.getReport();
            if (eoReport != null) {
                metrics.eventQueueSize = eoReport.getQueueSize();
                metrics.eventActiveThreads = eoReport.getActiveThreads();
                metrics.processedEvents = eoReport.getProcessedEvents();
                metrics.failedEvents = eoReport.getFailedEvents();
                metrics.avgEventProcessingTime = eoReport.getAvgProcessingTimeMs();
                metrics.batchSize = eoReport.getBatchSize();
            }
        }

        if (CacheOptimizer.isEnabled()) {
            CacheOptimizer.CacheOptimizerReport coReport = CacheOptimizer.getReport();
            if (coReport != null) {
                metrics.cacheEntries = coReport.getTotalEntries();
                metrics.cacheMemoryKB = coReport.getTotalMemoryBytes() / 1024;
                metrics.cacheHits = coReport.getTotalHits();
                metrics.cacheMisses = coReport.getTotalMisses();
                metrics.cacheHitRate = coReport.getHitRate();
                metrics.cacheEvictions = coReport.getTotalEvictions();
            }
        }

        if (ServiceOptimizer.isEnabled()) {
            ServiceOptimizer.ServiceOptimizerReport soReport = ServiceOptimizer.getReport();
            if (soReport != null) {
                metrics.totalServices = soReport.getTotalServices();
                metrics.activeServices = soReport.getActiveServices();
                metrics.healthyServices = soReport.getHealthyServices();
                metrics.avgServiceCpu = soReport.getAvgCpuUsage();
                metrics.avgServiceMemory = soReport.getAvgMemoryUsage();
                metrics.avgServiceLatency = soReport.getAvgLatency();
                metrics.redundancyIssues = soReport.getRedundancies().size();
            }
        }

        return metrics;
    }

    public static PerformanceReport generateReport() {
        PerformanceMetrics current = collectMetrics();
        PerformanceMetrics previous = snapshots.isEmpty() ? baseline : snapshots.get(snapshots.size() - 1).metrics;

        PerformanceReport report = new PerformanceReport();
        report.timestamp = System.currentTimeMillis();
        report.count = REPORT_COUNT.incrementAndGet();
        report.current = current;
        report.previous = previous;
        report.baseline = baseline;
        report.changes = calculateChanges(previous, current);
        report.recommendations = generateRecommendations(current);
        report.overallScore = calculateOverallScore(current);

        snapshots.add(new PerformanceSnapshot(current, report.overallScore));
        while (snapshots.size() > MAX_SNAPSHOTS) {
            snapshots.remove(0);
        }

        return report;
    }

    public static void generateAndLogReport() {
        if (!ENABLED.get()) return;

        PerformanceReport report = generateReport();
        LOGGER.log(Level.INFO, "=== Performance Report #{0} ===", report.count);
        LOGGER.log(Level.INFO, "Overall Score: {0}/100", String.format("%.1f", report.overallScore));
        LOGGER.log(Level.INFO, "CPU Usage: {0}%{1}", new Object[]{String.format("%.1f", report.current.cpuUsage), formatChange(report.changes.cpuUsageChange)});
        LOGGER.log(Level.INFO, "Memory Usage: {0}%{1}", new Object[]{String.format("%.1f", report.current.memoryUsage), formatChange(report.changes.memoryUsageChange)});
        LOGGER.log(Level.INFO, "Cache Hit Rate: {0}%{1}", new Object[]{String.format("%.1f", report.current.cacheHitRate), formatChange(report.changes.cacheHitRateChange)});
        LOGGER.log(Level.INFO, "Event Failures: {0}{1}", new Object[]{report.current.eventFailures, formatChange(report.changes.eventFailureChange)});

        if (!report.recommendations.isEmpty()) {
            LOGGER.info("Recommendations:");
            report.recommendations.forEach(rec ->
                LOGGER.log(Level.INFO, "  [{0}] {1}", new Object[]{rec.priority, rec.description}));
        }
    }

    private static PerformanceMetrics.Delta calculateChanges(PerformanceMetrics previous, PerformanceMetrics current) {
        PerformanceMetrics.Delta delta = new PerformanceMetrics.Delta();

        delta.cpuUsageChange = current.cpuUsage - previous.cpuUsage;
        delta.memoryUsageChange = current.memoryUsage - previous.memoryUsage;
        delta.cacheHitRateChange = current.cacheHitRate - previous.cacheHitRate;
        delta.eventFailureChange = current.eventFailures - previous.eventFailures;
        delta.avgEventProcessingTimeChange = current.avgEventProcessingTime - previous.avgEventProcessingTime;
        delta.serviceLatencyChange = current.avgServiceLatency - previous.avgServiceLatency;

        return delta;
    }

    private static String formatChange(double change) {
        if (change > 0) {
            return " (+" + String.format("%.1f", change) + ")";
        } else if (change < 0) {
            return " (" + String.format("%.1f", change) + ")";
        }
        return "";
    }

    private static List<Recommendation> generateRecommendations(PerformanceMetrics metrics) {
        List<Recommendation> recommendations = new ArrayList<>();

        if (metrics.cpuUsage > 80) {
            recommendations.add(new Recommendation(
                "HIGH",
                "CPU使用率过高 (" + String.format("%.1f%%", metrics.cpuUsage) + ")",
                "建议: 减少并发线程数、降低tick率或优化CPU密集型任务"
            ));
        } else if (metrics.cpuUsage > 60) {
            recommendations.add(new Recommendation(
                "MEDIUM",
                "CPU使用率偏高 (" + String.format("%.1f%%", metrics.cpuUsage) + ")",
                "建议: 监控趋势，考虑优化性能"
            ));
        }

        if (metrics.memoryUsage > 85) {
            recommendations.add(new Recommendation(
                "HIGH",
                "内存使用率过高 (" + String.format("%.1f%%", metrics.memoryUsage) + ")",
                "建议: 增加缓存淘汰频率、减少内存占用或扩展内存"
            ));
        }

        if (metrics.cacheHitRate < 70 && metrics.cacheMisses > 1000) {
            recommendations.add(new Recommendation(
                "MEDIUM",
                "缓存命中率偏低 (" + String.format("%.1f%%", metrics.cacheHitRate) + ")",
                "建议: 增加缓存预热、优化缓存键或调整缓存大小"
            ));
        }

        if (metrics.eventFailures > 100) {
            recommendations.add(new Recommendation(
                "HIGH",
                "事件处理失败数较高 (" + metrics.eventFailures + ")",
                "建议: 检查错误日志、优化事件处理逻辑或增加重试机制"
            ));
        }

        if (metrics.avgEventProcessingTime > 50) {
            recommendations.add(new Recommendation(
                "MEDIUM",
                "事件处理延迟偏高 (" + String.format("%.1fms", metrics.avgEventProcessingTime) + ")",
                "建议: 优化批量处理、增加线程或使用异步处理"
            ));
        }

        if (metrics.redundancyIssues > 0) {
            recommendations.add(new Recommendation(
                "LOW",
                "发现 " + metrics.redundancyIssues + " 个冗余服务",
                "建议: 评估服务合并可能性，减少资源浪费"
            ));
        }

        if (metrics.serviceLatency > 500) {
            recommendations.add(new Recommendation(
                "MEDIUM",
                "服务延迟偏高 (" + metrics.avgServiceLatency + "ms)",
                "建议: 优化服务依赖、检查性能瓶颈"
            ));
        }

        if (metrics.gcTime > 5000) {
            recommendations.add(new Recommendation(
                "MEDIUM",
                "GC停顿时间较长 (" + metrics.gcTime + "ms)",
                "建议: 调整堆大小、优化对象分配或使用G1GC"
            ));
        }

        Collections.sort(recommendations, (a, b) -> {
            int priorityOrder = Arrays.asList("HIGH", "MEDIUM", "LOW").indexOf(a.priority) -
                               Arrays.asList("HIGH", "MEDIUM", "LOW").indexOf(b.priority);
            return priorityOrder != 0 ? priorityOrder : a.description.compareTo(b.description);
        });

        return recommendations;
    }

    private static double calculateOverallScore(PerformanceMetrics metrics) {
        double score = 100.0;

        score -= Math.max(0, (metrics.cpuUsage - 50) / 2);

        score -= Math.max(0, (metrics.memoryUsage - 70) / 3);

        double cachePenalty = Math.max(0, 80 - metrics.cacheHitRate);
        score -= cachePenalty * 0.3;

        if (metrics.cacheMisses > 10000) {
            score -= 5;
        }

        score -= Math.min(20, metrics.eventFailures / 10);

        score -= Math.max(0, (metrics.avgEventProcessingTime - 20) / 5);

        score -= Math.min(15, metrics.redundancyIssues * 3);

        score -= Math.max(0, (metrics.serviceLatency - 200) / 100) * 5;

        score -= Math.max(0, (metrics.avgServiceCpu - 30) / 5);

        return Math.max(0, Math.min(100, score));
    }

    public static void recordOptimizationAction(String action, String description) {
        appliedActions.add(new OptimizationAction(
            System.currentTimeMillis(),
            action,
            description
        ));

        LOGGER.info("Optimization action recorded: " + action + " - " + description);
    }

    public static void setReportInterval(int seconds) {
        reportIntervalSeconds = Math.max(60, Math.min(3600, seconds));

        if (autoGenerateReports && reportScheduler != null && !reportScheduler.isShutdown()) {
            reportScheduler.shutdown();
            try {
                if (!reportScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    reportScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                reportScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }

            reportScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "PerformanceReporter-Scheduler");
                t.setDaemon(true);
                return t;
            });

            reportScheduler.scheduleAtFixedRate(
                PerformanceReporter::generateAndLogReport,
                reportIntervalSeconds,
                reportIntervalSeconds,
                TimeUnit.SECONDS
            );
        }
    }

    public static void setAutoGenerateReports(boolean enabled) {
        autoGenerateReports = enabled;
    }

    public static void setBaseline() {
        baseline = collectMetrics();
        LOGGER.info("Performance baseline reset");
    }

    public static PerformanceSnapshot getBaselineSnapshot() {
        return new PerformanceSnapshot(baseline, calculateOverallScore(baseline));
    }

    public static List<PerformanceSnapshot> getSnapshots() {
        return new ArrayList<>(snapshots);
    }

    public static List<OptimizationAction> getAppliedActions() {
        return new ArrayList<>(appliedActions);
    }

    public static PerformanceMetrics getCurrentMetrics() {
        return collectMetrics();
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static synchronized void shutdown() {
        if (!ENABLED.get()) return;

        ENABLED.set(false);

        if (reportScheduler != null && !reportScheduler.isShutdown()) {
            reportScheduler.shutdown();
            try {
                if (!reportScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    reportScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                reportScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        snapshots.clear();
        appliedActions.clear();

        LOGGER.info("PerformanceReporter shutdown complete");
    }

    public static final class PerformanceReport {
        private long timestamp;
        private long count;
        private PerformanceMetrics current;
        private PerformanceMetrics previous;
        private PerformanceMetrics baseline;
        private PerformanceMetrics.Delta changes;
        private List<Recommendation> recommendations;
        private double overallScore;

        public long getTimestamp() { return timestamp; }
        public long getCount() { return count; }
        public PerformanceMetrics getCurrent() { return current; }
        public PerformanceMetrics getPrevious() { return previous; }
        public PerformanceMetrics getBaseline() { return baseline; }
        public PerformanceMetrics.Delta getChanges() { return changes; }
        public List<Recommendation> getRecommendations() { return recommendations; }
        public double getOverallScore() { return overallScore; }

            @Override
    public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Performance Report #").append(count).append(" ===\n");
            sb.append("Timestamp: ").append(new Date(timestamp)).append("\n");
            sb.append("Overall Score: ").append(String.format("%.1f", overallScore)).append("/100\n");
            sb.append("\n--- Current Metrics ---\n");
            sb.append("CPU Usage: ").append(String.format("%.1f%%", current.cpuUsage)).append("\n");
            sb.append("Memory Usage: ").append(String.format("%.1f%%", current.memoryUsage)).append("\n");
            sb.append("Cache Hit Rate: ").append(String.format("%.1f%%", current.cacheHitRate)).append("\n");
            sb.append("Processed Events: ").append(current.eventProcessed).append("\n");
            sb.append("Event Failures: ").append(current.eventFailures).append("\n");
            sb.append("\n--- Changes from Previous ---\n");
            sb.append("CPU: ").append(formatChange(changes.cpuUsageChange)).append("\n");
            sb.append("Memory: ").append(formatChange(changes.memoryUsageChange)).append("\n");
            sb.append("Cache Hit Rate: ").append(formatChange(changes.cacheHitRateChange)).append("\n");
            sb.append("\n--- Recommendations ---\n");
            recommendations.forEach(rec ->
                sb.append("[").append(rec.priority).append("] ").append(rec.description)
                  .append(": ").append(rec.action).append("\n"));
            return sb.toString();
        }
    }

    public static final class PerformanceSnapshot {
        private final long timestamp;
        private final PerformanceMetrics metrics;
        private final double score;

        public PerformanceSnapshot(PerformanceMetrics metrics, double score) {
            this.timestamp = System.currentTimeMillis();
            this.metrics = metrics;
            this.score = score;
        }

        public long getTimestamp() { return timestamp; }
        public PerformanceMetrics getMetrics() { return metrics; }
        public double getScore() { return score; }
    }

    public static final class OptimizationAction {
        private final long timestamp;
        private final String action;
        private final String description;

        public OptimizationAction(long timestamp, String action, String description) {
            this.timestamp = timestamp;
            this.action = action;
            this.description = description;
        }

        public long getTimestamp() { return timestamp; }
        public String getAction() { return action; }
        public String getDescription() { return description; }

            @Override
    public String toString() {
            return "[" + new Date(timestamp) + "] " + action + ": " + description;
        }
    }

    public static final class Recommendation {
        private final String priority;
        private final String description;
        private final String action;

        public Recommendation(String priority, String description, String action) {
            this.priority = priority;
            this.description = description;
            this.action = action;
        }

        public String getPriority() { return priority; }
        public String getDescription() { return description; }
        public String getAction() { return action; }
    }

    public static class PerformanceMetrics {
        public long timestamp;
        public double cpuUsage;
        public double memoryUsage;
        public long peakMemory;
        public long gcTime;
        public long eventProcessed;
        public long eventFailures;
        public double avgEventDuration;
        public int threadPoolSize;
        public int tickInterval;
        public String serverRole;
        public int estimatedCapacity;
        public int eventQueueSize;
        public int eventActiveThreads;
        public long processedEvents;
        public long failedEvents;
        public double avgEventProcessingTime;
        public int batchSize;
        public long cacheEntries;
        public long cacheMemoryKB;
        public long cacheHits;
        public long cacheMisses;
        public double cacheHitRate;
        public long cacheEvictions;
        public int totalServices;
        public int activeServices;
        public int healthyServices;
        public double avgServiceCpu;
        public long avgServiceMemory;
        public long avgServiceLatency;
        public int redundancyIssues;
        public double overallScore;
        public long serviceLatency;

        public static class Delta {
            public double cpuUsageChange;
            public double memoryUsageChange;
            public double cacheHitRateChange;
            public long eventFailureChange;
            public double avgEventProcessingTimeChange;
            public long serviceLatencyChange;
        }
    }
}
