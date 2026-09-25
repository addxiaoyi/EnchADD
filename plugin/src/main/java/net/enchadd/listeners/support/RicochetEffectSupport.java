package net.enchadd.listeners.support;

import net.enchadd.enchants.RicochetEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class RicochetEffectSupport {

    private final RicochetEnchant config;
    private final NamespacedKey levelKey;
    private final NamespacedKey bouncedKey;

    public RicochetEffectSupport(@NotNull RicochetEnchant config,
                          @NotNull NamespacedKey levelKey,
                          @NotNull NamespacedKey bouncedKey) {
        this.config = config;
        this.levelKey = levelKey;
        this.bouncedKey = bouncedKey;
    }

    public void tagLaunchLevel(@NotNull AbstractArrow arrow, int level) {
        arrow.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
    }

    public @Nullable Integer readLaunchLevel(@NotNull AbstractArrow arrow) {
        return arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
    }

    public boolean hasBounced(@NotNull AbstractArrow arrow) {
        return arrow.getPersistentDataContainer().getOrDefault(bouncedKey, PersistentDataType.BOOLEAN, false);
    }

    public boolean shouldTrigger(int level) {
        if (level <= 0) return false;
        level = Math.min(level, config.getMaxLevel());
        double chance = config.getTriggerChance() * level;
        if (!Double.isFinite(chance)) {
            return false;
        }
        chance = Math.min(0.5d, Math.max(0.0d, chance));
        return net.enchadd.utils.PerformanceUtils.rollChance(chance);
    }

    public @Nullable LivingEntity findRicochetTarget(@NotNull AbstractArrow arrow,
                                              @NotNull LivingEntity shooter,
                                              @NotNull Entity originalVictim) {
        World world = arrow.getWorld();
        Location origin = arrow.getLocation();
        double radius = config.getRadius();
        if (!Double.isFinite(radius)) {
            return null;
        }
        radius = Math.min(8.0d, Math.max(1.0d, radius));
        Collection<Entity> nearbyEntities = world.getNearbyEntities(origin, radius, radius, radius);

        LivingEntity target = null;
        double minDistanceSquared = Double.MAX_VALUE;
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            if (livingEntity.equals(shooter) || livingEntity.equals(originalVictim)
                    || !livingEntity.isValid() || livingEntity.isDead()) {
                continue;
            }
            if (!EffectMotionSupport.withinRadius(livingEntity.getLocation().toVector().subtract(origin.toVector()), radius)) {
                continue;
            }
            double distanceSquared = livingEntity.getLocation().distanceSquared(origin);
            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                target = livingEntity;
            }
        }
        return target;
    }

    public boolean spawnRicochet(@NotNull AbstractArrow arrow,
                       @NotNull PlayerLike shooter,
                       @NotNull LivingEntity target,
                       int level) {
        if (hasBounced(arrow) || level <= 0 || !target.isValid() || target.isDead()
                || !arrow.getWorld().equals(target.getWorld())) return false;
        double scale = config.getSpeedScale();
        double damage = arrow.getDamage();
        if (!Double.isFinite(scale) || scale <= 0.0 || !Double.isFinite(damage) || damage <= 0.0) return false;
        Location origin = arrow.getLocation();
        double baseSpeed = arrow.getVelocity().length() * Math.min(1.25d, Math.max(0.25d, scale));
        Vector velocity = EffectMotionSupport.directedVelocity(
                target.getEyeLocation().toVector().subtract(origin.toVector()), baseSpeed);
        if (velocity == null) return false;
        int effectiveLevel = Math.min(level, config.getMaxLevel());
        // Mark the source too: piercing arrows may produce several hit events.
        arrow.getPersistentDataContainer().set(bouncedKey, PersistentDataType.BOOLEAN, true);
        arrow.getWorld().spawn(origin, arrow.getClass(), spawned -> {
            spawned.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, effectiveLevel);
            spawned.getPersistentDataContainer().set(bouncedKey, PersistentDataType.BOOLEAN, true);
            spawned.setVelocity(velocity);
            spawned.setShooter(shooter.asLivingEntity());
            spawned.setDamage(damage);
            spawned.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
        }, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.ENCHANTMENT);
        return true;
    }

    public interface PlayerLike {
        @NotNull LivingEntity asLivingEntity();
    }
}
