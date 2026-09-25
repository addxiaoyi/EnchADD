package net.enchadd.listeners.support;

import net.enchadd.enchants.FortitudeEnchant;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

public final class SurvivalBuffSupport {
    private static final int MAX_DURATION_SECONDS = 120;

    private SurvivalBuffSupport() {
    }

    static int durationSeconds(int level, int maxLevel, int secondsPerLevel) {
        if (level <= 0 || maxLevel <= 0 || secondsPerLevel <= 0) return 0;
        long seconds = (long) Math.min(level, maxLevel) * secondsPerLevel;
        return (int) Math.min(MAX_DURATION_SECONDS, seconds);
    }

    public static boolean applyFortitude(LivingEntity entity, int level, FortitudeEnchant config) {
        int regenSeconds = durationSeconds(level, config.getMaxLevel(), config.getRegenSecondsPerLevel());
        int resistanceSeconds = durationSeconds(level, config.getMaxLevel(), config.getResistanceSecondsPerLevel());
        boolean regenApplied = ActiveBuffSupport.apply(entity, PotionEffectType.REGENERATION, regenSeconds, 0);
        boolean resistanceApplied = ActiveBuffSupport.apply(entity, PotionEffectType.RESISTANCE, resistanceSeconds, 0);
        return regenApplied || resistanceApplied;
    }
}
