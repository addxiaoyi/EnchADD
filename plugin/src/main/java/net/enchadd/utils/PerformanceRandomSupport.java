package net.enchadd.utils;

import java.util.concurrent.ThreadLocalRandom;

final class PerformanceRandomSupport {

    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private PerformanceRandomSupport() {
    }

    static ThreadLocalRandom getRandom() {
        return RANDOM;
    }

    static boolean rollChance(double chance) {
        if (!Double.isFinite(chance)) {
            return false;
        }
        chance = Math.max(0.0d, Math.min(1.0d, chance));
        double effectiveChance = chance
                * SafetyModeManager.getChanceMultiplier()
                * EnchantExecutionBudgetManager.getCurrentChanceMultiplier();
        if (!Double.isFinite(effectiveChance)) {
            return false;
        }
        if (effectiveChance <= 0.0) {
            return false;
        }
        if (effectiveChance >= 1.0) {
            return true;
        }
        return RANDOM.nextDouble() < effectiveChance;
    }

    static boolean rollChanceWithLevel(double baseChance, int level, double maxChance) {
        if (!Double.isFinite(baseChance) || !Double.isFinite(maxChance) || level <= 0) {
            return false;
        }
        double chance = Math.min(1.0d, Math.min(Math.max(0.0d, maxChance), Math.max(0.0d, baseChance * level)));
        return rollChance(chance);
    }
}
