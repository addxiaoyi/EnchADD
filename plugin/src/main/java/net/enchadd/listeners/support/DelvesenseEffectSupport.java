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
import org.bukkit.potion.PotionEffect;
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

    public boolean startCooldown(@NotNull Player player) {
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null || PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return false;
        }
        PerformanceUtils.setCooldown(pdc, cooldownKey);
        return true;
    }

    public void applyNightVisionIfUpgrade(@NotNull Player player) {
        int durationTicks = Math.max(20, config.getNightVisionSeconds() * 20);
        PotionEffect current = player.getPotionEffect(PotionEffectType.NIGHT_VISION);
        if (current != null && current.getDuration() >= durationTicks) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, durationTicks, 0, false, false, true), true);
    }

    public void highlightNearbyMonsters(@NotNull Player player) {
        double radius = config.getRadius();
        int glowSeconds = config.getGlowSeconds();
        if (radius <= 0.0d || glowSeconds <= 0) {
            return;
        }

        int durationTicks = Math.max(20, glowSeconds * 20);
        Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Monster monster) || !PerformanceUtils.isEntityValid(monster)) {
                continue;
            }
            applyGlowIfUpgrade(monster, durationTicks);
        }
    }

    public void playCosmeticFeedback(@NotNull Player player) {
        Location origin = player.getEyeLocation().clone().add(0.0, -0.2, 0.0);
        if (origin.getWorld() != null) {
            ParticleQueue.submit(origin.getWorld(), origin, Particle.GLOW, 10, 0.35, 0.25, 0.35, 0.01);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_HIT, 0.8f, 0.9f);
    }

    private static void applyGlowIfUpgrade(@NotNull Monster monster, int durationTicks) {
        PotionEffect current = monster.getPotionEffect(PotionEffectType.GLOWING);
        if (current != null && current.getDuration() >= durationTicks) {
            return;
        }
        monster.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, durationTicks, 0, false, false, true), true);
    }
}
