package net.enchadd.listeners.support;

import net.enchadd.enchants.AfterglideEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AfterglideGlideSupport {

    public @Nullable AfterglideContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        int level = PerformanceUtils.getEnchantLevel(equipment.getChestplate(), enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new AfterglideContext(level, pdc);
    }

    public boolean isOnCooldown(@NotNull AfterglideContext context,
                                @NotNull NamespacedKey key,
                                int cooldownTicks) {
        return PerformanceUtils.isOnCooldown(context.pdc(), key, cooldownTicks);
    }

    public @Nullable PotionEffect createSlowFallingEffect(@NotNull AfterglideEnchant config, int level) {
        int durationTicks = durationTicks(level, config.getMaxLevel(), config.getSlowFallingSecondsPerLevel());
        if (durationTicks <= 0) {
            return null;
        }
        int amplifier = Math.min(1, Math.max(0, config.getSlowFallingAmplifier()));
        return new PotionEffect(PotionEffectType.SLOW_FALLING, durationTicks, amplifier, false, false, true);
    }

    public void apply(@NotNull Player player,
                      @NotNull PotionEffect effect,
                      @NotNull AfterglideContext context,
                      @NotNull NamespacedKey key) {
        if (effect.getDuration() <= 0) return;
        PotionEffect current = player.getPotionEffect(effect.getType());
        if (current != null && !ActiveBuffSupport.canUpgrade(current.getAmplifier(), current.getDuration(),
                effect.getAmplifier(), effect.getDuration())) return;
        if (player.addPotionEffect(effect)) PerformanceUtils.setCooldown(context.pdc(), key);
    }

    static int durationTicks(int level, int maxLevel, int secondsPerLevel) {
        if (level <= 0 || maxLevel <= 0 || secondsPerLevel <= 0) return 0;
        long seconds = (long) Math.min(level, maxLevel) * secondsPerLevel;
        return (int) Math.min(120L, seconds) * 20;
    }

    public record AfterglideContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
