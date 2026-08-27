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
        double effectiveChance = chance
                * SafetyModeManager.getChanceMultiplier()
                * EnchantExecutionBudgetManager.getCurrentChanceMultiplier();
        if (effectiveChance <= 0.0) {
            return false;
        }
        if (effectiveChance >= 1.0) {
            return true;
        }
        return RANDOM.nextDouble() < effectiveChance;
    }

    static boolean rollChanceWithLevel(double baseChance, int level, double maxChance) {
        double chance = Math.min(maxChance, baseChance * level);
        return rollChance(chance);
    }
}
