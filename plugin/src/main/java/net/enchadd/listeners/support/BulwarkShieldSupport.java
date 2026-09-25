package net.enchadd.listeners.support;

import net.enchadd.enchants.BulwarkEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BulwarkShieldSupport {

    public @Nullable BulwarkContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        int level = PerformanceUtils.getActiveOffhandShieldLevel(player, enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new BulwarkContext(level, pdc);
    }

    public boolean isOnCooldown(@NotNull BulwarkContext context,
                                @NotNull NamespacedKey key,
                                int cooldownTicks) {
        return PerformanceUtils.isOnCooldown(context.pdc(), key, cooldownTicks);
    }

    public @NotNull PotionEffect createResistanceEffect(@NotNull BulwarkEnchant config, int level) {
        int durationTicks = DefenseEffectRules.seconds(level, config.getMaxLevel(), config.getResistanceSecondsPerLevel()) * 20;
        int amplifier = Math.min(1, Math.max(0, config.getResistanceAmplifier()));
        return new PotionEffect(PotionEffectType.RESISTANCE, durationTicks, amplifier, false, false, true);
    }

    public void apply(@NotNull Player player,
                      @NotNull PotionEffect effect,
                      @NotNull BulwarkContext context,
                      @NotNull NamespacedKey key) {
        if (effect.getDuration() <= 0) return;
        PotionEffect current = player.getPotionEffect(effect.getType());
        if (current != null && !ActiveBuffSupport.canUpgrade(current.getAmplifier(), current.getDuration(),
                effect.getAmplifier(), effect.getDuration())) return;
        if (player.addPotionEffect(effect)) PerformanceUtils.setCooldown(context.pdc(), key);
    }

    public record BulwarkContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
