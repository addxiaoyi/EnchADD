package com.enadd.core.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 高性能缓存管理器
 * 支持TTL、LRU淘汰、自动清理
 *
 * 线程安全：所有公共方法都是线程安全的
 * 性能优化：使用ConcurrentHashMap和原子操作
 */
public final class CacheManager {
    private static final Logger LOGGER = Logger.getLogger(CacheManager.class.getName());

    private static final class Holder {
        private static final CacheManager INSTANCE = new CacheManager();
    }

    private final ConcurrentHashMap<String, Cache<?, ?>> caches = new ConcurrentHashMap<>(16);
    private final AtomicBoolean autoCleanupEnabled = new AtomicBoolean(true);
    private final AtomicBoolean cleanupThreadStarted = new AtomicBoolean(false);
    private volatile long cleanupInterval = 300000; // 5分钟（CPU优化：从1分钟延长）
    private volatile Thread cleanupThread;

    private CacheManager() {
        try {
            startCleanupTask();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start cleanup task", e);
        }
    }

    public static CacheManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 获取或创建缓存
     *
     * @param name 缓存名称，不能为null或空
     * @param maxSize 最大缓存条目数，必须>0
     * @param ttlMillis 过期时间（毫秒），必须>=0，0表示永不过期
     * @return 缓存实例
     * @throws IllegalArgumentException 如果参数无效
     */
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name, int maxSize, long ttlMillis) {
        // Bug修复1-3: 参数验证
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache name cannot be null or empty");
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Max size must be positive, got: " + maxSize);
        }
        if (ttlMillis < 0) {
            throw new IllegalArgumentException("TTL cannot be negative, got: " + ttlMillis);
        }

        try {
            // Bug修复4: 异常处理
            return (Cache<K, V>) caches.computeIfAbsent(name,
                k -> new Cache<>(k, maxSize, ttlMillis));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to create cache: " + name, e);
            throw new RuntimeException("Failed to create cache: " + name, e);
        }
    }

    /**
     * 获取或创建默认缓存（1000条，5分钟TTL）
     *
     * @param name 缓存名称，不能为null或空
     * @return 缓存实例
     * @throws IllegalArgumentException 如果name无效
     */
    public <K, V> Cache<K, V> getCache(String name) {
        return getCache(name, 1000, TimeUnit.MINUTES.toMillis(5));
    }

    /**
     * 清理所有过期条目
     * 线程安全，可以在任何时候调用
     */
    public void cleanupAll() {
        // CPU优化: 智能清理 - 先调整间隔
        smartCleanup();

        // Bug修复5: 异常处理
        for (Cache<?, ?> cache : caches.values()) {
            try {
                if (cache != null) {
                    cache.cleanup();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to cleanup cache: " +
                    (cache != null ? cache.getName() : "unknown"), e);
            }
        }
    }

    /**
     * CPU优化: 智能清理 - 根据命中率动态调整清理间隔
     */
    private void smartCleanup() {
        try {
            CacheStats stats = getStats();
            double hitRate = stats.getOverallHitRate();

            if (hitRate > 0.9) {
                // 命中率高，延长清理间隔
                cleanupInterval = 600000; // 10分钟
                LOGGER.fine("High cache hit rate (" + String.format("%.2f%%", hitRate * 100) +
                    "), extending cleanup interval to 10 minutes");
            } else if (hitRate < 0.5) {
                // 命中率低，缩短清理间隔
                cleanupInterval = 180000; // 3分钟
                LOGGER.fine("Low cache hit rate (" + String.format("%.2f%%", hitRate * 100) +
                    "), reducing cleanup interval to 3 minutes");
            } else {
                // 正常命中率，使用默认间隔
                cleanupInterval = 300000; // 5分钟
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error in smart cleanup", e);
        }
    }

    /**
     * 清空所有缓存
     * 线程安全，会重置所有统计信息
     */
    public void clearAll() {
        // Bug修复6: 异常处理
        for (Cache<?, ?> cache : caches.values()) {
            try {
                if (cache != null) {
                    cache.clear();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to clear cache: " +
                    (cache != null ? cache.getName() : "unknown"), e);
            }
        }
    }

    /**
     * 获取缓存统计
     * 注意：统计数据是快照，可能在返回后立即过时
     *
     * @return 缓存统计信息
     */
    public CacheStats getStats() {
        long totalSize = 0;
        long totalHits = 0;
        long totalMisses = 0;
        int cacheCount = 0;

        // Bug修复7: 使用快照避免并发修改，并添加异常处理
        try {
            for (Cache<?, ?> cache : caches.values()) {
                if (cache != null) {
                    try {
                        totalSize += cache.size();
                        totalHits += cache.getHits();
                        totalMisses += cache.getMisses();
                        cacheCount++;
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to get stats from cache: " +
                            cache.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to collect cache stats", e);
        }

        return new CacheStats(cacheCount, totalSize, totalHits, totalMisses);
    }

    /**
     * 启动清理任务
     * 使用守护线程定期清理过期条目
     */
    private void startCleanupTask() {
        // Bug修复9: 防止重复启动
        if (!autoCleanupEnabled.get() || !cleanupThreadStarted.compareAndSet(false, true)) {
            return;
        }

        try {
            // Bug修复8,10: 线程启动异常处理和参数验证
            cleanupThread = new Thread(() -> {
                LOGGER.info("Cache cleanup thread started");
                while (autoCleanupEnabled.get()) {
                    try {
                        long interval = cleanupInterval;
                        // Bug修复10: 验证清理间隔
                        if (interval <= 0) {
                            interval = 60000; // 默认1分钟
                        }
                        Thread.sleep(interval);
                        cleanupAll();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.info("Cache cleanup thread interrupted");
                        break;
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error in cleanup task", e);
                    }
                }
                LOGGER.info("Cache cleanup thread stopped");
            }, "CacheManager-Cleanup");

            cleanupThread.setDaemon(true);
            cleanupThread.setPriority(Thread.MIN_PRIORITY); // CPU优化: 最低优先级
            cleanupThread.start();
        } catch (Exception e) {
            cleanupThreadStarted.set(false);
            LOGGER.log(Level.SEVERE, "Failed to start cleanup thread", e);
            throw new RuntimeException("Failed to start cleanup thread", e);
        }
    }

    /**
     * 设置清理间隔
     *
     * @param intervalMillis 清理间隔（毫秒），必须>0
     * @throws IllegalArgumentException 如果间隔无效
     */
    public void setCleanupInterval(long intervalMillis) {
        // Bug修复36: 添加动态调整清理间隔的方法
        if (intervalMillis <= 0) {
            throw new IllegalArgumentException("Cleanup interval must be positive, got: " + intervalMillis);
        }
        this.cleanupInterval = intervalMillis;
        LOGGER.info("Cleanup interval set to: " + intervalMillis + "ms");
    }

    /**
     * 关闭缓存管理器
     * 停止清理线程并清空所有缓存
     * 此方法是幂等的，可以多次调用
     */
    public void shutdown() {
        // Bug修复29: 幂等性检查
        if (!autoCleanupEnabled.getAndSet(false)) {
            return; // 已经关闭
        }

        LOGGER.info("Shutting down CacheManager");

        // Bug修复11,12: 正确停止清理线程
        Thread thread = cleanupThread;
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join(5000); // 等待最多5秒
                if (thread.isAlive()) {
                    LOGGER.warning("Cleanup thread did not stop within timeout");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warning("Interrupted while waiting for cleanup thread to stop");
            }
        }

        clearAll();
        caches.clear();
        cleanupThreadStarted.set(false);
        LOGGER.info("CacheManager shutdown complete");
    }

    /**
     * 缓存实现
     * 线程安全的LRU缓存，支持TTL过期
     *
     * @param <K> 键类型
     * @param <V> 值类型
     */
    public static final class Cache<K, V> {
        private static final Logger CACHE_LOGGER = Logger.getLogger(Cache.class.getName());

        private final String name;
        private final int maxSize;
        private final long ttlMillis;
        private final ConcurrentHashMap<K, CacheEntry<V>> entries;

        private volatile long hits = 0;
        private volatile long misses = 0;

        private Cache(String name, int maxSize, long ttlMillis) {
            // Bug修复30-32: 参数验证
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Cache name cannot be null or empty");
            }
            if (maxSize <= 0) {
                throw new IllegalArgumentException("Max size must be positive, got: " + maxSize);
            }
            if (ttlMillis < 0) {
                throw new IllegalArgumentException("TTL cannot be negative, got: " + ttlMillis);
            }

            this.name = name;
            this.maxSize = maxSize;
            this.ttlMillis = ttlMillis;
            this.entries = new ConcurrentHashMap<>(Math.min(maxSize, 256));
        }

        /**
         * 获取缓存值
         *
         * @param key 键，不能为null
         * @return 缓存的值，如果不存在或已过期则返回null
         * @throws IllegalArgumentException 如果key为null
         */
        public V get(K key) {
            // Bug修复13: key参数验证
            if (key == null) {
                throw new IllegalArgumentException("Cache key cannot be null");
            }

            try {
                // Bug修复14: 完整的null处理
                CacheEntry<V> entry = entries.get(key);
                if (entry == null) {
                    misses++;
                    return null;
                }

                // Bug修复35: 时间溢出处理
                if (entry.isExpired()) {
                    entries.remove(key);
                    misses++;
                    return null;
                }

                entry.updateAccessTime();
                hits++;
                return entry.value;
            } catch (Exception e) {
                CACHE_LOGGER.log(Level.WARNING, "Error getting cache value for key: " + key, e);
                misses++;
                return null;
            }
        }

        /**
         * 获取或计算缓存值
         * 如果缓存中不存在，则使用computer函数计算并缓存结果
         *
         * @param key 键，不能为null
         * @param computer 计算函数，不能为null
         * @return 缓存的值或计算的值
         * @throws IllegalArgumentException 如果参数为null
         */
        public V getOrCompute(K key, Function<K, V> computer) {
            // Bug修复15-16: 参数验证
            if (key == null) {
                throw new IllegalArgumentException("Cache key cannot be null");
            }
            if (computer == null) {
                throw new IllegalArgumentException("Computer function cannot be null");
            }

            V value = get(key);
            if (value != null) {
                return value;
            }

            try {
                // Bug修复17: 计算异常处理
                value = computer.apply(key);
                if (value != null) {
                    put(key, value);
                }
                return value;
            } catch (Exception e) {
                CACHE_LOGGER.log(Level.WARNING, "Error computing cache value for key: " + key, e);
                throw new RuntimeException("Failed to compute cache value", e);
            }
        }

        /**
         * 放入缓存
         * 如果缓存已满，会先淘汰最旧的条目
         *
         * @param key 键，不能为null
         * @param value 值，不能为null
         * @throws IllegalArgumentException 如果参数为null
         */
        public void put(K key, V value) {
            // Bug修复18-19: 参数验证
            if (key == null) {
                throw new IllegalArgumentException("Cache key cannot be null");
            }
            if (value == null) {
                throw new IllegalArgumentException("Cache value cannot be null");
            }

            try {
                // Bug修复20,33: 改进淘汰策略
                if (entries.size() >= maxSize && !entries.containsKey(key)) {
                    evictOldest();
                    // 再次检查，确保有空间
                    if (entries.size() >= maxSize) {
                        CACHE_LOGGER.warning("Cache still full after eviction, forcing removal");
                        evictOldest();
                    }
                }
                entries.put(key, new CacheEntry<>(value, ttlMillis));
            } catch (Exception e) {
                CACHE_LOGGER.log(Level.SEVERE, "Failed to put cache entry for key: " + key, e);
                throw new RuntimeException("Failed to put cache entry", e);
            }
        }

        /**
         * 移除缓存
         *
         * @param key 键，不能为null
         * @throws IllegalArgumentException 如果key为null
         */
        public void remove(K key) {
            // Bug修复21: key参数验证
            if (key == null) {
                throw new IllegalArgumentException("Cache key cannot be null");
            }

            try {
                entries.remove(key);
            } catch (Exception e) {
                CACHE_LOGGER.log(Level.WARNING, "Failed to remove cache entry for key: " + key, e);
            }
        }

        /**
         * 清空缓存
         * 会重置所有统计信息
         */
        public void clear() {
            try {
                entries.clear();
                resetStats();
            } catch (Exception e) {
                CACHE_LOGGER.log(Level.SEVERE, "Failed to clear cache: " + name, e);
            }
        }

        /**
         * 重置统计信息
         */
        public void resetStats() {
            // Bug修复37: 添加统计重置方法
            hits = 0;
            misses = 0;
        }

        /**
         * 清理过期条目
         * 遍历所有条目，移除已过期的
         */
        public void cleanup() {
            try {
                // Bug修复22: 异常处理
                entries.entrySet().removeIf(entry -> {
                    try {
                        return entry != null && entry.getValue() != null && entry.getValue().isExpired();
                    } catch (Exception e) {
                        CACHE_LOGGER.log(Level.WARNING, "Error checking expiration", e);
                        return false;
                    }
                });
            } catch (Exception e) {
                CACHE_LOGGER.log(Level.WARNING, "Failed to cleanup cache: " + name, e);
            }
        }

        /**
         * 淘汰最旧的条目（LRU策略）
         * 遍历所有条目找到最久未访问的条目并移除
         */
        private void evictOldest() {
            try {
                K oldestKey = null;
                long oldestTime = Long.MAX_VALUE;

                // Bug修复23-24: 使用快照避免并发修改，改进淘汰逻辑
                for (var entry : entries.entrySet()) {
                    try {
                        if (entry != null && entry.getValue() != null) {
                            long accessTime = entry.getValue().lastAccessTime;
                            // 使用严格小于，确保找到真正最旧的
                            if (accessTime < oldestTime) {
                                oldestTime = accessTime;
                                oldestKey = entry.getKey();
                            }
                        }
                    } catch (Exception e) {
                        CACHE_LOGGER.log(Level.WARNING, "Error checking entry age", e);
                    }
                }

                if (oldestKey != null) {
                    CacheEntry<V> removed = entries.remove(oldestKey);
                    if (removed != null) {
                        CACHE_LOGGER.fine("Evicted oldest entry with key: " + oldestKey);
                    }
                }
            } catch (Exception e) {
                CACHE_LOGGER.log(Level.SEVERE, "Failed to evict oldest entry", e);
            }
        }

        public int size() {
            return entries.size();
        }

        public long getHits() {
            return hits;
        }

        public long getMisses() {
            return misses;
        }

        /**
         * 获取缓存命中率
         *
         * @return 命中率（0.0-1.0），如果没有访问则返回0
         */
        public double getHitRate() {
            // Bug修复34: 改进精度处理
            long totalHits = hits;
            long totalMisses = misses;
            long total = totalHits + totalMisses;

            if (total <= 0) {
                return 0.0;
            }

            return (double) totalHits / (double) total;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 缓存条目
     * 存储缓存值和元数据（创建时间、TTL、访问时间）
     *
     * @param <V> 值类型
     */
    private static final class CacheEntry<V> {
        final V value;
        final long createTime;
        final long ttlMillis;
        volatile long lastAccessTime;

        CacheEntry(V value, long ttlMillis) {
            // Bug修复25: value参数验证
            if (value == null) {
                throw new IllegalArgumentException("Cache entry value cannot be null");
            }

            this.value = value;
            this.createTime = System.currentTimeMillis();
            this.ttlMillis = ttlMillis;
            this.lastAccessTime = createTime;
        }

        /**
         * 检查条目是否已过期
         *
         * @return true如果已过期，false否则
         */
        boolean isExpired() {
            if (ttlMillis <= 0) {
                return false; // 永不过期
            }

            try {
                // Bug修复35: 时间溢出处理
                long currentTime = System.currentTimeMillis();
                long age = currentTime - createTime;

                // 防止时间溢出导致负数
                if (age < 0) {
                    LOGGER.warning("Time overflow detected in cache entry expiration check");
                    return true; // 保守处理，认为已过期
                }

                return age > ttlMillis;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error checking expiration", e);
                return true; // 出错时保守处理
            }
        }

        /**
         * 更新最后访问时间
         */
        void updateAccessTime() {
            try {
                this.lastAccessTime = System.currentTimeMillis();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to update access time", e);
            }
        }
    }

    /**
     * 缓存统计信息
     * 提供缓存系统的整体统计数据
     */
    public static final class CacheStats {
        private final int cacheCount;
        private final long totalSize;
        private final long totalHits;
        private final long totalMisses;

        public CacheStats(int cacheCount, long totalSize, long totalHits, long totalMisses) {
            this.cacheCount = Math.max(0, cacheCount);
            this.totalSize = Math.max(0, totalSize);
            this.totalHits = Math.max(0, totalHits);
            this.totalMisses = Math.max(0, totalMisses);
        }

        public int getCacheCount() { return cacheCount; }
        public long getTotalSize() { return totalSize; }
        public long getTotalHits() { return totalHits; }
        public long getTotalMisses() { return totalMisses; }

        /**
         * 获取整体命中率
         *
         * @return 命中率（0.0-1.0）
         */
        public double getOverallHitRate() {
            long total = totalHits + totalMisses;
            if (total <= 0) {
                return 0.0;
            }
            return (double) totalHits / (double) total;
        }

        /**
         * Bug修复38: 添加toString方法用于调试
         */
        @Override
        public String toString() {
            return String.format(
                "CacheStats{caches=%d, size=%d, hits=%d, misses=%d, hitRate=%.2f%%}",
                cacheCount, totalSize, totalHits, totalMisses, getOverallHitRate() * 100
            );
        }
    }
}
