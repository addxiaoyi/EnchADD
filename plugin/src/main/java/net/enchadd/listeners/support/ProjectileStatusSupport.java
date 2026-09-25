package net.enchadd.listeners.support;


final class ProjectileStatusSupport {
    private static final double MAX_TRIGGER_CHANCE = 0.75;

    private ProjectileStatusSupport() {
    }

    static int durationTicks(double damage, int level, int maxLevel, int secondsPerLevel) {
        return StatusDurationSupport.onHit(damage, level, maxLevel, secondsPerLevel);
    }

    static double chance(double baseChance, int multiplier, double configuredMax) {
        if (!Double.isFinite(baseChance) || baseChance <= 0.0 || multiplier <= 0
                || !Double.isFinite(configuredMax) || configuredMax <= 0.0) return 0.0;
        return Math.min(Math.min(MAX_TRIGGER_CHANCE, configuredMax), baseChance * multiplier);
    }
}
