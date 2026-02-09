package com.enadd.core.memory;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public final class ReferenceTracker {
    // Holder模式优化单例
    private static final class Holder {
        private static final ReferenceTracker INSTANCE = new ReferenceTracker();
    }

    // 预分配容量
    private final ConcurrentHashMap<String, WeakReference<?>> trackedReferences = new ConcurrentHashMap<>(128);
    private final ConcurrentHashMap<String, AtomicLong> referenceCount = new ConcurrentHashMap<>(128);
    private final AtomicInteger totalTracked = new AtomicInteger(0);
    private final AtomicInteger totalCollected = new AtomicInteger(0);

    private ReferenceTracker() {}

    public static ReferenceTracker getInstance() {
        return Holder.INSTANCE;
    }

    public <T> void track(String key, T object) {
        if (object == null) return;

        trackedReferences.put(key, new WeakReference<>(object));
        referenceCount.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        totalTracked.incrementAndGet();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        WeakReference<?> ref = trackedReferences.get(key);
        return ref != null ? (T) ref.get() : null;
    }

    public void untrack(String key) {
        trackedReferences.remove(key);
        referenceCount.remove(key);
    }

    public void incrementRefCount(String key) {
        referenceCount.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void decrementRefCount(String key) {
        AtomicLong count = referenceCount.get(key);
        if (count != null) {
            long newValue = count.decrementAndGet();
            if (newValue <= 0) {
                WeakReference<?> ref = trackedReferences.get(key);
                if (ref != null && ref.get() == null) {
                    trackedReferences.remove(key);
                    totalCollected.incrementAndGet();
                }
            }
        }
    }

    public long getRefCount(String key) {
        AtomicLong count = referenceCount.get(key);
        return count != null ? count.get() : 0;
    }

    public int getTrackedCount() {
        return totalTracked.get();
    }

    public int getCollectedCount() {
        return totalCollected.get();
    }

    public int getActiveCount() {
        return trackedReferences.size();
    }

    public void cleanup() {
        trackedReferences.entrySet().removeIf(entry -> entry.getValue().get() == null);
        totalCollected.addAndGet(totalTracked.get() - trackedReferences.size());
        totalTracked.set(trackedReferences.size());
    }

    public void clearAllRecords() {
        trackedReferences.clear();
        referenceCount.clear();
        totalTracked.set(0);
        totalCollected.set(0);
    }

    public ReferenceStats getStats() {
        return new ReferenceStats(
                totalTracked.get(),
                totalCollected.get(),
                trackedReferences.size(),
                referenceCount.size()
        );
    }

    public static final class ReferenceStats {
        private final int totalTracked;
        private final int totalCollected;
        private final int activeCount;
        private final int uniqueKeys;

        public ReferenceStats(int total, int collected, int active, int keys) {
            this.totalTracked = total;
            this.totalCollected = collected;
            this.activeCount = active;
            this.uniqueKeys = keys;
        }

        public int getTotalTracked() { return totalTracked; }
        public int getTotalCollected() { return totalCollected; }
        public int getActiveCount() { return activeCount; }
        public int getUniqueKeys() { return uniqueKeys; }
        public float getCollectionRate() { return totalTracked > 0 ? (float) totalCollected / totalTracked * 100 : 0; }
    }
}
