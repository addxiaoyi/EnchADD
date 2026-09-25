package net.enchadd.listeners.support;

import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

final class KnockbackRules {
    private KnockbackRules() {
    }

    static @Nullable Vector reduce(@Nullable Vector knockback, int level, int maxLevel,
                                   double perLevel, double configuredCap, double safetyCap) {
        if (knockback == null || !EffectMotionSupport.finite(knockback)
                || level <= 0 || maxLevel <= 0 || !Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(configuredCap) || configuredCap <= 0
                || !Double.isFinite(safetyCap) || safetyCap <= 0 || safetyCap >= 1) return null;
        double reduction = Math.min(safetyCap, Math.min(configuredCap, perLevel * Math.min(level, maxLevel)));
        Vector reduced = knockback.clone().multiply(1.0 - reduction);
        // An unchanged impulse must not spend the defensive enchantment's cooldown.
        if (reduced.getX() == knockback.getX() && reduced.getY() == knockback.getY()
                && reduced.getZ() == knockback.getZ()) return null;
        return reduced;
    }
}
