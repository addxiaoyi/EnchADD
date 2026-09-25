package net.enchadd.listeners.support;

final class HomewardRules {
    private static final int MAX_EFFECT_SECONDS = 120;
    private static final int MAX_WINDOW_TICKS = MAX_EFFECT_SECONDS * 20;
    private static final double MAX_FALL_REDUCTION = 0.90;

    private HomewardRules() {
    }

    static int effectiveLevel(int level, int maxLevel) {
        return Math.max(0, Math.min(level, maxLevel));
    }

    static boolean hasDamage(boolean cancelled, double damage, double finalDamage) {
        return !cancelled && Double.isFinite(damage) && damage > 0
                && Double.isFinite(finalDamage) && finalDamage > 0;
    }

    static int speedSeconds(int level, int maxLevel, int secondsPerLevel) {
        if (secondsPerLevel <= 0) return 0;
        long seconds = (long) effectiveLevel(level, maxLevel) * secondsPerLevel;
        return (int) Math.min(MAX_EFFECT_SECONDS, seconds);
    }

    static int windowTicks(int configuredTicks) {
        return Math.max(0, Math.min(MAX_WINDOW_TICKS, configuredTicks));
    }

    static double fallReduction(int level, int maxLevel, double perLevel, double maximum) {
        if (!Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(maximum) || maximum <= 0) return 0;
        double reduction = Math.min(Math.min(MAX_FALL_REDUCTION, maximum),
                effectiveLevel(level, maxLevel) * perLevel);
        return 1 - reduction < 1 ? reduction : 0;
    }

    static double reducedFallDamage(double damage, double finalDamage, double reduction) {
        if (!hasDamage(false, damage, finalDamage)
                || !Double.isFinite(reduction) || reduction <= 0) return damage;
        return damage * (1 - Math.min(MAX_FALL_REDUCTION, reduction));
    }

    static int windowLevel(int incoming, int previous, boolean active, int maxLevel) {
        int retained = active ? previous : 0;
        return effectiveLevel(Math.max(incoming, retained), maxLevel);
    }

    static boolean canRefreshWindow(int incoming, int previous, boolean active, int maxLevel) {
        int level = effectiveLevel(incoming, maxLevel);
        return level > 0 && (!active || level >= effectiveLevel(previous, maxLevel));
    }
}
