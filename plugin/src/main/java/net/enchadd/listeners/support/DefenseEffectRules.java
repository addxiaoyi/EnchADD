package net.enchadd.listeners.support;

final class DefenseEffectRules {
    private static final double MAX_DODGE_CHANCE = 0.6;
    private static final double MAX_REDUCTION = 0.8;
    private static final int MAX_DURATION_SECONDS = 120;

    private DefenseEffectRules() {
    }

    static double chance(int level, int maximum, double perLevel, double cap) {
        if (level <= 0 || maximum <= 0 || !Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(cap) || cap <= 0) return 0;
        return Math.min(MAX_DODGE_CHANCE, Math.min(cap, perLevel * Math.min(level, maximum)));
    }

    static double damage(double base, int level, int maximum, double perLevel) {
        if (!Double.isFinite(base) || base <= 0 || level <= 0 || maximum <= 0
                || !Double.isFinite(perLevel) || perLevel <= 0) return base;
        double reduction = Math.min(MAX_REDUCTION, perLevel * Math.min(level, maximum));
        return base * (1 - reduction);
    }

    static int seconds(int level, int maximum, int perLevel) {
        if (level <= 0 || maximum <= 0 || perLevel <= 0) return 0;
        return (int) Math.min(MAX_DURATION_SECONDS, (long) Math.min(level, maximum) * perLevel);
    }
}
