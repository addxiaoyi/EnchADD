package net.enchadd.listeners.support;

import net.enchadd.enchants.WaysongEnchant;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class WaysongEffectSupport {

    private final WaysongEnchant config;
    private final NamespacedKey cooldownKey;

    public WaysongEffectSupport(@NotNull WaysongEnchant config, @NotNull NamespacedKey cooldownKey) {
        this.config = config;
        this.cooldownKey = cooldownKey;
    }

    public boolean startCooldown(@NotNull Player player) {
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null || PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return false;
        }
        PerformanceUtils.setCooldown(pdc, cooldownKey);
        return true;
    }

    public void applyMarchBuffs(@NotNull Player caller) {
        applySpeedIfUpgrade(caller);

        double radius = config.getRadius();
        if (radius <= 0.0d) {
            return;
        }
        Collection<Entity> nearbyEntities = caller.getWorld().getNearbyEntities(caller.getLocation(), radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player target)) {
                continue;
            }
            if (target.equals(caller) || !PerformanceUtils.isPlayerValid(target)) {
                continue;
            }
            applySpeedIfUpgrade(target);
        }
    }

    public void playCosmeticFeedback(@NotNull Player player) {
        Location origin = player.getLocation().clone().add(0.0, 1.2, 0.0);
        if (origin.getWorld() != null) {
            ParticleQueue.submit(origin.getWorld(), origin, Particle.NOTE, 6, 0.4, 0.4, 0.4, 1.0);
        }
    }

    private void applySpeedIfUpgrade(@NotNull Player player) {
        int durationTicks = Math.max(20, config.getSpeedSeconds() * 20);
        int amplifier = config.getSpeedAmplifier();
        PotionEffect current = player.getPotionEffect(PotionEffectType.SPEED);
        if (current != null) {
            if (current.getAmplifier() > amplifier) {
                return;
            }
            if (current.getAmplifier() == amplifier && current.getDuration() >= durationTicks) {
                return;
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationTicks, amplifier, false, false, true), true);
    }
}
