package net.enchadd.listeners.support;

import net.enchadd.enchants.BarrierEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class BarrierShieldSupport {
    private static final double MAX_TRIGGER_CHANCE = 0.75;
    private static final double MAX_KNOCKBACK_RADIUS = 6.0;
    private static final int MAX_TARGETS = 24;

    private final BarrierEnchant config;

    public BarrierShieldSupport(@NotNull BarrierEnchant config) {
        this.config = config;
    }

    public boolean shouldTrigger(int level) {
        return PerformanceUtils.rollChance(triggerChance(level, config.getMaxLevel(),
                config.getTriggerChance(), config.getMaxTriggerChance()));
    }

    public boolean knockbackNearby(@NotNull Player player, int level) {
        double radius = knockbackRadius(level, config.getMaxLevel(), config.getKnockbackRadiusPerLevel());
        if (radius <= 0.0) return false;
        level = Math.min(level, config.getMaxLevel());
        Location loc = player.getLocation();
        Vector origin = loc.toVector();

        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(loc, radius, radius, radius);
        int affected = 0;
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity target)) {
                continue;
            }
            if (target.equals(player) || !PerformanceUtils.isEntityValid(target)) {
                continue;
            }

            Vector direction = target.getLocation().toVector().subtract(origin);
            if (!EffectMotionSupport.withinRadius(direction, radius)) {
                continue;
            }
            direction = knockbackVelocity(direction, level);
            if (direction == null) continue;
            target.setVelocity(direction);
            if (++affected >= MAX_TARGETS) break;
        }
        return affected > 0;
    }

    static double triggerChance(int level, int maximum, double perLevel, double cap) {
        if (level <= 0 || maximum <= 0 || !Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(cap) || cap <= 0) return 0;
        return Math.min(MAX_TRIGGER_CHANCE, Math.min(cap, perLevel * Math.min(level, maximum)));
    }

    static double knockbackRadius(int level, int maximum, double perLevel) {
        if (level <= 0 || maximum <= 0 || !Double.isFinite(perLevel) || perLevel <= 0) return 0;
        return Math.min(MAX_KNOCKBACK_RADIUS, perLevel * Math.min(level, maximum));
    }

    static @Nullable Vector knockbackVelocity(Vector offset, int level) {
        if (level <= 0 || !EffectMotionSupport.finite(offset)) return null;
        double horizontalStrength = Math.min(1.4, 0.6 + 0.2 * level);
        double verticalStrength = Math.min(0.8, 0.4 + 0.1 * level);
        Vector velocity = EffectMotionSupport.directedVelocity(offset.clone().setY(0), horizontalStrength);
        return velocity == null ? null : velocity.setY(verticalStrength);
    }
}
