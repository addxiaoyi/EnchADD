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

    public boolean activate(@NotNull Player player) {
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null || PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return false;
        }
        if (!applyMarchBuffs(player)) return false;
        PerformanceUtils.setCooldown(pdc, cooldownKey);
        return true;
    }

    private boolean applyMarchBuffs(@NotNull Player caller) {
        boolean applied = applySpeedIfUpgrade(caller);

        double radius = ActiveBuffSupport.radius(config.getRadius(), 12.0);
        if (radius <= 0.0d) {
            return applied;
        }
        Location origin = caller.getLocation();
        Collection<Entity> nearbyEntities = caller.getWorld().getNearbyEntities(origin, radius, radius, radius);
        int affected = 0;
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player target)) {
                continue;
            }
            if (target.equals(caller) || !PerformanceUtils.isPlayerValid(target)) {
                continue;
            }
            if (!EffectMotionSupport.withinRadius(target.getLocation().toVector().subtract(origin.toVector()), radius)) continue;
            if (!applySpeedIfUpgrade(target)) continue;
            applied = true;
            if (++affected >= 16) break;
        }
        return applied;
    }

    public void playCosmeticFeedback(@NotNull Player player) {
        Location origin = player.getLocation().clone().add(0.0, 1.2, 0.0);
        if (origin.getWorld() != null) {
            ParticleQueue.submit(origin.getWorld(), origin, Particle.NOTE, 6, 0.4, 0.4, 0.4, 1.0);
        }
    }

    private boolean applySpeedIfUpgrade(@NotNull Player player) {
        int amplifier = Math.min(2, Math.max(0, config.getSpeedAmplifier()));
        return ActiveBuffSupport.apply(player, PotionEffectType.SPEED, config.getSpeedSeconds(), amplifier);
    }
}
