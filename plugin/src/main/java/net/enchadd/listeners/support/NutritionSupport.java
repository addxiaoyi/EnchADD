package net.enchadd.listeners.support;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public final class NutritionSupport {
    private NutritionSupport() {
    }

    static int absorptionSeconds(int level, int maxLevel, int secondsPerLevel) {
        if (level <= 0 || maxLevel <= 0 || secondsPerLevel <= 0) return 0;
        return (int) Math.min(120L, (long) Math.min(level, maxLevel) * secondsPerLevel);
    }

    public static boolean applyAbsorption(Player player, int level, int maxLevel, int secondsPerLevel, int amplifier) {
        int seconds = absorptionSeconds(level, maxLevel, secondsPerLevel);
        return seconds > 0 && ActiveBuffSupport.apply(player, PotionEffectType.ABSORPTION,
                seconds, Math.min(1, Math.max(0, amplifier)));
    }

    static int food(int current, int level, int maxLevel, int perLevel) {
        if (level <= 0 || maxLevel <= 0 || perLevel <= 0) return current;
        return (int) Math.min(20L, (long) current + (long) Math.min(level, maxLevel) * perLevel);
    }

    static float saturation(float current, int food, int level, int maxLevel, double perLevel) {
        if (level <= 0 || maxLevel <= 0 || !Double.isFinite(perLevel) || perLevel <= 0
                || !Float.isFinite(current)) return current;
        double restored = current + perLevel * Math.min(level, maxLevel);
        return (float) Math.max(current, Math.min(food, restored));
    }
}
