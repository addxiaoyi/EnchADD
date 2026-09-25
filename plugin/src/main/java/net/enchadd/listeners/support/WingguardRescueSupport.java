package net.enchadd.listeners.support;

import net.enchadd.enchants.WingguardEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WingguardRescueSupport {

    public @Nullable WingguardContext resolveContext(@NotNull Player player, @NotNull Enchantment enchant) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return null;
        }

        ItemStack chest = equipment.getChestplate();
        int level = chest == null ? 0 : PerformanceUtils.getEnchantLevel(chest, enchant);
        if (level <= 0) {
            return null;
        }

        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) {
            return null;
        }
        return new WingguardContext(level, pdc);
    }

    public boolean isLethal(@NotNull Player player, double finalDamage) {
        return PerformanceUtils.isPlayerValid(player)
                && SurvivalHealthSupport.isLethal(player.getHealth(), finalDamage);
    }

    public boolean shouldTrigger(@NotNull WingguardContext context,
                                 @NotNull NamespacedKey key,
                                 @NotNull WingguardEnchant config) {
        if (PerformanceUtils.isOnCooldown(context.pdc(), key, config.getCooldownTicks())) {
            return false;
        }
        double chance = triggerChance(context.level(), config.getMaxLevel(),
                config.getTriggerChance(), config.getMaxTriggerChance());
        return PerformanceUtils.rollChance(chance);
    }

    static double triggerChance(int level, int maxLevel, double perLevel, double maximum) {
        if (level <= 0 || maxLevel <= 0 || !Double.isFinite(perLevel) || perLevel <= 0
                || !Double.isFinite(maximum) || maximum <= 0) return 0;
        return Math.min(Math.min(1.0, maximum), Math.min(1.0, perLevel) * Math.min(level, maxLevel));
    }

    public void applyRescue(@NotNull Player player,
                            @NotNull WingguardContext context,
                            @NotNull NamespacedKey key,
                            @NotNull WingguardEnchant config) {
        // Cancelling the lethal hit already grants a rescue, even if follow-up buffs are blocked.
        PerformanceUtils.setCooldown(context.pdc(), key);
        int seconds = SurvivalBuffSupport.durationSeconds(context.level(), config.getMaxLevel(),
                config.getSafeSecondsPerLevel());
        ActiveBuffSupport.apply(player, PotionEffectType.SLOW_FALLING, seconds, 0);
        ActiveBuffSupport.apply(player, PotionEffectType.RESISTANCE, seconds, 0);
    }

    public record WingguardContext(int level, @NotNull PersistentDataContainer pdc) {
    }
}
