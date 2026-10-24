package net.enchadd.mod.common;

final class ExperienceRewardRules {
    private static final int MAX_LEVEL = 3;
    private static final double GREED_BONUS_PER_LEVEL = 0.30;
    private static final double MAX_GREED_MULTIPLIER = 3.0;
    private static final double MISFORTUNE_PENALTY_PER_LEVEL = 0.25;
    private static final double MAX_MISFORTUNE_PENALTY = 0.90;

    private ExperienceRewardRules() {}

    static int adjust(int baseExperience, int greedLevel, int misfortuneLevel,
                      boolean playerCausedDeath, boolean directPlayerKill) {
        if (baseExperience <= 0 || !playerCausedDeath) return baseExperience;

        int effectiveGreed = directPlayerKill ? clampLevel(greedLevel) : 0;
        int effectiveMisfortune = clampLevel(misfortuneLevel);
        double multiplier = effectiveGreed == 0
                ? 1.0
                : Math.min(MAX_GREED_MULTIPLIER, 1.0 + GREED_BONUS_PER_LEVEL * effectiveGreed);
        double penalty = Math.min(MAX_MISFORTUNE_PENALTY,
                MISFORTUNE_PENALTY_PER_LEVEL * effectiveMisfortune);
        double adjusted = baseExperience * multiplier * (1.0 - penalty);
        if (!Double.isFinite(adjusted) || adjusted <= 0.0) return 0;
        return (int) Math.min(Integer.MAX_VALUE, Math.round(adjusted));
    }

    static int greedLevel(int level, boolean directPlayerKill) {
        return directPlayerKill ? clampLevel(level) : 0;
    }

    static double vulnerabilityMultiplier(int level) {
        return 1.0 + 0.25 * clampLevel(level);
    }

    static long vulnerabilityDurationTicks(int level) {
        return 120L * clampLevel(level);
    }

    static int clampLevel(int level) {
        return Math.max(0, Math.min(MAX_LEVEL, level));
    }
}
