package net.enchadd.listeners.support;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

final class ActiveBuffSupport {
    private static final int MAX_DURATION_TICKS = 2400;

    private ActiveBuffSupport() {
    }

    static int durationTicks(int seconds) {
        return seconds <= 0 ? 0 : (int) Math.min(MAX_DURATION_TICKS, (long) seconds * 20);
    }

    static boolean canUpgrade(int currentAmplifier, int remainingTicks, int amplifier, int ticks) {
        return ticks > 0 && remainingTicks >= 0 && currentAmplifier <= amplifier
                && (currentAmplifier < amplifier || remainingTicks < ticks);
    }

    static double radius(double configured, double maximum) {
        return Double.isFinite(configured) ? Math.max(0, Math.min(maximum, configured)) : 0;
    }

    static boolean apply(LivingEntity target, PotionEffectType type, int seconds, int amplifier) {
        int ticks = durationTicks(seconds);
        if (ticks <= 0) return false;
        PotionEffect current = target.getPotionEffect(type);
        if (current != null && !canUpgrade(current.getAmplifier(), current.getDuration(), amplifier, ticks)) {
            return false;
        }
        return target.addPotionEffect(new PotionEffect(type, ticks, amplifier, false, false, true));
    }
}
