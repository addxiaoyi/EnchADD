package net.enchadd.listeners.support;

public final class SurvivalHealthSupport {
    private static final double RESCUE_HEALTH_FRACTION = 0.20;
    private static final double MIN_RESCUE_HEALTH = 2.0;
    private static final double MAX_LOW_HEALTH_FRACTION = 0.50;

    private SurvivalHealthSupport() {
    }

    public static boolean isLethal(double health, double damage) {
        return Double.isFinite(health) && health > 0.0
                && Double.isFinite(damage) && damage > 0.0 && damage >= health;
    }

    public static double rescueHealth(double maxHealth) {
        if (!Double.isFinite(maxHealth) || maxHealth <= 0.0) return 0.0;
        return Math.min(maxHealth, Math.max(MIN_RESCUE_HEALTH, maxHealth * RESCUE_HEALTH_FRACTION));
    }

    public static boolean survivesBelowThreshold(double health, double damage, double maxHealth,
                                                  double threshold) {
        if (!Double.isFinite(health) || health <= 0.0 || !Double.isFinite(damage) || damage <= 0.0
                || !Double.isFinite(maxHealth) || maxHealth <= 0.0
                || !Double.isFinite(threshold) || threshold <= 0.0) return false;
        double remaining = health - damage;
        return remaining > 0.0 && remaining <= Math.min(maxHealth * MAX_LOW_HEALTH_FRACTION, threshold);
    }

    public static double reducedHealing(double amount, double scale) {
        if (!Double.isFinite(amount) || amount <= 0.0 || !Double.isFinite(scale)) return amount;
        return amount * Math.max(0.0, Math.min(1.0, scale));
    }
}
