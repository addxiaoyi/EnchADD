package net.enchadd.listeners.support;

final class RangedDamageRules {
    private static final double MAX_BONUS = 0.75;
    private static final double MIN_STEADY_SPEED = 2.8;

    private RangedDamageRules() {
    }

    static boolean isSteadySpeed(double speed) {
        return Double.isFinite(speed) && speed >= MIN_STEADY_SPEED;
    }

    static double distanceFactor(double distance, double minimum, double maximum) {
        if (!Double.isFinite(distance) || !Double.isFinite(minimum) || !Double.isFinite(maximum)
                || minimum < 0 || maximum <= minimum || distance <= minimum) return 0;
        return (Math.min(distance, maximum) - minimum) / (maximum - minimum);
    }

    static double bonus(int level, int maximum, double perLevel, double factor) {
        if (level <= 0 || maximum <= 0 || !Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(factor) || factor <= 0) return 0;
        double weightedRate = perLevel * Math.min(1.0, factor);
        return Math.min(MAX_BONUS, weightedRate * Math.min(level, maximum));
    }

    static double damage(double base, double bonus) {
        if (!Double.isFinite(base) || base <= 0 || !Double.isFinite(bonus) || bonus <= 0) return base;
        double scaled = base * (1.0 + Math.min(MAX_BONUS, bonus));
        return Double.isFinite(scaled) ? scaled : base;
    }
}
