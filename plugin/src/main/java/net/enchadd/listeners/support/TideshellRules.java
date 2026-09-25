package net.enchadd.listeners.support;

import org.bukkit.Material;

final class TideshellRules {
    private static final int MAX_DURATION_TICKS = 2400;

    private TideshellRules() {
    }

    static boolean containsWater(Material material, boolean waterlogged) {
        return waterlogged || material == Material.WATER || material == Material.BUBBLE_COLUMN
                || material == Material.KELP || material == Material.KELP_PLANT
                || material == Material.SEAGRASS || material == Material.TALL_SEAGRASS;
    }

    static int durationTicks(int seconds) {
        return seconds <= 0 ? 0 : (int) Math.min(MAX_DURATION_TICKS, (long) seconds * 20);
    }

    static boolean canRefresh(int amplifier, int remainingTicks, int proposedTicks) {
        // Never replace a stronger or infinite effect with the level-I tide buff.
        return proposedTicks > 0 && amplifier <= 0 && remainingTicks >= 0 && remainingTicks < proposedTicks;
    }
}
