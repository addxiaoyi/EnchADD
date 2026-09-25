package net.enchadd.listeners.support;

public final class EnchantDamageSupport {
    private static final double MAX_FLAT_BONUS = 4.0;
    private static final double MAX_EXECUTIONER_MULTIPLIER = 2.5;

    private EnchantDamageSupport() {
    }

    public static double bonusDamage(int level, double perLevel, double configuredMax) {
        if (level <= 0 || !Double.isFinite(perLevel) || perLevel <= 0.0
                || !Double.isFinite(configuredMax) || configuredMax <= 0.0) {
            return 0.0;
        }
        return Math.min(Math.min(MAX_FLAT_BONUS, configuredMax), perLevel * level);
    }

    public static double addBonus(double damage, double bonus) {
        // A blocked or zero-damage hit must not become damaging through an enchantment.
        if (!Double.isFinite(damage) || damage <= 0.0
                || !Double.isFinite(bonus) || bonus <= 0.0) {
            return damage;
        }
        double adjusted = damage + Math.min(MAX_FLAT_BONUS, bonus);
        return Double.isFinite(adjusted) ? adjusted : damage;
    }

    public static double quellDamage(double damage, double finalDamage, int level, int maxLevel, double perLevel) {
        if (!Double.isFinite(damage) || damage <= 0.0
                || !Double.isFinite(finalDamage) || finalDamage <= 0.0
                || level <= 0 || maxLevel <= 0 || !Double.isFinite(perLevel) || perLevel <= 0.0) {
            return damage;
        }
        double reduction = Math.min(0.5, perLevel * Math.min(level, maxLevel));
        return damage * (1.0 - reduction);
    }

    public static double executionerDamage(double damage, int level, double perLevel,
                                          double health, double maxHealth, double threshold) {
        if (!Double.isFinite(damage) || damage <= 0.0 || level <= 0
                || !Double.isFinite(perLevel) || perLevel <= 0.0
                || !Double.isFinite(health) || health <= 0.0
                || !Double.isFinite(maxHealth) || maxHealth <= 0.0
                || !Double.isFinite(threshold) || threshold <= 0.0) {
            return damage;
        }
        if (health / maxHealth >= Math.min(1.0, threshold)) {
            return damage;
        }
        double multiplier = Math.min(MAX_EXECUTIONER_MULTIPLIER, 1.0 + perLevel * level);
        double adjusted = damage * multiplier;
        return Double.isFinite(adjusted) ? adjusted : damage;
    }
}
