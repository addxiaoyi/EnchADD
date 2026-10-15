package net.enchadd.mod.common;

public final class ModBalanceRules {

    public static final int MIN_WEIGHT = 1;
    public static final int MAX_WEIGHT = 1;
    public static final double MAX_DAMAGE_BONUS = 0.35;
    public static final double MAX_DAMAGE_REDUCTION = 0.45;
    public static final int MAX_EFFECT_TICKS = 40;

    private ModBalanceRules() {
    }

    public static int weight(int configuredWeight) {
        return Math.max(MIN_WEIGHT, Math.min(MAX_WEIGHT, configuredWeight));
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

    public static double damageReduction(double damage, int level, double perLevel, double configuredMax) {
        if (!Double.isFinite(damage) || damage <= 0.0 || level <= 0
                || !Double.isFinite(perLevel) || perLevel <= 0.0
                || !Double.isFinite(configuredMax) || configuredMax <= 0.0) {
            return damage;
        }
        double reduction = Math.min(MAX_DAMAGE_REDUCTION, Math.min(configuredMax, perLevel * level));
        double adjusted = damage * (1.0 - reduction);
        return Double.isFinite(adjusted) ? adjusted : damage;
    }

    public static double distanceFactor(double distance, double minimum, double maximum) {
        if (!Double.isFinite(distance) || !Double.isFinite(minimum) || !Double.isFinite(maximum)
                || minimum < 0.0 || maximum <= minimum || distance <= minimum) {
            return 0.0;
        }
        return Math.min(1.0, (Math.min(distance, maximum) - minimum) / (maximum - minimum));
    }

    public static int durationTicks(int level, int ticksPerLevel, int configuredMax) {
        if (level <= 0 || ticksPerLevel <= 0 || configuredMax <= 0) {
            return 0;
        }
        long duration = (long) level * ticksPerLevel;
        return (int) Math.min(MAX_EFFECT_TICKS, Math.min(configuredMax, duration));
    }
}