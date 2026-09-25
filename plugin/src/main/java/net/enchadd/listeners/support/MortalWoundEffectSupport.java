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
    private final MortalWoundWindowSupport window;

    public MortalWoundEffectSupport(@NotNull MortalWoundEnchant config, @NotNull NamespacedKey key) {
        this.config = config;
        this.key = key;
        this.window = new MortalWoundWindowSupport(key);
    }

    public void tagLaunchLevel(@NotNull AbstractArrow arrow, int level) {
        arrow.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);
    }

    public @Nullable Integer readLaunchLevel(@NotNull AbstractArrow arrow) {
        return arrow.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
    }

    public boolean shouldTrigger(int level) {
        level = Math.min(level, config.getMaxLevel());
        if (level <= 0 || antiHealDurationTicks(level) <= 0) return false;
        double chance = config.getTriggerChance() * level;
        if (!Double.isFinite(chance)) {
            return false;
        }
        chance = Math.min(0.6d, Math.max(0.0d, chance));
        return PerformanceUtils.rollChance(chance);
    }

    public int antiHealDurationTicks(int level) {
        return MortalWoundWindowSupport.durationTicks(level, config.getMaxLevel(),
                config.getAntiHealSecondsPerLevel(), config.getAntiHealScale());
    }

    public boolean applyAntiHealWindow(@NotNull LivingEntity victim, int durationTicks) {
        PersistentDataContainer victimPdc = PerformanceUtils.getPDCSafe(victim);
        return victimPdc != null && window.apply(victimPdc, durationTicks);
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
        if (!window.isActive(pdc)) {
            return false;
        }
        if (!Double.isFinite(originalAmount) || originalAmount <= 0.0d) {
            return false;
        }
        double adjusted = SurvivalHealthSupport.reducedHealing(originalAmount, config.getAntiHealScale());
        if (adjusted >= originalAmount) return false;
        scaledAmountConsumer.accept(adjusted);
        return true;
    }
}
