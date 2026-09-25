package net.enchadd.listeners.support;

final class CursePenaltySupport {
    private static final double MAX_DAMAGE_MULTIPLIER = 2.0;

    private CursePenaltySupport() {
    }

    static double backfireChance(int level, int maxLevel, double perLevel, double maxChance) {
        if (level <= 0 || maxLevel <= 0 || !Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(maxChance) || maxChance <= 0) return 0;
        return Math.min(Math.min(1.0, maxChance), perLevel * Math.min(level, maxLevel));
    }

    static double selfDamage(double damage, double multiplier) {
        if (!Double.isFinite(damage) || damage <= 0
                || !Double.isFinite(multiplier) || multiplier <= 0) return 0;
        double penalty = damage * Math.min(MAX_DAMAGE_MULTIPLIER, multiplier);
        return Double.isFinite(penalty) ? penalty : 0;
    }

    static int durabilityDamage(int damage, int level, int maxLevel, double perLevel, double maxMultiplier) {
        if (damage <= 0 || level <= 0 || maxLevel <= 0
                || !Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(maxMultiplier) || maxMultiplier <= 1) return damage;
        double multiplier = Math.min(Math.min(MAX_DAMAGE_MULTIPLIER, maxMultiplier),
                1.0 + perLevel * Math.min(level, maxLevel));
        return (int) Math.round(Math.min(Integer.MAX_VALUE, damage * multiplier));
    }
}
