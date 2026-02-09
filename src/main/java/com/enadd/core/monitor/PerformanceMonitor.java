package com.enadd.core.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public final class PerformanceMonitor {
    // Holder模式优化单例
    private static final class Holder {
        private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();
    }

    private final OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    // 预分配容量
    private final ConcurrentHashMap<String, AtomicLong> operationTimes = new ConcurrentHashMap<>(64);
    private final ConcurrentHashMap<String, AtomicInteger> operationCounts = new ConcurrentHashMap<>(64);
    private final AtomicLong totalUpdateTime = new AtomicLong(0);
    private final AtomicLong frameCount = new AtomicLong(0);
    private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

    private volatile double warningThreshold = 70.0;
    private volatile double criticalThreshold = 90.0;

    private PerformanceMonitor() {
        threadMXBean.setThreadCpuTimeEnabled(true);
    }

    public static PerformanceMonitor getInstance() {
        return Holder.INSTANCE;
    }

    public float getSystemLoadAverage() {
        return (float) osMXBean.getSystemLoadAverage();
    }

    public double getCpuUsage() {
        return osMXBean.getSystemLoadAverage() / osMXBean.getAvailableProcessors() * 100;
    }

    public int getAvailableProcessors() {
        return osMXBean.getAvailableProcessors();
    }

    public long getTotalThreadCount() {
        return threadMXBean.getThreadCount();
    }

    public long getPeakThreadCount() {
        return threadMXBean.getPeakThreadCount();
    }

    public long getActiveThreadCount() {
        return threadMXBean.getThreadCount();
    }

    public void beginOperation(String operation) {
        operationCounts.computeIfAbsent(operation, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void endOperation(String operation, long durationNs) {
        operationTimes.computeIfAbsent(operation, k -> new AtomicLong(0)).addAndGet(durationNs);
    }

    public void recordFrame() {
        frameCount.incrementAndGet();
    }

    public void recordUpdateTime(long durationNs) {
        totalUpdateTime.addAndGet(durationNs);
    }

    public float getCurrentFPS() {
        long frames = frameCount.get();
        long time = System.currentTimeMillis() - startTime.get();
        return time > 0 ? ((float) frames) / ((float) time / 1000.0f) : 0;
    }

    public float getAverageUpdateTime() {
        long frames = frameCount.get();
        return frames > 0 ? (float) totalUpdateTime.get() / frames / 1_000_000 : 0;
    }

    public PerformanceStats getStats() {
        long totalOps = 0;
        long totalTime = 0;

        for (var entry : operationCounts.entrySet()) {
            totalOps += entry.getValue().get();
        }

        for (var entry : operationTimes.entrySet()) {
            totalTime += entry.getValue().get();
        }

        double cpuUsage = getCpuUsage();
        float systemLoad = (float) getSystemLoadAverage();
        int activeThreads = (int) getActiveThreadCount();
        float avgUpdateTime = getAverageUpdateTime();
        float currentFPS = getCurrentFPS();
        long frameCountVal = frameCount.get();
        int processors = osMXBean.getAvailableProcessors();

        return new PerformanceStats(
                cpuUsage,
                systemLoad,
                activeThreads,
                avgUpdateTime,
                currentFPS,
                totalOps,
                totalTime,
                frameCountVal,
                processors
        );
    }

    public OperationStats getOperationStats(String operation) {
        AtomicLong time = operationTimes.get(operation);
        AtomicInteger count = operationCounts.get(operation);

        long totalTime = time != null ? time.get() : 0;
        int opCount = count != null ? count.get() : 0;

        return new OperationStats(
                operation,
                opCount,
                totalTime,
                opCount > 0 ? totalTime / opCount : 0,
                opCount > 0 ? (float) totalTime / opCount / 1_000_000 : 0
        );
    }

    public void setWarningThreshold(double threshold) {
        this.warningThreshold = threshold;
    }

    public void setCriticalThreshold(double threshold) {
        this.criticalThreshold = threshold;
    }

    public boolean isUnderLoad() {
        return getCpuUsage() > warningThreshold;
    }

    public boolean isCriticalLoad() {
        return getCpuUsage() > criticalThreshold;
    }

    public void resetStats() {
        operationTimes.clear();
        operationCounts.clear();
        totalUpdateTime.set(0);
        frameCount.set(0);
    }

    public void shutdown() {
        resetStats();
        threadMXBean.setThreadCpuTimeEnabled(false);
    }

    public static final class PerformanceStats {
        private final double cpuUsage;
        private final float systemLoad;
        private final int activeThreads;
        private final float avgUpdateTime;
        private final float currentFPS;
        private final long totalOperations;
        private final long totalTime;
        private final long frameCount;
        private final int processors;

        public PerformanceStats(double cpu, float load, int threads, float avgUpdate, float fps,
                               long ops, long time, long frames, int procs) {
            this.cpuUsage = cpu;
            this.systemLoad = load;
            this.activeThreads = threads;
            this.avgUpdateTime = avgUpdate;
            this.currentFPS = fps;
            this.totalOperations = ops;
            this.totalTime = time;
            this.frameCount = frames;
            this.processors = procs;
        }

        public double getCpuUsage() { return cpuUsage; }
        public float getSystemLoad() { return systemLoad; }
        public int getActiveThreads() { return activeThreads; }
        public float getAvgUpdateTime() { return avgUpdateTime; }
        public float getCurrentFPS() { return currentFPS; }
        public long getTotalOperations() { return totalOperations; }
        public long getTotalTime() { return totalTime; }
        public long getFrameCount() { return frameCount; }
        public int getProcessors() { return processors; }

        public boolean isHealthy() {
            return cpuUsage < 70 && currentFPS >= 54;
        }
    }

    public static final class OperationStats {
        private final String operation;
        private final int count;
        private final long totalTime;
        private final long avgTimeNs;
        private final float avgTimeMs;

        public OperationStats(String op, int cnt, long total, long avgNs, float avgMs) {
            this.operation = op;
            this.count = cnt;
            this.totalTime = total;
            this.avgTimeNs = avgNs;
            this.avgTimeMs = avgMs;
        }

        public String getOperation() { return operation; }
        public int getCount() { return count; }
        public long getTotalTime() { return totalTime; }
        public long getAvgTimeNs() { return avgTimeNs; }
        public float getAvgTimeMs() { return avgTimeMs; }
    }
}
