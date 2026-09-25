package net.enchadd.listeners.support;

public final class ReboundDurabilitySupport {
    private static final double MAX_REFUND_CHANCE = 0.45;

    private ReboundDurabilitySupport() {
    }

    public static double refundChance(int damage, int level, int maxLevel, double perLevel) {
        if (damage <= 0 || level <= 0 || maxLevel <= 0
                || !Double.isFinite(perLevel) || perLevel <= 0.0) return 0.0;
        return Math.min(MAX_REFUND_CHANCE, perLevel * Math.min(level, maxLevel));
    }
}
