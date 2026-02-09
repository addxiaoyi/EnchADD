package com.enadd.core.optimize;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.*;
import java.util.*;
import java.util.logging.Logger;
import com.enadd.core.optimize.PerformanceReporter.PerformanceMetrics;
import com.enadd.core.optimize.PerformanceReporter.PerformanceSnapshot;


public final class OptimizationVerifier {
    private static final Logger LOGGER = Logger.getLogger(OptimizationVerifier.class.getName());

    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);
    private static final AtomicInteger VERIFICATION_COUNT = new AtomicInteger(0);

    private static ScheduledExecutorService verificationScheduler;
    private static volatile int verificationIntervalSeconds = 600;
    private static volatile boolean autoVerify = false;

    private static PerformanceMetrics preOptimizationBaseline;
    private static final List<VerificationResult> verificationHistory = new CopyOnWriteArrayList<>();

    private static final int WARMUP_ITERATIONS = 3;
    private static final int BENCHMARK_ITERATIONS = 10;

    private static final Map<String, BenchmarkResult> benchmarkResults = new ConcurrentHashMap<>();

    private static final Map<String, Double> optimizationGoals = new ConcurrentHashMap<>();

    private OptimizationVerifier() {}

    public static synchronized void initialize() {
        if (ENABLED.get()) {
            return;
        }

        registerDefaultGoals();

        verificationScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "OptimizationVerifier-Scheduler");
            t.setDaemon(true);
            return t;
        });

        if (autoVerify) {
            verificationScheduler.scheduleAtFixedRate(
                OptimizationVerifier::performAutoVerification,
                verificationIntervalSeconds,
                verificationIntervalSeconds,
                TimeUnit.SECONDS
            );
        }

        ENABLED.set(true);
        LOGGER.info("OptimizationVerifier initialized");
    }

    private static void registerDefaultGoals() {
        optimizationGoals.put("cpu_usage_reduction", 20.0);
        optimizationGoals.put("memory_usage_reduction", 15.0);
        optimizationGoals.put("cache_hit_rate_minimum", 75.0);
        optimizationGoals.put("event_failure_reduction", 50.0);
        optimizationGoals.put("latency_reduction", 25.0);
        optimizationGoals.put("throughput_increase", 30.0);
    }

    public static VerificationResult captureBaseline(String name) {
        preOptimizationBaseline = PerformanceReporter.collectMetrics();

        VerificationResult result = new VerificationResult();
        result.name = name;
        result.timestamp = System.currentTimeMillis();
        result.baseline = preOptimizationBaseline;
        result.current = preOptimizationBaseline;
        result.status = "BASELINE_CAPTURED";

        verificationHistory.add(result);
        LOGGER.info("Optimization baseline captured: " + name);

        return result;
    }

    public static VerificationResult verifyOptimizations(String name) {
        if (preOptimizationBaseline == null) {
            LOGGER.warning("No baseline captured, capturing current state");
            captureBaseline(name);
        }

        PerformanceMetrics current = PerformanceReporter.collectMetrics();

        VerificationResult result = new VerificationResult();
        result.name = name;
        result.timestamp = System.currentTimeMillis();
        result.baseline = preOptimizationBaseline;
        result.current = current;
        result.changes = calculateOptimizations(preOptimizationBaseline, current);
        result.goalAchievements = checkGoalAchievements(current);
        result.status = determineStatus(result.goalAchievements);

        verificationHistory.add(result);
        VERIFICATION_COUNT.incrementAndGet();

        LOGGER.info("Verification completed: " + result.status);
        LOGGER.info("CPU improvement: " + String.format("%.1f%%", result.changes.cpuImprovement));
        LOGGER.info("Memory improvement: " + String.format("%.1f%%", result.changes.memoryImprovement));
        LOGGER.info("Cache hit rate: " + String.format("%.1f%%", result.changes.cacheHitRateImprovement));

        return result;
    }

    private static VerificationResult.OptimizationChanges calculateOptimizations(
            PerformanceMetrics baseline, PerformanceMetrics current) {
        VerificationResult.OptimizationChanges changes = new VerificationResult.OptimizationChanges();

        if (baseline.cpuUsage > 0) {
            changes.cpuImprovement = (baseline.cpuUsage - current.cpuUsage) / baseline.cpuUsage * 100;
        }

        if (baseline.memoryUsage > 0) {
            changes.memoryImprovement = (baseline.memoryUsage - current.memoryUsage) / baseline.memoryUsage * 100;
        }

        if (baseline.cacheHitRate > 0) {
            changes.cacheHitRateImprovement = current.cacheHitRate - baseline.cacheHitRate;
        }

        changes.eventFailureReduction = baseline.eventFailures - current.eventFailures;

        if (baseline.avgEventProcessingTime > 0) {
            changes.latencyReduction = (baseline.avgEventProcessingTime - current.avgEventProcessingTime) /
                                      baseline.avgEventProcessingTime * 100;
        }

        changes.cacheHitRateCurrent = current.cacheHitRate;
        changes.eventFailuresCurrent = current.eventFailures;
        changes.latencyCurrent = current.avgEventProcessingTime;

        return changes;
    }

    private static Map<String, Boolean> checkGoalAchievements(PerformanceMetrics current) {
        Map<String, Boolean> achievements = new LinkedHashMap<>();

        achievements.put("cpu_usage_reduction",
            optimizationGoals.getOrDefault("cpu_usage_reduction", 20.0) <= 0 ||
            (preOptimizationBaseline.cpuUsage - current.cpuUsage) / preOptimizationBaseline.cpuUsage * 100 >=
            optimizationGoals.getOrDefault("cpu_usage_reduction", 20.0));

        achievements.put("memory_usage_reduction",
            optimizationGoals.getOrDefault("memory_usage_reduction", 15.0) <= 0 ||
            (preOptimizationBaseline.memoryUsage - current.memoryUsage) / preOptimizationBaseline.memoryUsage * 100 >=
            optimizationGoals.getOrDefault("memory_usage_reduction", 15.0));

        achievements.put("cache_hit_rate_minimum",
            current.cacheHitRate >= optimizationGoals.getOrDefault("cache_hit_rate_minimum", 75.0));

        achievements.put("event_failure_reduction",
            optimizationGoals.getOrDefault("event_failure_reduction", 50.0) <= 0 ||
            (preOptimizationBaseline.eventFailures - current.eventFailures) /
            Math.max(1, preOptimizationBaseline.eventFailures) * 100 >=
            optimizationGoals.getOrDefault("event_failure_reduction", 50.0));

        achievements.put("latency_reduction",
            optimizationGoals.getOrDefault("latency_reduction", 25.0) <= 0 ||
            (preOptimizationBaseline.avgEventProcessingTime - current.avgEventProcessingTime) /
            Math.max(1, preOptimizationBaseline.avgEventProcessingTime) * 100 >=
            optimizationGoals.getOrDefault("latency_reduction", 25.0));

        return achievements;
    }

    private static String determineStatus(Map<String, Boolean> achievements) {
        long passed = achievements.values().stream().filter(v -> v).count();
        long total = achievements.size();

        if (passed == total) {
            return "ALL_GOALS_MET";
        } else if (passed >= total * 0.7) {
            return "MOST_GOALS_MET";
        } else if (passed >= total * 0.5) {
            return "PARTIAL_SUCCESS";
        } else {
            return "NEEDS_IMPROVEMENT";
        }
    }

    public static BenchmarkResult runBenchmark(String name, Runnable benchmark) {
        LOGGER.info("Starting benchmark: " + name);

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            benchmark.run();
        }

        List<Long> times = new ArrayList<>();

        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.nanoTime();
            benchmark.run();
            long end = System.nanoTime();
            times.add(end - start);
        }

        double avgTimeNs = times.stream().mapToLong(Long::longValue).average().orElse(0);
        double minTimeNs = times.stream().mapToLong(Long::longValue).min().orElse(0);
        double maxTimeNs = times.stream().mapToLong(Long::longValue).max().orElse(0);
        double stdDev = calculateStdDev(times, avgTimeNs);

        BenchmarkResult result = new BenchmarkResult();
        result.name = name;
        result.timestamp = System.currentTimeMillis();
        result.avgTimeMs = avgTimeNs / 1_000_000.0;
        result.minTimeMs = minTimeNs / 1_000_000.0;
        result.maxTimeMs = maxTimeNs / 1_000_000.0;
        result.stdDevMs = stdDev / 1_000_000.0;
        result.iterations = BENCHMARK_ITERATIONS;

        benchmarkResults.put(name, result);

        LOGGER.info("Benchmark '" + name + "' completed: avg=" +
                    String.format("%.3fms", result.avgTimeMs) +
                    ", stdDev=" + String.format("%.3fms", result.stdDevMs));

        return result;
    }

    private static double calculateStdDev(List<Long> values, double mean) {
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        return Math.sqrt(variance);
    }

    public static BenchmarkComparison compareBenchmarks(String beforeName, String afterName) {
        BenchmarkResult before = benchmarkResults.get(beforeName);
        BenchmarkResult after = benchmarkResults.get(afterName);

        if (before == null || after == null) {
            LOGGER.warning("Benchmark results not found for comparison");
            return null;
        }

        BenchmarkComparison comparison = new BenchmarkComparison();
        comparison.beforeName = beforeName;
        comparison.afterName = afterName;
        comparison.timestamp = System.currentTimeMillis();

        double improvement = (before.avgTimeMs - after.avgTimeMs) / before.avgTimeMs * 100;
        comparison.improvementPercent = improvement;
        comparison.beforeAvgMs = before.avgTimeMs;
        comparison.afterAvgMs = after.avgTimeMs;
        comparison.isImproved = improvement > 0;

        LOGGER.info("Benchmark comparison: " + beforeName + " vs " + afterName +
                    ": " + String.format("%.1f%%", improvement) + " improvement");

        return comparison;
    }

    public static HealthCheckResult performHealthCheck() {
        HealthCheckResult result = new HealthCheckResult();
        result.timestamp = System.currentTimeMillis();
        result.checks = new LinkedHashMap<>();

        result.checks.put("ServerAnalyzer", checkComponent("ServerAnalyzer",
            ServerAnalyzer.isEnabled()));

        result.checks.put("PerformanceMonitor", checkComponent("PerformanceMonitor",
            PerformanceMonitor.isEnabled()));

        result.checks.put("AutoScaler", checkComponent("AutoScaler",
            AutoScaler.isEnabled()));

        result.checks.put("EventOptimizer", checkComponent("EventOptimizer",
            EventOptimizer.isEnabled()));

        result.checks.put("CacheOptimizer", checkComponent("CacheOptimizer",
            CacheOptimizer.isEnabled()));

        result.checks.put("ServiceOptimizer", checkComponent("ServiceOptimizer",
            ServiceOptimizer.isEnabled()));

        result.checks.put("PerformanceReporter", checkComponent("PerformanceReporter",
            PerformanceReporter.isEnabled()));

        result.passed = result.checks.values().stream().allMatch(r -> r.passed);
        result.passRate = (double) result.checks.values().stream()
            .filter(r -> r.passed).count() / result.checks.size() * 100;

        if (!result.passed) {
            LOGGER.warning("Health check found issues");
            result.checks.entrySet().stream()
                .filter(e -> !e.getValue().passed)
                .forEach(e -> LOGGER.warning("  - " + e.getKey() + ": " + e.getValue().message));
        }

        return result;
    }

    private static HealthCheckResult.CheckResult checkComponent(String name, boolean enabled) {
        HealthCheckResult.CheckResult check = new HealthCheckResult.CheckResult();
        check.name = name;
        check.passed = enabled;
        check.message = enabled ? "Running" : "Not initialized";
        return check;
    }

    private static void performAutoVerification() {
        if (!ENABLED.get()) return;

        HealthCheckResult health = performHealthCheck();
        if (!health.passed) {
            LOGGER.warning("Auto-verification skipped due to health check failures");
            return;
        }

        verifyOptimizations("Auto-Verification #" + VERIFICATION_COUNT.get());
    }

    public static TrendAnalysis analyzeTrend(String metricName, List<PerformanceSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return new TrendAnalysis(metricName, TrendDirection.INSUFFICIENT_DATA, 0, 0, snapshots.size());
        }

        List<Double> values = new ArrayList<>();
        for (PerformanceSnapshot snapshot : snapshots) {
            values.add(getMetricValue(snapshot.getMetrics(), metricName));
        }

        if (values.stream().anyMatch(v -> v < 0)) {
            return new TrendAnalysis(metricName, TrendDirection.INSUFFICIENT_DATA, 0, 0, snapshots.size());
        }

        double first = values.get(0);
        double last = values.get(values.size() - 1);
        double change = first > 0 ? (last - first) / first * 100 : 0;

        TrendDirection direction;
        if (Math.abs(change) < 5) {
            direction = TrendDirection.STABLE;
        } else if (change < -5) {
            direction = TrendDirection.IMPROVING;
        } else if (change > 5) {
            direction = TrendDirection.DECLINING;
        } else {
            direction = TrendDirection.STABLE;
        }

        double volatility = calculateVolatility(values);

        return new TrendAnalysis(metricName, direction, change, volatility, snapshots.size());
    }

    private static double getMetricValue(PerformanceMetrics metrics, String metricName) {
        switch (metricName.toLowerCase()) {
            case "cpu": return metrics.cpuUsage;
            case "memory": return metrics.memoryUsage;
            case "cachehitrate": return metrics.cacheHitRate;
            case "eventfailures": return (double) metrics.eventFailures;
            case "latency": return metrics.avgEventProcessingTime;
            case "throughput": return (double) metrics.eventProcessed;
            case "score": return metrics.overallScore;
            default: return -1;
        }
    }

    private static double calculateVolatility(List<Double> values) {
        if (values.size() < 2) return 0;

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0);
        return Math.sqrt(variance);
    }

    public static void setOptimizationGoal(String goal, double target) {
        optimizationGoals.put(goal, target);
    }

    public static void setVerificationInterval(int seconds) {
        verificationIntervalSeconds = Math.max(60, Math.min(3600, seconds));
    }

    public static void setAutoVerify(boolean enabled) {
        autoVerify = enabled;
    }

    public static VerificationResult getLatestVerification() {
        if (verificationHistory.isEmpty()) return null;
        return verificationHistory.get(verificationHistory.size() - 1);
    }

    public static List<VerificationResult> getVerificationHistory() {
        return new ArrayList<>(verificationHistory);
    }

    public static Map<String, BenchmarkResult> getBenchmarkResults() {
        return new LinkedHashMap<>(benchmarkResults);
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static synchronized void shutdown() {
        if (!ENABLED.get()) return;

        ENABLED.set(false);

        if (verificationScheduler != null && !verificationScheduler.isShutdown()) {
            verificationScheduler.shutdown();
            try {
                if (!verificationScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    verificationScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                verificationScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        verificationHistory.clear();
        benchmarkResults.clear();
        optimizationGoals.clear();

        LOGGER.info("OptimizationVerifier shutdown complete");
    }

    public static final class VerificationResult {
        private String name;
        private long timestamp;
        private PerformanceMetrics baseline;
        private PerformanceMetrics current;
        private OptimizationChanges changes;
        private Map<String, Boolean> goalAchievements;
        private String status;

        public String getName() { return name; }
        public long getTimestamp() { return timestamp; }
        public PerformanceMetrics getBaseline() { return baseline; }
        public PerformanceMetrics getCurrent() { return current; }
        public OptimizationChanges getChanges() { return changes; }
        public Map<String, Boolean> getGoalAchievements() { return goalAchievements; }
        public String getStatus() { return status; }

            @Override
    public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Verification Result: ").append(name).append(" ===\n");
            sb.append("Status: ").append(status).append("\n");
            sb.append("Timestamp: ").append(new Date(timestamp)).append("\n");
            sb.append("\n--- Improvements ---\n");
            sb.append("CPU: ").append(String.format("%.1f%%", changes.cpuImprovement)).append("\n");
            sb.append("Memory: ").append(String.format("%.1f%%", changes.memoryImprovement)).append("\n");
            sb.append("Cache Hit Rate: ").append(String.format("%.1f%%", changes.cacheHitRateImprovement)).append("\n");
            sb.append("Latency: ").append(String.format("%.1f%%", changes.latencyReduction)).append("\n");
            sb.append("\n--- Goal Achievements ---\n");
            goalAchievements.forEach((goal, achieved) ->
                sb.append("  ").append(goal).append(": ").append(achieved ? "✓" : "✗").append("\n"));
            return sb.toString();
        }

        public static final class OptimizationChanges {
            private double cpuImprovement;
            private double memoryImprovement;
            private double cacheHitRateImprovement;
            private long eventFailureReduction;
            private double latencyReduction;
            private double cacheHitRateCurrent;
            private long eventFailuresCurrent;
            private double latencyCurrent;

            public double getCpuImprovement() { return cpuImprovement; }
            public double getMemoryImprovement() { return memoryImprovement; }
            public double getCacheHitRateImprovement() { return cacheHitRateImprovement; }
            public long getEventFailureReduction() { return eventFailureReduction; }
            public double getLatencyReduction() { return latencyReduction; }
            public double getCacheHitRateCurrent() { return cacheHitRateCurrent; }
            public long getEventFailuresCurrent() { return eventFailuresCurrent; }
            public double getLatencyCurrent() { return latencyCurrent; }
        }
    }

    public static final class BenchmarkResult {
        private String name;
        private long timestamp;
        private double avgTimeMs;
        private double minTimeMs;
        private double maxTimeMs;
        private double stdDevMs;
        private int iterations;

        public String getName() { return name; }
        public long getTimestamp() { return timestamp; }
        public double getAvgTimeMs() { return avgTimeMs; }
        public double getMinTimeMs() { return minTimeMs; }
        public double getMaxTimeMs() { return maxTimeMs; }
        public double getStdDevMs() { return stdDevMs; }
        public int getIterations() { return iterations; }

            @Override
    public String toString() {
            return String.format("%s: avg=%.3fms, min=%.3fms, max=%.3fms, stdDev=%.3fms",
                name, avgTimeMs, minTimeMs, maxTimeMs, stdDevMs);
        }
    }

    public static final class BenchmarkComparison {
        private String beforeName;
        private String afterName;
        private long timestamp;
        private double improvementPercent;
        private double beforeAvgMs;
        private double afterAvgMs;
        private boolean isImproved;

        public String getBeforeName() { return beforeName; }
        public String getAfterName() { return afterName; }
        public long getTimestamp() { return timestamp; }
        public double getImprovementPercent() { return improvementPercent; }
        public double getBeforeAvgMs() { return beforeAvgMs; }
        public double getAfterAvgMs() { return afterAvgMs; }
        public boolean isImproved() { return isImproved; }

            @Override
    public String toString() {
            return String.format("%s -> %s: %.1f%% improvement (%.3fms -> %.3fms)",
                beforeName, afterName, improvementPercent, beforeAvgMs, afterAvgMs);
        }
    }

    public static final class HealthCheckResult {
        private long timestamp;
        private boolean passed;
        private double passRate;
        private Map<String, CheckResult> checks;

        public long getTimestamp() { return timestamp; }
        public boolean isPassed() { return passed; }
        public double getPassRate() { return passRate; }
        public Map<String, CheckResult> getChecks() { return checks; }

            @Override
    public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Health Check Result ===\n");
            sb.append("Passed: ").append(passed).append("\n");
            sb.append("Pass Rate: ").append(String.format("%.1f%%", passRate)).append("\n");
            checks.forEach((name, check) ->
                sb.append("  ").append(name).append(": ")
                  .append(check.passed ? "✓" : "✗").append(" ").append(check.message).append("\n"));
            return sb.toString();
        }

        public static final class CheckResult {
            private String name;
            private boolean passed;
            private String message;

            public String getName() { return name; }
            public boolean isPassed() { return passed; }
            public String getMessage() { return message; }
        }
    }

    public static final class TrendAnalysis {
        private final String metricName;
        private final TrendDirection direction;
        private final double changePercent;
        private final double volatility;
        private final int dataPoints;

        public TrendAnalysis(String metricName, TrendDirection direction,
                           double changePercent, double volatility, int dataPoints) {
            this.metricName = metricName;
            this.direction = direction;
            this.changePercent = changePercent;
            this.volatility = volatility;
            this.dataPoints = dataPoints;
        }

        public String getMetricName() { return metricName; }
        public TrendDirection getDirection() { return direction; }
        public double getChangePercent() { return changePercent; }
        public double getVolatility() { return volatility; }
        public int getDataPoints() { return dataPoints; }

            @Override
    public String toString() {
            return String.format("%s: %s (%.1f%% change, %.2f volatility, %d data points)",
                metricName, direction, changePercent, volatility, dataPoints);
        }
    }

    public enum TrendDirection {
        IMPROVING,
        DECLINING,
        STABLE,
        INSUFFICIENT_DATA
    }
}
