package net.enchadd.listeners.support;

public final class SpearCombatSupport {

    private static final double MAX_DAMAGE_BONUS = 0.35;
    private static final double MAX_DAMAGE_REDUCTION = 0.35;
    private static final int MAX_DURATION_TICKS = 40;

    private SpearCombatSupport() {
    }

    public static double damageBonus(double damage, int level, double perLevel, double configuredMax, double factor) {
        if (!Double.isFinite(damage) || damage <= 0.0 || level <= 0
                || !Double.isFinite(perLevel) || perLevel <= 0.0
                || !Double.isFinite(configuredMax) || configuredMax <= 0.0
                || !Double.isFinite(factor) || factor <= 0.0) {
            return damage;
        }
        double bonus = Math.min(MAX_DAMAGE_BONUS, Math.min(configuredMax, perLevel * level * Math.min(1.0, factor)));
        double adjusted = damage * (1.0 + bonus);
        return Double.isFinite(adjusted) ? adjusted : damage;
    }

    public static double reduceDamage(double damage, int level, double perLevel, double configuredMax) {
        if (!Double.isFinite(damage) || damage <= 0.0 || level <= 0
                || !Double.isFinite(perLevel) || perLevel <= 0.0
                || !Double.isFinite(configuredMax) || configuredMax <= 0.0) {
            return damage;
        }
        double reduction = Math.min(MAX_DAMAGE_REDUCTION, Math.min(configuredMax, perLevel * level));
        double adjusted = damage * (1.0 - reduction);
        return Double.isFinite(adjusted) ? adjusted : damage;
    }

    public static double reachFactor(double distance, double minimum, double maximum) {
        if (!Double.isFinite(distance) || !Double.isFinite(minimum) || !Double.isFinite(maximum)
                || minimum < 0.0 || maximum <= minimum || distance <= minimum) {
            return 0.0;
        }
        return Math.min(1.0, (Math.min(distance, maximum) - minimum) / (maximum - minimum));
    }

    public static int durationTicks(int level, int maxLevel, int ticksPerLevel, int configuredMax) {
        if (level <= 0 || maxLevel <= 0 || ticksPerLevel <= 0 || configuredMax <= 0) {
            return 0;
        }
        int boundedLevel = Math.min(level, maxLevel);
        long duration = (long) boundedLevel * ticksPerLevel;
        return (int) Math.min(MAX_DURATION_TICKS, Math.min(configuredMax, duration));
    }
}