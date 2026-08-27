package net.enchadd.utils;

import net.enchadd.EnchADDConfig;

/**
 * Lightweight runtime error counter for health monitoring and CI metrics.
 */
public final class RuntimeErrorTracker {

    private static final RuntimeErrorTrackerState STATE = new RuntimeErrorTrackerState();

    private RuntimeErrorTracker() {
    }

    public static long recordError(String source) {
        long value = STATE.increment();
        if (EnchADDConfig.DEBUG) {
            org.bukkit.Bukkit.getLogger().warning(RuntimeErrorTrackerLogSupport.format(source, value));
        }
        return value;
    }

    public static long getTotalErrors() {
        return STATE.getTotalErrors();
    }

    public static void reset() {
        STATE.reset();
    }
}
