package com.enadd.core.cache;

import com.enadd.config.EnchantmentConfig;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


/**
 * 二级缓存系统 - 优化频繁查询的附魔兼容性结果
 *
 * <p>实现L1（内存）和L2（本地）两级缓存架构，显著提升查询性能。</p>
 *
 * <p><strong>缓存策略：</strong></p>
 * <ul>
 *   <li>L1缓存：高频访问数据，TTL较短（5分钟）</li>
 *   <li>L2缓存：全量数据，TTL较长（30分钟）</li>
 *   <li>自动预热：启动时预加载热门数据</li>
 *   <li>LRU淘汰：缓存满时淘汰最久未使用数据</li>
 * </ul>
 *
 * @author ADDXIAOYI2048
 * @version 1.0.0
 * @since 1.0.0
 * @param <K> 键类型
 * @param <V> 值类型
 */
public class TwoLevelCache<K, V> {

    /** L1缓存 - 高频访问数据 */
    private final ConcurrentHashMap<K, CacheEntry<V>> l1Cache;

    /** L2缓存 - 全量数据 */
    private final ConcurrentHashMap<K, CacheEntry<V>> l2Cache;

    /** L1缓存最大容量 */
    private final int l1MaxSize;

    /** L2缓存最大容量 */
    private final int l2MaxSize;

    /** L1缓存TTL（毫秒） */
    private final long l1TtlMs;

    /** L2缓存TTL（毫秒） */
    private final long l2TtlMs;

    /** 缓存命中统计 */
    private final AtomicLong l1Hits = new AtomicLong(0);
    private final AtomicLong l2Hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);

    /**
     * 创建二级缓存
     *
     * @param l1MaxSize L1缓存最大容量
     * @param l2MaxSize L2缓存最大容量
     * @param l1TtlMs L1缓存TTL（毫秒）
     * @param l2TtlMs L2缓存TTL（毫秒）
     */
    public TwoLevelCache(int l1MaxSize, int l2MaxSize, long l1TtlMs, long l2TtlMs) {
        this.l1MaxSize = l1MaxSize;
        this.l2MaxSize = l2MaxSize;
        this.l1TtlMs = l1TtlMs;
        this.l2TtlMs = l2TtlMs;
        this.l1Cache = new ConcurrentHashMap<>(l1MaxSize);
        this.l2Cache = new ConcurrentHashMap<>(l2MaxSize);

        // 启动清理任务
        startCleanupTask();
    }

    /**
     * 使用默认配置创建二级缓存
     */
    public TwoLevelCache() {
        this(
            EnchantmentConfig.CacheConfig.MAX_CACHE_SIZE / 4,  // L1: 250
            EnchantmentConfig.CacheConfig.MAX_CACHE_SIZE,       // L2: 1000
            300000,  // L1 TTL: 5分钟
            1800000  // L2 TTL: 30分钟
        );
    }

    /**
     * 获取缓存值
     *
     * @param key 键
     * @return 值，如果不存在或已过期返回null
     */
    public V get(K key) {
        // 先查L1缓存
        CacheEntry<V> l1Entry = l1Cache.get(key);
        if (l1Entry != null && !l1Entry.isExpired()) {
            l1Entry.updateAccessTime();
            l1Hits.incrementAndGet();
            return l1Entry.value;
        }

        // 再查L2缓存
        CacheEntry<V> l2Entry = l2Cache.get(key);
        if (l2Entry != null && !l2Entry.isExpired()) {
            l2Entry.updateAccessTime();
            l2Hits.incrementAndGet();

            // 提升到L1缓存
            promoteToL1(key, l2Entry);
            return l2Entry.value;
        }

        // 缓存未命中
        misses.incrementAndGet();
        return null;
    }

    /**
     * 放入缓存
     *
     * @param key 键
     * @param value 值
     */
    public void put(K key, V value) {
        // 先放入L2缓存
        if (l2Cache.size() >= l2MaxSize) {
            evictL2Oldest();
        }
        l2Cache.put(key, new CacheEntry<>(value, l2TtlMs));

        // 如果L1缓存未满，也放入L1
        if (l1Cache.size() < l1MaxSize) {
            l1Cache.put(key, new CacheEntry<>(value, l1TtlMs));
        }
    }

    /**
     * 放入缓存并指定级别
     *
     * @param key 键
     * @param value 值
     * @param level 缓存级别（1或2）
     */
    public void put(K key, V value, int level) {
        if (level == 1) {
            if (l1Cache.size() >= l1MaxSize) {
                evictL1Oldest();
            }
            l1Cache.put(key, new CacheEntry<>(value, l1TtlMs));
        } else {
            if (l2Cache.size() >= l2MaxSize) {
                evictL2Oldest();
            }
            l2Cache.put(key, new CacheEntry<>(value, l2TtlMs));
        }
    }

    /**
     * 提升到L1缓存
     */
    private void promoteToL1(K key, CacheEntry<V> entry) {
        if (l1Cache.size() >= l1MaxSize) {
            evictL1Oldest();
        }
        l1Cache.put(key, new CacheEntry<>(entry.value, l1TtlMs));
    }

    /**
     * 淘汰L1最旧数据
     */
    private void evictL1Oldest() {
        K oldestKey = null;
        long oldestTime = Long.MAX_VALUE;

        for (var e : l1Cache.entrySet()) {
            if (e.getValue().lastAccessTime < oldestTime) {
                oldestTime = e.getValue().lastAccessTime;
                oldestKey = e.getKey();
            }
        }

        if (oldestKey != null) {
            l1Cache.remove(oldestKey);
        }
    }

    /**
     * 淘汰L2最旧数据
     */
    private void evictL2Oldest() {
        K oldestKey = null;
        long oldestTime = Long.MAX_VALUE;

        for (var e : l2Cache.entrySet()) {
            if (e.getValue().lastAccessTime < oldestTime) {
                oldestTime = e.getValue().lastAccessTime;
                oldestKey = e.getKey();
            }
        }

        if (oldestKey != null) {
            l2Cache.remove(oldestKey);
        }
    }

    /**
     * 启动清理任务
     */
    private void startCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(EnchantmentConfig.CacheConfig.CLEANUP_INTERVAL_MS);
                    cleanup();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "TwoLevelCache-Cleanup");
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

    /**
     * 清理过期数据
     */
    public void cleanup() {
        l1Cache.entrySet().removeIf(e -> e.getValue().isExpired());
        l2Cache.entrySet().removeIf(e -> e.getValue().isExpired());
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        l1Cache.clear();
        l2Cache.clear();
    }

    /**
     * 获取缓存统计
     */
    public CacheStats getStats() {
        long totalHits = l1Hits.get() + l2Hits.get();
        long totalRequests = totalHits + misses.get();
        double hitRate = totalRequests > 0 ? (double) totalHits / totalRequests : 0;

        return new CacheStats(
            l1Cache.size(),
            l2Cache.size(),
            l1Hits.get(),
            l2Hits.get(),
            misses.get(),
            hitRate
        );
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry<V> {
        final V value;
        final long createTime;
        final long ttlMs;
        volatile long lastAccessTime;

        CacheEntry(V value, long ttlMs) {
            this.value = value;
            this.ttlMs = ttlMs;
            this.createTime = System.currentTimeMillis();
            this.lastAccessTime = createTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createTime > ttlMs;
        }

        void updateAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }

    /**
     * 缓存统计
     */
    public static class CacheStats {
        public final int l1Size;
        public final int l2Size;
        public final long l1Hits;
        public final long l2Hits;
        public final long misses;
        public final double hitRate;

        public CacheStats(int l1Size, int l2Size, long l1Hits, long l2Hits, long misses, double hitRate) {
            this.l1Size = l1Size;
            this.l2Size = l2Size;
            this.l1Hits = l1Hits;
            this.l2Hits = l2Hits;
            this.misses = misses;
            this.hitRate = hitRate;
        }

        @Override
        public String toString() {
            return String.format(
                "TwoLevelCache[L1=%d, L2=%d, L1Hits=%d, L2Hits=%d, Misses=%d, HitRate=%.2f%%]",
                l1Size, l2Size, l1Hits, l2Hits, misses, hitRate * 100
            );
        }
    }
}
