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
        double chance = Math.min(0.5d, config.getTriggerChance() * level);
        return net.enchadd.utils.PerformanceUtils.rollChance(chance);
    }

    public @Nullable LivingEntity findRicochetTarget(@NotNull AbstractArrow arrow,
                                              @NotNull LivingEntity shooter,
                                              @NotNull Entity originalVictim) {
        World world = arrow.getWorld();
        Location origin = arrow.getLocation();
        double radius = Math.max(1.0d, config.getRadius());
        Collection<Entity> nearbyEntities = world.getNearbyEntities(origin, radius, radius, radius);

        LivingEntity target = null;
        double minDistanceSquared = Double.MAX_VALUE;
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            if (livingEntity.equals(shooter) || livingEntity.equals(originalVictim)) {
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

    public void spawnRicochet(@NotNull AbstractArrow arrow,
                       @NotNull PlayerLike shooter,
                       @NotNull LivingEntity target,
                       int level) {
        Location origin = arrow.getLocation();
        Vector originVector = origin.toVector();
        Vector dir = target.getEyeLocation().toVector().subtract(originVector).normalize();
        double baseSpeed = arrow.getVelocity().length() * config.getSpeedScale();
        Vector velocity = dir.multiply(baseSpeed);
        arrow.getWorld().spawn(origin, arrow.getClass(), spawned -> {
            spawned.setVelocity(velocity);
            spawned.setShooter(shooter.asLivingEntity());
            spawned.getPersistentDataContainer().set(levelKey, PersistentDataType.INTEGER, level);
            spawned.getPersistentDataContainer().set(bouncedKey, PersistentDataType.BOOLEAN, true);
        });
    }

    public interface PlayerLike {
        @NotNull LivingEntity asLivingEntity();
    }
}
