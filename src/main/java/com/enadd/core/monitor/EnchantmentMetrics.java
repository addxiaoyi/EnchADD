package com.enadd.core.monitor;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;


/**
 * 附魔系统性能指标监控器
 * 监控附魔效果触发频率、缓存命中率、内存使用趋势
 */
public final class EnchantmentMetrics {

    private static final class Holder {
        private static final EnchantmentMetrics INSTANCE = new EnchantmentMetrics();
    }

    // 附魔触发统计
    private final Map<String, LongAdder> enchantmentTriggerCounts = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> enchantmentSuccessCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> enchantmentLastTriggerTime = new ConcurrentHashMap<>();

    // 缓存统计
    private final LongAdder cacheHits = new LongAdder();
    private final LongAdder cacheMisses = new LongAdder();
    private final LongAdder cacheEvictions = new LongAdder();

    // 性能统计
    private final Map<String, LongAdder> operationCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> operationTotalTime = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> operationMaxTime = new ConcurrentHashMap<>();

    // 内存趋势
    private final Map<Long, MemorySnapshot> memoryHistory = new ConcurrentHashMap<>();
    private final AtomicLong lastMemoryRecordTime = new AtomicLong(0);
    private static final long MEMORY_RECORD_INTERVAL = 60000; // 1分钟
    private static final int MAX_MEMORY_HISTORY_SIZE = 1440; // 24小时

    // 启动时间
    private final long startTime = System.currentTimeMillis();

    private EnchantmentMetrics() {}

    public static EnchantmentMetrics getInstance() {
        return Holder.INSTANCE;
    }

    // ==================== 附魔触发统计 ====================

    /**
     * 记录附魔触发
     */
    public void recordEnchantmentTrigger(String enchantmentId) {
        enchantmentTriggerCounts.computeIfAbsent(enchantmentId, k -> new LongAdder()).increment();
        enchantmentLastTriggerTime.put(enchantmentId, new AtomicLong(System.currentTimeMillis()));
    }

    /**
     * 记录附魔成功应用
     */
    public void recordEnchantmentSuccess(String enchantmentId) {
        enchantmentSuccessCounts.computeIfAbsent(enchantmentId, k -> new LongAdder()).increment();
    }

    /**
     * 获取附魔触发次数
     */
    public long getEnchantmentTriggerCount(String enchantmentId) {
        LongAdder counter = enchantmentTriggerCounts.get(enchantmentId);
        return counter != null ? counter.sum() : 0;
    }

    /**
     * 获取附魔成功率
     */
    public double getEnchantmentSuccessRate(String enchantmentId) {
        long triggers = getEnchantmentTriggerCount(enchantmentId);
        if (triggers == 0) return 0.0;

        LongAdder successes = enchantmentSuccessCounts.get(enchantmentId);
        long successCount = successes != null ? successes.sum() : 0;

        return (double) successCount / triggers * 100;
    }

    /**
     * 获取最热门的附魔（触发次数最多）
     */
    public Map<String, Long> getTopEnchantments(int limit) {
        return enchantmentTriggerCounts.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue().sum(), e1.getValue().sum()))
            .limit(limit)
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().sum(),
                (e1, e2) -> e1,
                java.util.LinkedHashMap::new
            ));
    }

    // ==================== 缓存统计 ====================

    /**
     * 记录缓存命中
     */
    public void recordCacheHit() {
        cacheHits.increment();
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss() {
        cacheMisses.increment();
    }

    /**
     * 记录缓存淘汰
     */
    public void recordCacheEviction() {
        cacheEvictions.increment();
    }

    /**
     * 获取缓存命中率
     */
    public double getCacheHitRate() {
        long hits = cacheHits.sum();
        long misses = cacheMisses.sum();
        long total = hits + misses;

        return total > 0 ? (double) hits / total * 100 : 0.0;
    }

    /**
     * 获取缓存统计
     */
    public CacheStats getCacheStats() {
        return new CacheStats(
            cacheHits.sum(),
            cacheMisses.sum(),
            cacheEvictions.sum(),
            getCacheHitRate()
        );
    }

    // ==================== 性能统计 ====================

    /**
     * 记录操作执行时间
     */
    public void recordOperationTime(String operation, long timeNanos) {
        operationCounts.computeIfAbsent(operation, k -> new LongAdder()).increment();

        AtomicLong totalTime = operationTotalTime.computeIfAbsent(operation, k -> new AtomicLong(0));
        totalTime.addAndGet(timeNanos);

        AtomicLong maxTime = operationMaxTime.computeIfAbsent(operation, k -> new AtomicLong(0));
        maxTime.updateAndGet(current -> Math.max(current, timeNanos));
    }

    /**
     * 获取操作平均执行时间（毫秒）
     */
    public double getAverageOperationTime(String operation) {
        LongAdder count = operationCounts.get(operation);
        AtomicLong totalTime = operationTotalTime.get(operation);

        if (count == null || totalTime == null || count.sum() == 0) {
            return 0.0;
        }

        return (double) totalTime.get() / count.sum() / 1_000_000.0;
    }

    /**
     * 获取操作最大执行时间（毫秒）
     */
    public double getMaxOperationTime(String operation) {
        AtomicLong maxTime = operationMaxTime.get(operation);
        return maxTime != null ? maxTime.get() / 1_000_000.0 : 0.0;
    }

    // ==================== 内存趋势 ====================

    /**
     * 记录内存快照
     */
    public void recordMemorySnapshot() {
        long currentTime = System.currentTimeMillis();

        // 检查是否需要记录
        if (currentTime - lastMemoryRecordTime.get() < MEMORY_RECORD_INTERVAL) {
            return;
        }

        lastMemoryRecordTime.set(currentTime);

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        MemorySnapshot snapshot = new MemorySnapshot(
            currentTime,
            usedMemory,
            maxMemory,
            runtime.totalMemory(),
            runtime.freeMemory()
        );

        memoryHistory.put(currentTime, snapshot);

        // 清理旧数据
        if (memoryHistory.size() > MAX_MEMORY_HISTORY_SIZE) {
            long oldestTime = memoryHistory.keySet().stream()
                .min(Comparator.naturalOrder())
                .orElse(0L);
            memoryHistory.remove(oldestTime);
        }
    }

    /**
     * 获取内存使用趋势
     */
    public Map<Long, MemorySnapshot> getMemoryHistory() {
        return new ConcurrentHashMap<>(memoryHistory);
    }

    /**
     * 获取内存使用增长率（每小时MB）
     */
    public double getMemoryGrowthRate() {
        if (memoryHistory.size() < 2) {
            return 0.0;
        }

        long oldestTime = memoryHistory.keySet().stream().min(Comparator.naturalOrder()).orElse(0L);
        long newestTime = memoryHistory.keySet().stream().max(Comparator.naturalOrder()).orElse(0L);

        MemorySnapshot oldest = memoryHistory.get(oldestTime);
        MemorySnapshot newest = memoryHistory.get(newestTime);

        if (oldest == null || newest == null) {
            return 0.0;
        }

        long timeDiff = newestTime - oldestTime;
        long memoryDiff = newest.usedMemory - oldest.usedMemory;

        if (timeDiff == 0) {
            return 0.0;
        }

        // 转换为每小时MB
        return (double) memoryDiff / 1024 / 1024 / (timeDiff / 3600000.0);
    }

    // ==================== 综合统计 ====================

    /**
     * 获取运行时间（小时）
     */
    public double getUptimeHours() {
        return (System.currentTimeMillis() - startTime) / 3600000.0;
    }

    /**
     * 生成完整报告
     */
    public MetricsReport generateReport() {
        return new MetricsReport(
            getUptimeHours(),
            getCacheStats(),
            getTopEnchantments(10),
            getMemoryGrowthRate(),
            enchantmentTriggerCounts.size()
        );
    }

    /**
     * 重置所有统计
     */
    public void reset() {
        enchantmentTriggerCounts.clear();
        enchantmentSuccessCounts.clear();
        enchantmentLastTriggerTime.clear();
        cacheHits.reset();
        cacheMisses.reset();
        cacheEvictions.reset();
        operationCounts.clear();
        operationTotalTime.clear();
        operationMaxTime.clear();
        memoryHistory.clear();
    }

    // ==================== 数据类 ====================

    public static class CacheStats {
        public final long hits;
        public final long misses;
        public final long evictions;
        public final double hitRate;

        public CacheStats(long hits, long misses, long evictions, double hitRate) {
            this.hits = hits;
            this.misses = misses;
            this.evictions = evictions;
            this.hitRate = hitRate;
        }
    }

    public static class MemorySnapshot {
        public final long timestamp;
        public final long usedMemory;
        public final long maxMemory;
        public final long totalMemory;
        public final long freeMemory;

        public MemorySnapshot(long timestamp, long usedMemory, long maxMemory, long totalMemory, long freeMemory) {
            this.timestamp = timestamp;
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
            this.totalMemory = totalMemory;
            this.freeMemory = freeMemory;
        }

        public double getUsedPercentage() {
            return maxMemory > 0 ? (double) usedMemory / maxMemory * 100 : 0;
        }
    }

    public static class MetricsReport {
        public final double uptimeHours;
        public final CacheStats cacheStats;
        public final Map<String, Long> topEnchantments;
        public final double memoryGrowthRate;
        public final int totalEnchantmentTypes;

        public MetricsReport(double uptimeHours, CacheStats cacheStats,
                           Map<String, Long> topEnchantments, double memoryGrowthRate,
                           int totalEnchantmentTypes) {
            this.uptimeHours = uptimeHours;
            this.cacheStats = cacheStats;
            this.topEnchantments = topEnchantments;
            this.memoryGrowthRate = memoryGrowthRate;
            this.totalEnchantmentTypes = totalEnchantmentTypes;
        }
    }
}
