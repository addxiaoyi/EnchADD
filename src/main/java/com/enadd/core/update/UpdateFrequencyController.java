package com.enadd.core.update;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public final class UpdateFrequencyController {
    // Holder模式优化单例
    private static final class Holder {
        private static final UpdateFrequencyController INSTANCE = new UpdateFrequencyController();
    }

    // 预分配容量
    private final ConcurrentHashMap<String, AtomicInteger> updateCounters = new ConcurrentHashMap<>(256);
    private final ConcurrentHashMap<String, Float> updateIntervals = new ConcurrentHashMap<>(256);
    private final ConcurrentHashMap<String, Long> lastUpdateTimes = new ConcurrentHashMap<>(256);

    private volatile float globalUpdateInterval = 0.05f;
    private volatile int maxUpdatesPerTick = 100;
    private volatile boolean adaptiveEnabled = true;

    private UpdateFrequencyController() {}

    public static UpdateFrequencyController getInstance() {
        return Holder.INSTANCE;
    }

    public boolean shouldUpdate(String entityId, float deltaTime) {
        Float interval = updateIntervals.get(entityId);
        float effectiveInterval = interval != null ? interval : globalUpdateInterval;

        if (!adaptiveEnabled) {
            return true;
        }

        Long lastTime = lastUpdateTimes.get(entityId);
        long currentTime = System.currentTimeMillis();

        if (lastTime == null) {
            lastUpdateTimes.put(entityId, currentTime);
            return true;
        }

        long elapsed = currentTime - lastTime;
        boolean shouldUpdate = elapsed >= effectiveInterval * 1000;

        if (shouldUpdate) {
            lastUpdateTimes.put(entityId, currentTime);
        }

        return shouldUpdate;
    }

    public void setUpdateInterval(String entityId, float interval) {
        updateIntervals.put(entityId, interval);
    }

    public void setGlobalUpdateInterval(float interval) {
        this.globalUpdateInterval = interval;
    }

    public void setMaxUpdatesPerTick(int max) {
        this.maxUpdatesPerTick = max;
    }

    public float getGlobalUpdateInterval() {
        return globalUpdateInterval;
    }

    public int getMaxUpdatesPerTick() {
        return maxUpdatesPerTick;
    }

    public void incrementUpdateCount(String entityId) {
        updateCounters.computeIfAbsent(entityId, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public int getUpdateCount(String entityId) {
        return updateCounters.getOrDefault(entityId, new AtomicInteger(0)).get();
    }

    public void resetCounter(String entityId) {
        AtomicInteger counter = updateCounters.get(entityId);
        if (counter != null) {
            counter.set(0);
        }
    }

    public void clearEntity(String entityId) {
        updateCounters.remove(entityId);
        updateIntervals.remove(entityId);
        lastUpdateTimes.remove(entityId);
    }

    public void clearAll() {
        updateCounters.clear();
        updateIntervals.clear();
        lastUpdateTimes.clear();
    }

    public int getTotalUpdateCount() {
        return updateCounters.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
    }

    public void setAdaptiveEnabled(boolean enabled) {
        this.adaptiveEnabled = enabled;
    }

    public boolean isAdaptiveEnabled() {
        return adaptiveEnabled;
    }

    public UpdateStats getStats() {
        int total = getTotalUpdateCount();
        int unique = updateCounters.size();

        return new UpdateStats(
                globalUpdateInterval,
                maxUpdatesPerTick,
                total,
                unique,
                adaptiveEnabled
        );
    }

    public static final class UpdateStats {
        private final float globalInterval;
        private final int maxPerTick;
        private final int totalUpdates;
        private final int uniqueEntities;
        private final boolean adaptive;

        public UpdateStats(float interval, int max, int total, int unique, boolean adaptive) {
            this.globalInterval = interval;
            this.maxPerTick = max;
            this.totalUpdates = total;
            this.uniqueEntities = unique;
            this.adaptive = adaptive;
        }

        public float getGlobalInterval() { return globalInterval; }
        public int getMaxPerTick() { return maxPerTick; }
        public int getTotalUpdates() { return totalUpdates; }
        public int getUniqueEntities() { return uniqueEntities; }
        public boolean isAdaptive() { return adaptive; }
        public float getAvgUpdatesPerEntity() { return uniqueEntities > 0 ? (float) totalUpdates / uniqueEntities : 0; }
    }
}
