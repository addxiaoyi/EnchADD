package net.enchadd.mod.common;

import net.minecraft.util.RandomSource;

import java.util.Set;

public final class RefineRules {

    private static final int MAX_LEVEL = 3;
    private static final double CHANCE_PER_LEVEL = 0.15;
    private static final double MAX_CHANCE = 0.50;
    private static final Set<String> ORE_DROPS = Set.of(
            "raw_iron", "raw_copper", "raw_gold", "coal", "redstone",
            "lapis_lazuli", "diamond", "emerald", "netherite_scrap"
    );

    private RefineRules() {
    }

    public static boolean isOreDrop(String path) {
        return ORE_DROPS.contains(path);
    }

    public static boolean shouldTrigger(RandomSource random, int level) {
        if (random == null || level <= 0) return false;
        return random.nextDouble() < Math.min(MAX_CHANCE, CHANCE_PER_LEVEL * Math.min(MAX_LEVEL, level));
    }
}
