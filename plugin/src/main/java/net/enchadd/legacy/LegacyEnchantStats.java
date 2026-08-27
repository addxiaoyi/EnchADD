package net.enchadd.legacy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class LegacyEnchantStats {

    private static final Map<String, AtomicLong> keyHits = new ConcurrentHashMap<>();
    private static final AtomicLong totalSanitized = new AtomicLong(0L);

    private LegacyEnchantStats() {
    }

    public static void recordHit(String legacyKey) {
        totalSanitized.incrementAndGet();
        keyHits.computeIfAbsent(legacyKey, ignored -> new AtomicLong(0L)).incrementAndGet();
    }

    public static long getTotalSanitized() {
        return totalSanitized.get();
    }

    public static Map<String, Long> snapshotByKey() {
        Map<String, Long> snapshot = new java.util.LinkedHashMap<>();
        keyHits.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .forEach(entry -> snapshot.put(entry.getKey(), entry.getValue().get()));
        return snapshot;
    }

    public static void reset() {
        totalSanitized.set(0L);
        keyHits.clear();
    }
}
