package net.enchadd.listeners.support;

import net.enchadd.enchants.TideshellEnchant;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class TideshellEffectSupport {

    private final TideshellEnchant config;
    private final NamespacedKey cooldownKey;

    public TideshellEffectSupport(@NotNull TideshellEnchant config, @NotNull NamespacedKey cooldownKey) {
        this.config = config;
        this.cooldownKey = cooldownKey;
    }

    public boolean isTouchingWater(@NotNull Player player) {
        Block feet = player.getLocation().getBlock();
        if (feet.isLiquid()) {
            return true;
        }

        Location eye = player.getEyeLocation();
        Block eyeBlock = eye.getBlock();
        if (eyeBlock.isLiquid()) {
            return true;
        }

        return eyeBlock.getRelative(BlockFace.UP).isLiquid();
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
        applyEffectIfUpgrade(player, PotionEffectType.WATER_BREATHING, config.getWaterBreathingSeconds());
        applyEffectIfUpgrade(player, PotionEffectType.DOLPHINS_GRACE, config.getDolphinsGraceSeconds());
    }

    public void playCosmeticFeedback(@NotNull Player player) {
        Location origin = player.getEyeLocation().clone().add(0.0, -0.1, 0.0);
        if (origin.getWorld() != null) {
            ParticleQueue.submit(origin.getWorld(), origin, Particle.BUBBLE, 10, 0.25, 0.25, 0.25, 0.01);
        }
        player.playSound(player.getLocation(), Sound.BLOCK_CONDUIT_ACTIVATE, 0.7f, 1.2f);
    }

    private static void applyEffectIfUpgrade(@NotNull Player player,
                                             @NotNull PotionEffectType type,
                                             int seconds) {
        int durationTicks = Math.max(20, seconds * 20);
        PotionEffect current = player.getPotionEffect(type);
        if (current != null && current.getDuration() >= durationTicks) {
            return;
        }

        player.addPotionEffect(new PotionEffect(type, durationTicks, 0, false, false, true), true);
    }
}
