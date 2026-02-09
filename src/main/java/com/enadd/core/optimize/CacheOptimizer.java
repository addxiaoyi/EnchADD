package com.enadd.core.optimize;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class CacheOptimizer {
    private static final Logger LOGGER = Logger.getLogger(CacheOptimizer.class.getName());

    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);
    private static final AtomicLong TOTAL_HITS = new AtomicLong(0);
    private static final AtomicLong TOTAL_MISSES = new AtomicLong(0);
    private static final AtomicLong TOTAL_EVICTIONS = new AtomicLong(0);
    private static final AtomicLong TOTAL_SIZE = new AtomicLong(0);

    private static ConcurrentHashMap<String, CacheLevel> caches;
    private static ScheduledExecutorService maintenanceScheduler;
    private static ScheduledExecutorService warmingScheduler;

    private static volatile double maxMemoryPercent = 25.0;
    private static volatile int defaultTTLSeconds = 300;

    /**
     * 获取默认 TTL（秒）
     */
    public static int getDefaultTTLSeconds() {
        return defaultTTLSeconds;
    }
    private static volatile int cleanupIntervalSeconds = 60;
    private static volatile boolean warmingEnabled = true;

    private static final int MAX_CACHES = 20;

    private static final Map<String, Object> warmingData = new ConcurrentHashMap<>();

    private CacheOptimizer() {}

    public static synchronized void initialize() {
        if (ENABLED.get()) {
            return;
        }

        caches = new ConcurrentHashMap<>();

        maintenanceScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CacheOptimizer-Maintenance");
            t.setDaemon(true);
            return t;
        });

        maintenanceScheduler.scheduleAtFixedRate(CacheOptimizer::performMaintenance,
                                                  cleanupIntervalSeconds,
                                                  cleanupIntervalSeconds,
                                                  TimeUnit.SECONDS);

        if (warmingEnabled) {
            warmingScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "CacheOptimizer-Warming");
                t.setDaemon(true);
                return t;
            });

            warmingScheduler.scheduleAtFixedRate(CacheOptimizer::warmCaches,
                                                 300000L,
                                                 300000L,
                                                 TimeUnit.MILLISECONDS);
        }

        createDefaultCache("default", 1000, 3600);
        createDefaultCache("player", 500, 1800);
        createDefaultCache("chunk", 2000, 900);
        createDefaultCache("entity", 1000, 600);
        createDefaultCache("config", 100, 7200);

        ENABLED.set(true);
        LOGGER.log(Level.INFO, "CacheOptimizer initialized with {0} cache levels", MAX_CACHES);
    }

    public static String createDefaultCache(String name, int maxSize, int ttlSeconds) {
        if (!ENABLED.get()) {
            return null;
        }

        if (caches.size() >= MAX_CACHES) {
            LOGGER.warning("Maximum cache count reached");
            return null;
        }

        CacheLevel cache = new CacheLevel(name, maxSize, ttlSeconds);
        caches.put(name, cache);

        return name;
    }

    public static <T> Optional<T> get(String cacheName, String key, Class<T> type) {
        if (!ENABLED.get()) {
            return Optional.empty();
        }

        CacheLevel cache = caches.get(cacheName);
        if (cache == null) {
            return Optional.empty();
        }

        return cache.get(key, type);
    }

    public static <T> void put(String cacheName, String key, T value) {
        if (!ENABLED.get()) {
            return;
        }

        CacheLevel cache = caches.get(cacheName);
        if (cache == null) {
            return;
        }

        cache.put(key, value);
    }

    public static <T> T getOrCompute(String cacheName, String key, Callable<T> computation, Class<T> type) {
        if (!ENABLED.get()) {
            try {
                return computation.call();
            } catch (Exception e) {
                return null;
            }
        }

        CacheLevel cache = caches.get(cacheName);
        if (cache == null) {
            try {
                return computation.call();
            } catch (Exception e) {
                return null;
            }
        }

        Optional<T> cached = cache.get(key, type);
        if (cached.isPresent()) {
            TOTAL_HITS.incrementAndGet();
            return cached.get();
        }

        TOTAL_MISSES.incrementAndGet();

        try {
            T value = computation.call();
            if (value != null) {
                cache.put(key, value);
            }
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    public static void invalidate(String cacheName, String key) {
        CacheLevel cache = caches.get(cacheName);
        if (cache != null) {
            cache.invalidate(key);
        }
    }

    public static void invalidateAll(String cacheName) {
        CacheLevel cache = caches.get(cacheName);
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    public static void preload(String cacheName, String key, Object value) {
        CacheLevel cache = caches.get(cacheName);
        if (cache != null && value != null) {
            cache.put(key, value);
            warmingData.put(cacheName + ":" + key, value);
        }
    }

    public static void registerWarmingData(String cacheName, String key, Callable<?> dataLoader) {
        String compositeKey = cacheName + ":" + key;
        warmingData.put(compositeKey, dataLoader);
    }

    private static void warmCaches() {
        if (!warmingEnabled || !ENABLED.get()) return;

        warmingData.forEach((compositeKey, data) -> {
            try {
                String[] parts = compositeKey.split(":", 2);
                if (parts.length == 2) {
                    String cacheName = parts[0];
                    String key = parts[1];

                    if (data instanceof Callable) {
                        Object result = ((Callable<?>) data).call();
                        if (result != null) {
                            CacheLevel cache = caches.get(cacheName);
                            if (cache != null) {
                                cache.put(key, result);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to warm cache for key: " + compositeKey, e);
            }
        });
    }

    private static void performMaintenance() {
        if (!ENABLED.get()) return;

        long currentSize = TOTAL_SIZE.get();
        long maxSize = calculateMaxMemorySize();

        if (currentSize > maxSize) {
            evictLRU(currentSize - maxSize);
        }

        caches.forEach((name, cache) -> cache.cleanupExpired());
    }

    private static long calculateMaxMemorySize() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        return (long) (maxMemory * (maxMemoryPercent / 100.0));
    }

    private static void evictLRU(long bytesToEvict) {
        List<CacheLevel.CacheEntry<?>> entries = new ArrayList<>();

        caches.forEach((name, cache) -> {
            cache.getAllEntries().forEach(entry -> entries.add(entry));
        });

        entries.sort(Comparator.comparingLong(CacheLevel.CacheEntry::getLastAccessTime));

        long evicted = 0;
        for (CacheLevel.CacheEntry<?> entry : entries) {
            if (evicted >= bytesToEvict) break;

            String[] parts = entry.getKey().split(":", 2);
            if (parts.length == 2) {
                CacheLevel cache = caches.get(parts[0]);
                if (cache != null) {
                    cache.invalidate(parts[1]);
                    evicted += entry.getSize();
                    TOTAL_EVICTIONS.incrementAndGet();
                }
            }
        }
    }

    public static void setMaxMemoryPercent(double percent) {
        maxMemoryPercent = Math.max(5.0, Math.min(50.0, percent));
    }

    public static void setDefaultTTL(int seconds) {
        defaultTTLSeconds = Math.max(60, Math.max(86400, seconds));
    }

    public static void setCleanupInterval(int seconds) {
        cleanupIntervalSeconds = Math.max(10, Math.min(3600, seconds));
    }

    public static void setWarmingEnabled(boolean enabled) {
        warmingEnabled = enabled;
    }

    public static CacheOptimizerReport getReport() {
        Map<String, CacheLevel.CacheReport> cacheReports = new LinkedHashMap<>();
        AtomicLong totalEntries = new AtomicLong(0);
        AtomicLong totalMemoryBytes = new AtomicLong(0);

        caches.forEach((name, cache) -> {
            CacheLevel.CacheReport report = cache.getReport();
            cacheReports.put(name, report);
            totalEntries.addAndGet(report.getEntryCount());
            totalMemoryBytes.addAndGet(report.getMemoryBytes());
        });

        long totalHits = TOTAL_HITS.get();
        long totalAccesses = totalHits + TOTAL_MISSES.get();
        double hitRate = totalAccesses > 0 ? (double) totalHits / totalAccesses * 100 : 0;

        return new CacheOptimizerReport(
            ENABLED.get(),
            cacheReports,
            totalEntries.get(),
            totalMemoryBytes.get(),
            totalHits,
            TOTAL_MISSES.get(),
            hitRate,
            TOTAL_EVICTIONS.get(),
            maxMemoryPercent,
            warmingEnabled
        );
    }

    public static double getHitRate() {
        long totalHits = TOTAL_HITS.get();
        long totalAccesses = totalHits + TOTAL_MISSES.get();
        return totalAccesses > 0 ? (double) totalHits / totalAccesses * 100 : 0;
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static synchronized void shutdown() {
        if (!ENABLED.get()) return;

        ENABLED.set(false);

        if (maintenanceScheduler != null && !maintenanceScheduler.isShutdown()) {
            maintenanceScheduler.shutdown();
            try {
                if (!maintenanceScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    maintenanceScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                maintenanceScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (warmingScheduler != null && !warmingScheduler.isShutdown()) {
            warmingScheduler.shutdown();
            try {
                if (!warmingScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    warmingScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                warmingScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        caches.forEach((name, cache) -> cache.invalidateAll());
        caches.clear();
        warmingData.clear();

        LOGGER.info("CacheOptimizer shutdown complete");
    }

    private static final class CacheLevel {
        private final String name;
        private final int maxSize;
        private final int ttlSeconds;
        private final ConcurrentHashMap<String, CacheEntry<?>> storage;
        private final LinkedHashMap<String, Long> accessOrder;
        private final ReadWriteLock lock;
        private final AtomicInteger size;

        public CacheLevel(String name, int maxSize, int ttlSeconds) {
            this.name = name;
            this.maxSize = maxSize;
            this.ttlSeconds = ttlSeconds;
            this.storage = new ConcurrentHashMap<>();
            this.accessOrder = new LinkedHashMap<>(100, 0.75f, true);
            this.lock = new ReentrantReadWriteLock();
            this.size = new AtomicInteger(0);
        }

        public <T> Optional<T> get(String key, Class<T> type) {
            CacheEntry<?> entry = storage.get(key);
            if (entry == null || entry.isExpired()) {
                if (entry != null) {
                    storage.remove(key);
                    size.decrementAndGet();
                }
                return Optional.empty();
            }

            lock.writeLock().lock();
            try {
                accessOrder.remove(key);
                accessOrder.put(key, System.currentTimeMillis());
            } finally {
                lock.writeLock().unlock();
            }

            return type.isInstance(entry.getValue())
                ? Optional.of(type.cast(entry.getValue()))
                : Optional.empty();
        }

        public <T> void put(String key, T value) {
            if (key == null || value == null) return;

            lock.writeLock().lock();
            try {
                storage.compute(key, (k, existing) -> {
                    if (existing != null) {
                        accessOrder.remove(key);
                        return new CacheEntry<>(key, value, ttlSeconds);
                    }

                    while (size.get() >= maxSize) {
                        evictOldest();
                    }

                    accessOrder.put(key, System.currentTimeMillis());
                    size.incrementAndGet();
                    return new CacheEntry<>(key, value, ttlSeconds);
                });
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void invalidate(String key) {
            lock.writeLock().lock();
            try {
                if (storage.remove(key) != null) {
                    accessOrder.remove(key);
                    size.decrementAndGet();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void invalidateAll() {
            lock.writeLock().lock();
            try {
                storage.clear();
                accessOrder.clear();
                size.set(0);
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void cleanupExpired() {
            long expiryTime = System.currentTimeMillis() - (ttlSeconds * 1000L);
            List<String> expiredKeys = new ArrayList<>();

            storage.forEach((key, entry) -> {
                if (entry.getCreationTime() < expiryTime) {
                    expiredKeys.add(key);
                }
            });

            expiredKeys.forEach(this::invalidate);
        }

        private void evictOldest() {
            if (accessOrder.isEmpty()) return;
            String oldestKey = accessOrder.entrySet().iterator().next().getKey();
            invalidate(oldestKey);
        }

        public List<CacheEntry<?>> getAllEntries() {
            return new ArrayList<>(storage.values());
        }

        public CacheReport getReport() {
            int entryCount = size.get();
            long memoryBytes = storage.values().stream().mapToLong(CacheEntry::getSize).sum();
            return new CacheReport(name, entryCount, memoryBytes, maxSize, ttlSeconds);
        }

        private static final class CacheEntry<T> {
            private final String key;
            private final T value;
            private final long creationTime;
            private final long ttlMillis;
            private long lastAccessTime;

            public CacheEntry(String key, T value, int ttlSeconds) {
                this.key = key;
                this.value = value;
                this.creationTime = System.currentTimeMillis();
                this.ttlMillis = ttlSeconds * 1000L;
                this.lastAccessTime = this.creationTime;
            }

            public String getKey() { return key; }
            public T getValue() { return value; }
            public long getCreationTime() { return creationTime; }
            public long getLastAccessTime() { return lastAccessTime; }

            public boolean isExpired() {
                return System.currentTimeMillis() - creationTime > ttlMillis;
            }

            public long getSize() {
                return 32 + key.length() * 2 + estimateSize(value);
            }

            private long estimateSize(Object obj) {
                if (obj == null) return 0;
                if (obj instanceof String) return ((String) obj).length() * 2;
                if (obj instanceof Number) return 16;
                if (obj instanceof Collection) return ((Collection<?>) obj).size() * 16;
                if (obj instanceof Map) return ((Map<?,?>) obj).size() * 32;
                return 64;
            }
        }

        public static final class CacheReport {
            private final String name;
            private final int entryCount;
            private final long memoryBytes;
            private final int maxSize;
            private final int ttlSeconds;

            public CacheReport(String name, int entryCount, long memoryBytes,
                             int maxSize, int ttlSeconds) {
                this.name = name;
                this.entryCount = entryCount;
                this.memoryBytes = memoryBytes;
                this.maxSize = maxSize;
                this.ttlSeconds = ttlSeconds;
            }

            @SuppressWarnings("unused")
            public String getName() { return name; }
            public int getEntryCount() { return entryCount; }
            public long getMemoryBytes() { return memoryBytes; }
            @SuppressWarnings("unused")
            public int getTtlSeconds() { return ttlSeconds; }
            public double getUsagePercent() {
                return maxSize > 0 ? (double) entryCount / maxSize * 100 : 0;
            }
        }
    }

    public static final class CacheOptimizerReport {
        private final boolean enabled;
        private final Map<String, CacheLevel.CacheReport> cacheReports;
        private final long totalEntries;
        private final long totalMemoryBytes;
        private final long totalHits;
        private final long totalMisses;
        private final double hitRate;
        private final long totalEvictions;
        private final double maxMemoryPercent;
        private final boolean warmingEnabled;

        public CacheOptimizerReport(boolean enabled, Map<String, CacheLevel.CacheReport> cacheReports,
                                   long totalEntries, long totalMemoryBytes,
                                   long totalHits, long totalMisses, double hitRate,
                                   long totalEvictions, double maxMemoryPercent,
                                   boolean warmingEnabled) {
            this.enabled = enabled;
            this.cacheReports = cacheReports;
            this.totalEntries = totalEntries;
            this.totalMemoryBytes = totalMemoryBytes;
            this.totalHits = totalHits;
            this.totalMisses = totalMisses;
            this.hitRate = hitRate;
            this.totalEvictions = totalEvictions;
            this.maxMemoryPercent = maxMemoryPercent;
            this.warmingEnabled = warmingEnabled;
        }

        public boolean isEnabled() { return enabled; }
        public Map<String, CacheLevel.CacheReport> getCacheReports() { return cacheReports; }
        public long getTotalEntries() { return totalEntries; }
        public long getTotalMemoryBytes() { return totalMemoryBytes; }
        public long getTotalHits() { return totalHits; }
        public long getTotalMisses() { return totalMisses; }
        public double getHitRate() { return hitRate; }
        public long getTotalEvictions() { return totalEvictions; }
        public double getMaxMemoryPercent() { return maxMemoryPercent; }
        public boolean isWarmingEnabled() { return warmingEnabled; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Cache Optimizer Report ===\n");
            sb.append("Enabled: ").append(enabled).append("\n");
            sb.append("Total Entries: ").append(totalEntries).append("\n");
            sb.append("Total Memory: ").append(totalMemoryBytes / 1024).append("KB\n");
            sb.append("Total Hits: ").append(totalHits).append("\n");
            sb.append("Total Misses: ").append(totalMisses).append("\n");
            sb.append("Hit Rate: ").append(String.format("%.2f%%", hitRate)).append("\n");
            sb.append("Total Evictions: ").append(totalEvictions).append("\n");
            sb.append("Max Memory %: ").append(maxMemoryPercent).append("\n");
            sb.append("Warming Enabled: ").append(warmingEnabled).append("\n");
            sb.append("Cache Details:\n");
            cacheReports.forEach((name, report) ->
                sb.append("  - ").append(name).append(": ").append(report.getEntryCount())
                  .append(" entries (").append(String.format("%.1f%%", report.getUsagePercent()))
                  .append("), ").append(report.getMemoryBytes() / 1024).append("KB\n"));
            return sb.toString();
        }
    }
}
