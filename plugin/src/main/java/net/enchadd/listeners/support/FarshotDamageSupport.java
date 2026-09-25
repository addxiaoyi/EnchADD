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
        double factor = RangedDamageRules.distanceFactor(distance, config.getMinDistance(), config.getMaxDistance());
        return RangedDamageRules.bonus(level, config.getMaxLevel(), config.getBonusDamagePerLevel(), factor);
    }

    public double scaledDamage(double baseDamage, int level, double distance) {
        double bonus = bonusFactor(level, distance);
        return RangedDamageRules.damage(baseDamage, bonus);
    }
}
