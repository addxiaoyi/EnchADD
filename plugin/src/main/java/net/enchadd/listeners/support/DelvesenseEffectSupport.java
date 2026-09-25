package net.enchadd.listeners.support;

import net.enchadd.enchants.DelvesenseEnchant;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class DelvesenseEffectSupport {

    private final DelvesenseEnchant config;
    private final NamespacedKey cooldownKey;

    public DelvesenseEffectSupport(@NotNull DelvesenseEnchant config, @NotNull NamespacedKey cooldownKey) {
        this.config = config;
        this.cooldownKey = cooldownKey;
    }

    public boolean canTrigger(@NotNull Player player) {
        return player.getLocation().getY() <= config.getMaxActivationY();
    }

    public boolean activate(@NotNull Player player) {
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null || PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return false;
        }
        boolean vision = ActiveBuffSupport.apply(player, PotionEffectType.NIGHT_VISION, config.getNightVisionSeconds(), 0);
        boolean highlighted = highlightNearbyMonsters(player);
        if (!vision && !highlighted) return false;
        PerformanceUtils.setCooldown(pdc, cooldownKey);
        return true;
    }

    private boolean highlightNearbyMonsters(@NotNull Player player) {
        double radius = ActiveBuffSupport.radius(config.getRadius(), 16.0);
        int glowSeconds = config.getGlowSeconds();
        if (radius <= 0.0d || glowSeconds <= 0) {
            return false;
        }

        Location origin = player.getLocation();
        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(origin, radius, radius, radius);
        int affected = 0;
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Monster monster) || !PerformanceUtils.isEntityValid(monster)) {
                continue;
            }
            if (!EffectMotionSupport.withinRadius(monster.getLocation().toVector().subtract(origin.toVector()), radius)) continue;
            if (!ActiveBuffSupport.apply(monster, PotionEffectType.GLOWING, glowSeconds, 0)) continue;
            if (++affected >= 32) break;
        }
        return affected > 0;
    }

    public void playCosmeticFeedback(@NotNull Player player) {
        Location origin = player.getEyeLocation().clone().add(0.0, -0.2, 0.0);
        if (origin.getWorld() != null) {
            ParticleQueue.submit(origin.getWorld(), origin, Particle.GLOW, 10, 0.35, 0.25, 0.35, 0.01);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.8f, 0.9f);
    }

}
