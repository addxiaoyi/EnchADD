package net.enchadd;

import net.enchadd.security.PluginProtection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Main plugin class providing enchantment helper methods.
 * Optimized with cached slot group checks for repeated lookups.
 */
@SuppressWarnings("UnstableApiUsage")
public class EnchADD extends JavaPlugin {

    private EnchADDLifecycleService lifecycleService;

    @Override
    public void onEnable() {
        if (!PluginProtection.verifyPlugin(this)) {
            return;
        }
        lifecycleService = new EnchADDLifecycleService(this);
        if (!lifecycleService.start()) {
            return;
        }
        getLogger().info("EnchADD 已启用。");
    }

    @Override
    public void onDisable() {
        if (lifecycleService != null) {
            lifecycleService.stop();
        }
        getLogger().info("EnchADD 已关闭。");
    }

    public static int getHighestEnchantLevel(
            @NotNull EntityEquipment equipment,
            @NotNull Enchantment enchantment
    ) {
        Set<EquipmentSlotGroup> groups = enchantment.getActiveSlotGroups();
        // Cache slot group checks
        boolean anyGroup = groups.contains(EquipmentSlotGroup.ANY);
        boolean handGroup = anyGroup || groups.contains(EquipmentSlotGroup.HAND);
        boolean armorGroup = anyGroup || groups.contains(EquipmentSlotGroup.ARMOR);

        int highestLevel = 0;
        if (armorGroup || groups.contains(EquipmentSlotGroup.FEET)) {
            highestLevel = Math.max(highestLevel, levelOf(equipment.getBoots(), enchantment, true));
        }
        if (armorGroup || groups.contains(EquipmentSlotGroup.LEGS)) {
            highestLevel = Math.max(highestLevel, levelOf(equipment.getLeggings(), enchantment, true));
        }
        if (armorGroup || groups.contains(EquipmentSlotGroup.CHEST)) {
            highestLevel = Math.max(highestLevel, levelOf(equipment.getChestplate(), enchantment, true));
        }
        if (armorGroup || groups.contains(EquipmentSlotGroup.HEAD)) {
            highestLevel = Math.max(highestLevel, levelOf(equipment.getHelmet(), enchantment, true));
        }
        if (handGroup || groups.contains(EquipmentSlotGroup.MAINHAND)) {
            highestLevel = Math.max(highestLevel, levelOf(equipment.getItemInMainHand(), enchantment, true));
        }
        if (handGroup || groups.contains(EquipmentSlotGroup.OFFHAND)) {
            highestLevel = Math.max(highestLevel, levelOf(equipment.getItemInOffHand(), enchantment, true));
        }
        return highestLevel;
    }

    public static int getSumOfEnchantLevels(
            @NotNull EntityEquipment equipment,
            @NotNull Enchantment enchantment
    ) {
        Set<EquipmentSlotGroup> groups = enchantment.getActiveSlotGroups();
        // Cache slot group checks
        boolean anyGroup = groups.contains(EquipmentSlotGroup.ANY);
        boolean handGroup = anyGroup || groups.contains(EquipmentSlotGroup.HAND);
        boolean armorGroup = anyGroup || groups.contains(EquipmentSlotGroup.ARMOR);

        long level = 0;
        if (armorGroup || groups.contains(EquipmentSlotGroup.FEET)) {
            level += levelOf(equipment.getBoots(), enchantment, true);
        }
        if (armorGroup || groups.contains(EquipmentSlotGroup.LEGS)) {
            level += levelOf(equipment.getLeggings(), enchantment, true);
        }
        if (armorGroup || groups.contains(EquipmentSlotGroup.CHEST)) {
            level += levelOf(equipment.getChestplate(), enchantment, true);
        }
        if (armorGroup || groups.contains(EquipmentSlotGroup.HEAD)) {
            level += levelOf(equipment.getHelmet(), enchantment, true);
        }
        if (handGroup || groups.contains(EquipmentSlotGroup.MAINHAND)) {
            level += levelOf(equipment.getItemInMainHand(), enchantment, true);
        }
        if (handGroup || groups.contains(EquipmentSlotGroup.OFFHAND)) {
            level += levelOf(equipment.getItemInOffHand(), enchantment, true);
        }
        return (int) Math.min(4, level);
    }

    private static int levelOf(ItemStack item, Enchantment ench, boolean enabled) {
        if (!enabled || item == null) return 0;
        return Math.max(0, net.enchadd.utils.EnchantCache.getLevel(item, ench));
    }
}
