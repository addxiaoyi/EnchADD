package net.enchadd.listeners.support;

import net.enchadd.enchants.MortalWoundEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MortalWoundEffectSupport {

    private final MortalWoundEnchant config;
    private final NamespacedKey key;

    public MortalWoundEffectSupport(@NotNull MortalWoundEnchant config, @NotNull NamespacedKey key) {
        this.config = config;
        this.key = key;
    }

    public void tagLaunchLevel(@NotNull AbstractArrow arrow, int level) {
        arrow.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);
    }

    public @Nullable Integer readLaunchLevel(@NotNull AbstractArrow arrow) {
        return arrow.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }

    public boolean shouldTrigger(int level) {
        double chance = Math.min(0.6d, config.getTriggerChance() * level);
        return PerformanceUtils.rollChance(chance);
    }

    public int antiHealDurationTicks(int level) {
        return PerformanceUtils.calculateDurationTicksPerLevel(config.getAntiHealSecondsPerLevel(), level);
    }

    public void applyAntiHealWindow(@NotNull LivingEntity victim, int durationTicks) {
        PersistentDataContainer victimPdc = PerformanceUtils.getPDCSafe(victim);
        if (victimPdc != null) {
            PerformanceUtils.setWindowUntilTicks(victimPdc, key, durationTicks);
        }
    }

    public void writeShooterCooldown(@NotNull Player shooter) {
        PersistentDataContainer shooterPdc = PerformanceUtils.getPDCSafe(shooter);
        if (shooterPdc != null) {
            PerformanceUtils.setCooldown(shooterPdc, key);
        }
    }

    public boolean isShooterOnCooldown(@NotNull Player shooter) {
        PersistentDataContainer shooterPdc = PerformanceUtils.getPDCSafe(shooter);
        return shooterPdc != null && PerformanceUtils.isOnCooldown(shooterPdc, key, config.getCooldownTicks());
    }

    public boolean applyHealingScale(@NotNull LivingEntity entity,
                              double originalAmount,
                              @NotNull java.util.function.DoubleConsumer scaledAmountConsumer) {
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(entity);
        if (pdc == null) {
            return false;
        }
        if (!PerformanceUtils.isWindowActive(pdc, key)) {
            pdc.remove(key);
            return false;
        }
        double scale = PerformanceUtils.clamp(config.getAntiHealScale(), 0.0d, 1.0d);
        scaledAmountConsumer.accept(originalAmount * scale);
        return true;
    }
}
