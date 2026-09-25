package net.enchadd.listeners.support;

public final class ThirstRules {
    private static final double MAX_REGEN_REDUCTION = 0.9;
    private static final int MAX_EXTRA_HUNGER_LOSS = 4;

    private ThirstRules() {
    }

    public static boolean isCombatHit(boolean cancelled, double finalDamage) {
        return !cancelled && Double.isFinite(finalDamage) && finalDamage > 0;
    }

    static int effectiveLevel(int level, int maximum) {
        return Math.max(0, Math.min(level, maximum));
    }

    static double healing(double amount, int level, int maximum, double perLevel) {
        int boundedLevel = effectiveLevel(level, maximum);
        if (!Double.isFinite(amount) || amount <= 0 || boundedLevel == 0
                || !Double.isFinite(perLevel) || perLevel <= 0) return amount;
        double reduction = Math.min(MAX_REGEN_REDUCTION, perLevel * boundedLevel);
        return SurvivalHealthSupport.reducedHealing(amount, 1.0 - reduction);
    }

    static int food(int current, int proposed, int level, int maximum, int perLevel) {
        int boundedLevel = effectiveLevel(level, maximum);
        if (proposed < 0 || proposed >= current || boundedLevel == 0 || perLevel <= 0) return proposed;
        int extraLoss = (int) Math.min(MAX_EXTRA_HUNGER_LOSS, (long) perLevel * boundedLevel);
        return Math.max(0, proposed - extraLoss);
    }
}
