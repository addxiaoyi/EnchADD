package net.enchadd.utils;

import java.util.concurrent.atomic.AtomicLong;

final class RuntimeErrorTrackerState {

    private final AtomicLong totalErrors = new AtomicLong(0L);

    long increment() {
        return totalErrors.incrementAndGet();
    }

    long getTotalErrors() {
        return totalErrors.get();
    }

    void reset() {
        totalErrors.set(0L);
    }
}
