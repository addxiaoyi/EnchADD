package net.enchadd.listeners.support;

final class GreedRules {
    private static final double MAX_MULTIPLIER = 2.0;
    private static final int MAX_SECONDS = 60;

    private GreedRules() {
    }

    static int experience(int original, int level, int maxLevel, double perLevel, double maximum) {
        if (original <= 0 || level <= 0 || maxLevel <= 0
                || !Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(maximum) || maximum <= 1) return original;
        double multiplier = Math.min(MAX_MULTIPLIER,
                Math.min(maximum, 1.0 + perLevel * Math.min(level, maxLevel)));
        return (int) Math.min(Integer.MAX_VALUE, Math.round(original * multiplier));
    }

    static double vulnerability(int level, int maxLevel, double perLevel, double maximum) {
        if (level <= 0 || maxLevel <= 0 || !Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(maximum) || maximum <= 0) return 1.0;
        return 1.0 + Math.min(MAX_MULTIPLIER - 1.0,
                Math.min(maximum, perLevel * Math.min(level, maxLevel)));
    }

    static int durationTicks(int level, int maxLevel, int secondsPerLevel) {
        if (level <= 0 || maxLevel <= 0 || secondsPerLevel <= 0) return 0;
        long seconds = (long) secondsPerLevel * Math.min(level, maxLevel);
        return (int) Math.min(MAX_SECONDS, seconds) * 20;
    }

    static double damage(double original, double scale) {
        if (!Double.isFinite(original) || original <= 0 || !Double.isFinite(scale) || scale <= 1) {
            return original;
        }
        double adjusted = original * Math.min(MAX_MULTIPLIER, scale);
        return Double.isFinite(adjusted) ? adjusted : original;
    }
}
