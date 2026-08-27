package net.enchadd.listeners.support;

import net.enchadd.enchants.StarwishEnchant;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class StarwishEffectSupport {

    private final StarwishEnchant config;
    private final NamespacedKey cooldownKey;

    public StarwishEffectSupport(@NotNull StarwishEnchant config, @NotNull NamespacedKey cooldownKey) {
        this.config = config;
        this.cooldownKey = cooldownKey;
    }

    public boolean canTrigger(@NotNull Player player) {
        if (!isEligibleWorld(player.getWorld())) {
            return false;
        }
        if (!isNight(player.getWorld().getTime())) {
            return false;
        }
        return player.getLocation().getPitch() <= config.getLookUpPitchThreshold();
    }

    public boolean startCooldown(@NotNull Player player) {
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null || PerformanceUtils.isOnCooldown(pdc, cooldownKey, config.getCooldownTicks())) {
            return false;
        }
        PerformanceUtils.setCooldown(pdc, cooldownKey);
        return true;
    }

    public void applyEffects(@NotNull Player player) {
        applyEffectIfUpgrade(player, PotionEffectType.NIGHT_VISION, config.getNightVisionSeconds());
        applyEffectIfUpgrade(player, PotionEffectType.LUCK, config.getLuckSeconds());
    }

    public void playCosmeticFeedback(@NotNull Player player) {
        Location burst = player.getEyeLocation().clone().add(0.0, 2.2, 0.0);
        if (burst.getWorld() != null) {
            ParticleQueue.submit(burst.getWorld(), burst, Particle.END_ROD, 8, 0.4, 0.8, 0.4, 0.01);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.6f);
    }

    private static boolean isEligibleWorld(@NotNull World world) {
        return world.getEnvironment() == World.Environment.NORMAL;
    }

    private static boolean isNight(long worldTime) {
        return worldTime >= 13000L && worldTime <= 23000L;
    }

    private static void applyEffectIfUpgrade(@NotNull Player player,
                                             @NotNull PotionEffectType type,
                                             int seconds) {
        int durationTicks = Math.max(20, seconds * 20);
        PotionEffect current = player.getPotionEffect(type);
        if (current != null) {
            if (current.getAmplifier() > 0) {
                return;
            }
            if (current.getAmplifier() == 0 && current.getDuration() >= durationTicks) {
                return;
            }
        }

        player.addPotionEffect(new PotionEffect(type, durationTicks, 0, false, false, true), true);
    }
}
