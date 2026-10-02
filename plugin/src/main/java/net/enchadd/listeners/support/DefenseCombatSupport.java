package net.enchadd.listeners.support;

public final class DefenseCombatSupport {

    private static final double MAX_REDUCTION = 0.45;

    private DefenseCombatSupport() {
    }

    public static double reduce(double damage, int level, int maxLevel, double perLevel, double configuredMax) {
        if (!Double.isFinite(damage) || damage <= 0.0 || level <= 0 || maxLevel <= 0
                || !Double.isFinite(perLevel) || perLevel <= 0.0
                || !Double.isFinite(configuredMax) || configuredMax <= 0.0) {
            return damage;
        }
        double reduction = Math.min(MAX_REDUCTION, Math.min(configuredMax, perLevel * Math.min(level, maxLevel)));
        double adjusted = damage * (1.0 - reduction);
        return Double.isFinite(adjusted) ? adjusted : damage;
    }
}