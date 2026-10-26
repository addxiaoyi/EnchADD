package net.enchadd.mod.common;

import net.minecraft.util.RandomSource;

public final class TrawlerRules {

    private static final int MAX_LEVEL = 3;
    private static final int EXTRA_ITEMS_PER_LEVEL = 1;
    private static final double TRIGGER_CHANCE_PER_LEVEL = 0.30;
    private static final double MAX_TRIGGER_CHANCE = 0.75;

    private TrawlerRules() {
    }

    public static boolean shouldTrigger(RandomSource random, int level) {
        if (random == null || level <= 0) return false;
        double chance = Math.min(MAX_TRIGGER_CHANCE, TRIGGER_CHANCE_PER_LEVEL * Math.min(level, MAX_LEVEL));
        return random.nextDouble() < chance;
    }

    public static int extraItems(int level) {
        if (level <= 0) return 0;
        return Math.min(level, MAX_LEVEL) * EXTRA_ITEMS_PER_LEVEL;
    }
}
