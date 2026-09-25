package net.enchadd.listeners.support;

import org.bukkit.util.Vector;

final class TideRunnerRules {
    private static final int MIN_DURATION_TICKS = 40;
    private static final int MAX_DURATION_TICKS = 2400;
    private static final double MIN_MOVEMENT_SQUARED = 0.01;

    private TideRunnerRules() {
    }

    static int duration(int level, int maxLevel, int ticksPerLevel) {
        if (level <= 0 || maxLevel <= 0 || ticksPerLevel <= 0) return 0;
        long ticks = (long) Math.min(level, maxLevel) * ticksPerLevel;
        return (int) Math.min(MAX_DURATION_TICKS, Math.max(MIN_DURATION_TICKS, ticks));
    }

    static int amplifier(int level, int maxLevel, double perLevel) {
        if (level <= 0 || maxLevel <= 0 || !Double.isFinite(perLevel) || perLevel <= 0) return 0;
        double scaled = Math.min(1.0, perLevel * Math.min(level, maxLevel));
        return (int) Math.round(scaled);
    }

    static boolean hasMovement(Vector delta) {
        double squared = delta.lengthSquared();
        return EffectMotionSupport.finite(delta) && Double.isFinite(squared) && squared >= MIN_MOVEMENT_SQUARED;
    }
}
