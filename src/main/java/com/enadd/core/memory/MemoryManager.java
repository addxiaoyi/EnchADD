package com.enadd.core.memory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public final class MemoryManager {
    // Holder模式优化单例
    private static final class Holder {
        private static final MemoryManager INSTANCE = new MemoryManager();
    }

    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    private final List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
    private final AtomicLong totalAllocated = new AtomicLong(0);
    private final AtomicLong totalFreed = new AtomicLong(0);
    private final AtomicLong allocationBudget = new AtomicLong(512 * 1024 * 1024);
    // 预分配容量
    private final ConcurrentHashMap<String, AtomicLong> allocationByType = new ConcurrentHashMap<>(32);
    private final ConcurrentHashMap<String, MemoryWarningCallback> warningCallbacks = new ConcurrentHashMap<>(16);

    private volatile long baselineMemory = 0;
    private volatile boolean leakDetectionEnabled = true;
    private volatile long lastCheckTime = System.currentTimeMillis();
    private volatile long checkInterval = 60000;
    private volatile float warningThreshold = 0.8f;
    private volatile float criticalThreshold = 0.9f;

    private MemoryManager() {
        baselineMemory = getUsedMemory();
    }

    public static MemoryManager getInstance() {
        return Holder.INSTANCE;
    }

    public long getUsedMemory() {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }

    public long getCommittedMemory() {
        return memoryMXBean.getHeapMemoryUsage().getCommitted();
    }

    public long getMaxMemory() {
        return memoryMXBean.getHeapMemoryUsage().getMax();
    }

    public float getMemoryUsagePercent() {
        long used = getUsedMemory();
        long max = getMaxMemory();
        return max > 0 ? (float) used / max * 100 : 0;
    }

    public Map<String, MemoryUsage> getMemoryPoolStats() {
        Map<String, MemoryUsage> stats = new HashMap<>();
        for (MemoryPoolMXBean pool : memoryPools) {
            stats.put(pool.getName(), pool.getUsage());
        }
        return stats;
    }

    public void trackAllocation(String type, long size) {
        totalAllocated.addAndGet(size);
        allocationByType.computeIfAbsent(type, k -> new AtomicLong(0)).addAndGet(size);

        if (getMemoryUsagePercent() > criticalThreshold * 100) {
            triggerEmergencyCleanup();
        }
    }

    public void trackDeallocation(String type, long size) {
        totalFreed.addAndGet(size);
        allocationByType.computeIfAbsent(type, k -> new AtomicLong(0)).addAndGet(-size);
    }

    public void registerWarningCallback(String id, MemoryWarningCallback callback) {
        warningCallbacks.put(id, callback);
    }

    public void unregisterWarningCallback(String id) {
        warningCallbacks.remove(id);
    }

    private void notifyWarningCallbacks(long memoryGrowth, float growthPercent) {
        warningCallbacks.values().forEach(callback -> callback.onMemoryWarning(memoryGrowth, growthPercent));
    }

    private void notifyCriticalCallbacks(long memoryGrowth, float growthPercent) {
        warningCallbacks.values().forEach(callback -> callback.onMemoryCritical(memoryGrowth, growthPercent));
    }

    public MemoryStats getStats() {
        ConcurrentHashMap<String, AtomicLong> allocationCopy = new ConcurrentHashMap<>();
        allocationByType.forEach((key, value) -> allocationCopy.put(key, new AtomicLong(value.get())));

        return new MemoryStats(
                getUsedMemory(),
                getCommittedMemory(),
                getMaxMemory(),
                totalAllocated.get(),
                totalFreed.get(),
                allocationCopy,
                baselineMemory,
                getGCStats(),
                getMemoryPoolStats()
        );
    }

    private List<GCStats> getGCStats() {
        List<GCStats> stats = new ArrayList<>();
        for (GarbageCollectorMXBean gc : gcBeans) {
            stats.add(new GCStats(
                    gc.getName(),
                    gc.getCollectionCount(),
                    gc.getCollectionTime(),
                    gc.getMemoryPoolNames(),
                    gc.isValid()
            ));
        }
        return stats;
    }

    public void triggerGC() {
        System.gc();
        lastCheckTime = System.currentTimeMillis();
    }

    public void triggerEmergencyCleanup() {
        System.gc();
        cleanupWeakReferences();
        notifyCriticalCallbacks(getUsedMemory() - baselineMemory, getMemoryUsagePercent());
    }

    public void cleanupWeakReferences() {}

    public void resetBaseline() {
        baselineMemory = getUsedMemory();
    }

    public void setAllocationBudget(long bytes) {
        allocationBudget.set(bytes);
    }

    public boolean isWithinBudget() {
        return totalAllocated.get() - totalFreed.get() < allocationBudget.get();
    }

    public void setLeakDetectionEnabled(boolean enabled) {
        leakDetectionEnabled = enabled;
    }

    public boolean isLeakDetectionEnabled() {
        return leakDetectionEnabled;
    }

    public void setCheckInterval(long millis) {
        this.checkInterval = millis;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setWarningThresholds(float warning, float critical) {
        this.warningThreshold = Math.min(warning, critical);
        this.criticalThreshold = critical;
    }

    public boolean shouldCheck() {
        return System.currentTimeMillis() - lastCheckTime > checkInterval;
    }

    public void markCheckComplete() {
        lastCheckTime = System.currentTimeMillis();
    }

    public void checkForLeaks() {
        if (!leakDetectionEnabled || !shouldCheck()) return;

        markCheckComplete();

        long currentMemory = getUsedMemory();
        long memoryGrowth = currentMemory - baselineMemory;
        float growthPercent = baselineMemory > 0 ? (float) memoryGrowth / baselineMemory * 100 : 0;

        if (growthPercent > warningThreshold * 100) {
            notifyWarningCallbacks(memoryGrowth, growthPercent);
        }

        if (growthPercent > criticalThreshold * 100) {
            notifyCriticalCallbacks(memoryGrowth, growthPercent);
            triggerEmergencyCleanup();
        }
    }

    public void shutdown() {
        leakDetectionEnabled = false;
        warningCallbacks.clear();
        allocationByType.clear();
        totalAllocated.set(0);
        totalFreed.set(0);
    }

    @FunctionalInterface
    public interface MemoryWarningCallback {
        void onMemoryWarning(long growth, float percent);
        default void onMemoryCritical(long growth, float percent) {
            onMemoryWarning(growth, percent);
        }
    }

    public static final class MemoryStats {
        private final long usedMemory;
        private final long committedMemory;
        private final long maxMemory;
        private final long totalAllocated;
        private final long totalFreed;
        private ConcurrentHashMap<String, AtomicLong> allocationByType;
        private final long baselineMemory;
        private final List<GCStats> gcStats;
        private final Map<String, MemoryUsage> memoryPoolStats;

        public MemoryStats(long used, long committed, long max, long allocated, long freed,
                          ConcurrentHashMap<String, AtomicLong> byType, long baseline,
                          List<GCStats> gc, Map<String, MemoryUsage> poolStats) {
            this.usedMemory = used;
            this.committedMemory = committed;
            this.maxMemory = max;
            this.totalAllocated = allocated;
            this.totalFreed = freed;
            this.allocationByType = byType;
            this.baselineMemory = baseline;
            this.gcStats = gc;
            this.memoryPoolStats = poolStats;
        }

        public long getUsedMemory() { return usedMemory; }
        public long getCommittedMemory() { return committedMemory; }
        public long getMaxMemory() { return maxMemory; }
        public long getTotalAllocated() { return totalAllocated; }
        public long getTotalFreed() { return totalFreed; }
        public long getCurrentAllocation() { return totalAllocated - totalFreed; }
        public long getBaselineMemory() { return baselineMemory; }
        public float getMemoryUsagePercent() { return maxMemory > 0 ? (float) usedMemory / maxMemory * 100 : 0; }
        public float getGrowthPercent() { return baselineMemory > 0 ? (float) (usedMemory - baselineMemory) / baselineMemory * 100 : 0; }
        public List<GCStats> getGcStats() { return gcStats; }
        public Map<String, MemoryUsage> getMemoryPoolStats() { return memoryPoolStats; }

        public ConcurrentHashMap<String, AtomicLong> getAllocationByType() {
            return allocationByType;
        }

        public void setAllocationByType(ConcurrentHashMap<String, AtomicLong> allocationByType) {
            this.allocationByType = allocationByType;
        }
    }

    public static final class GCStats {
        private final String name;
        private final long collectionCount;
        private final long collectionTime;
        private final String[] memoryPools;
        private final boolean valid;

        public GCStats(String name, long count, long time, String[] pools, boolean valid) {
            this.name = name;
            this.collectionCount = count;
            this.collectionTime = time;
            this.memoryPools = pools;
            this.valid = valid;
        }

        public String getName() { return name; }
        public long getCollectionCount() { return collectionCount; }
        public long getCollectionTime() { return collectionTime; }
        public String[] getMemoryPools() { return memoryPools; }
        public boolean isValid() { return valid; }
        public float getAvgCollectionTime() { return collectionCount > 0 ? (float) collectionTime / collectionCount : 0; }
    }
}
