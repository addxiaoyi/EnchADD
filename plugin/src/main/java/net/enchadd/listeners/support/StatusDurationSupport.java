package net.enchadd.listeners.support;

import net.enchadd.utils.PerformanceUtils;

public final class StatusDurationSupport {
    private StatusDurationSupport() {
    }

    public static int onHit(double damage, int level, int maxLevel, int secondsPerLevel) {
        if (!Double.isFinite(damage) || damage <= 0.0 || level <= 0
                || maxLevel <= 0 || secondsPerLevel <= 0) return 0;
        return PerformanceUtils.calculateDurationTicksPerLevel(secondsPerLevel, Math.min(level, maxLevel));
    }
}
