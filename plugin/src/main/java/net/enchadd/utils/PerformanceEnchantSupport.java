package net.enchadd.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for enchantment-related performance optimizations.
 */
final class PerformanceEnchantSupport {

    private static final int MAX_EFFECTIVE_STACKED_LEVEL = 4;

    private PerformanceEnchantSupport() {
    }

    static int getEnchantLevel(@Nullable ItemStack item, @NotNull Enchantment enchantment) {
        return EnchantCache.getLevel(item, enchantment);
    }

    static int getHighestEnchantLevel(@Nullable EntityEquipment equipment, @NotNull Enchantment enchantment) {
        if (equipment == null) {
            return 0;
        }

        int maxLevel = 0;
        // Optimized: reduce method calls by caching item references
        ItemStack mainHand = equipment.getItemInMainHand();
        ItemStack offHand = equipment.getItemInOffHand();
        ItemStack helmet = equipment.getHelmet();
        ItemStack chestplate = equipment.getChestplate();
        ItemStack leggings = equipment.getLeggings();
        ItemStack boots = equipment.getBoots();

        // Single call per item slot
        int level = getEnchantLevel(mainHand, enchantment);
        if (level > maxLevel) maxLevel = level;

        level = getEnchantLevel(offHand, enchantment);
        if (level > maxLevel) maxLevel = level;

        level = getEnchantLevel(helmet, enchantment);
        if (level > maxLevel) maxLevel = level;

        level = getEnchantLevel(chestplate, enchantment);
        if (level > maxLevel) maxLevel = level;

        level = getEnchantLevel(leggings, enchantment);
        if (level > maxLevel) maxLevel = level;

        level = getEnchantLevel(boots, enchantment);
        if (level > maxLevel) maxLevel = level;

        return maxLevel;
    }

    static int getSumOfEnchantLevels(@Nullable EntityEquipment equipment, @NotNull Enchantment enchantment) {
        if (equipment == null) {
            return 0;
        }

        int sum = 0;
        sum = addStackedLevel(sum, getEnchantLevel(equipment.getItemInMainHand(), enchantment));
        sum = addStackedLevel(sum, getEnchantLevel(equipment.getItemInOffHand(), enchantment));
        sum = addStackedLevel(sum, getEnchantLevel(equipment.getHelmet(), enchantment));
        sum = addStackedLevel(sum, getEnchantLevel(equipment.getChestplate(), enchantment));
        sum = addStackedLevel(sum, getEnchantLevel(equipment.getLeggings(), enchantment));
        return addStackedLevel(sum, getEnchantLevel(equipment.getBoots(), enchantment));
    }

    static int addStackedLevel(int total, int level) {
        long sum = (long) Math.max(0, total) + Math.max(0, level);
        return (int) Math.min(MAX_EFFECTIVE_STACKED_LEVEL, sum);
    }
}
