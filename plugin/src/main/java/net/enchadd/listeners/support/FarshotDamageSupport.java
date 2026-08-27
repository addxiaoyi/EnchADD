package net.enchadd.listeners.support;

import net.enchadd.enchants.FarshotEnchant;
import org.jetbrains.annotations.NotNull;

public final class FarshotDamageSupport {

    private final FarshotEnchant config;

    public FarshotDamageSupport(@NotNull FarshotEnchant config) {
        this.config = config;
    }

    public double triggerChance() {
        return Math.min(config.getMaxTriggerChance(), config.getTriggerChance());
    }

    public double scaledDistance(double distance) {
        double minDistance = config.getMinDistance();
        double maxDistance = config.getMaxDistance();
        if (distance <= minDistance) {
            return minDistance;
        }
        return Math.max(minDistance, Math.min(maxDistance, distance));
    }

    public double bonusFactor(int level, double distance) {
        double minDistance = config.getMinDistance();
        double maxDistance = config.getMaxDistance();
        if (distance <= minDistance) {
            return 0.0d;
        }
        double rangeSpan = Math.max(0.0d, maxDistance - minDistance);
        double factor = rangeSpan > 0.0d ? (scaledDistance(distance) - minDistance) / rangeSpan : 1.0d;
        if (factor <= 0.0d) {
            return 0.0d;
        }
        return Math.max(0.0d, level * config.getBonusDamagePerLevel() * factor);
    }

    public double scaledDamage(double baseDamage, int level, double distance) {
        double bonus = bonusFactor(level, distance);
        return bonus <= 0.0d ? baseDamage : baseDamage * (1.0d + bonus);
    }
}
