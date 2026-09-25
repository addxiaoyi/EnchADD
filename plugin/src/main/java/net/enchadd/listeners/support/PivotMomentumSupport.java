package net.enchadd.listeners.support;

import net.enchadd.enchants.PivotEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PivotMomentumSupport {

    public @Nullable PivotContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        int level = PerformanceUtils.getActiveOffhandShieldLevel(player, enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new PivotContext(level, pdc);
    }

    public boolean isOnCooldown(@NotNull PivotContext context,
                                @NotNull NamespacedKey key,
                                int cooldownTicks) {
        return PerformanceUtils.isOnCooldown(context.pdc(), key, cooldownTicks);
    }

    public @Nullable PotionEffect createSpeedEffect(@NotNull PivotEnchant config, int level) {
        int durationTicks = DefenseEffectRules.seconds(level, config.getMaxLevel(), config.getSpeedSecondsPerLevel()) * 20;
        if (durationTicks <= 0) {
            return null;
        }

        int amplifier = Math.min(2, Math.max(0, config.getSpeedAmplifier()));
        return new PotionEffect(PotionEffectType.SPEED, durationTicks, amplifier, false, false, true);
    }

    public void apply(@NotNull Player player,
                      @NotNull PotionEffect effect,
                      @NotNull PivotContext context,
                      @NotNull NamespacedKey key) {
        if (effect.getDuration() <= 0) return;
        PotionEffect current = player.getPotionEffect(effect.getType());
        if (current != null && !ActiveBuffSupport.canUpgrade(current.getAmplifier(), current.getDuration(),
                effect.getAmplifier(), effect.getDuration())) return;
        if (player.addPotionEffect(effect)) PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record PivotContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
