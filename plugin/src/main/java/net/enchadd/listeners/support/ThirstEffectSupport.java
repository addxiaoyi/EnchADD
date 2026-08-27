package net.enchadd.listeners.support;

import net.enchadd.enchants.ThirstEnchant;
import net.enchadd.utils.EnchantCache;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ThirstEffectSupport {

    private final org.bukkit.enchantments.Enchantment enchant;
    private final ThirstEnchant config;
    private final NamespacedKey combatKey;

    public ThirstEffectSupport(@Nullable org.bukkit.enchantments.Enchantment enchant,
                        @NotNull ThirstEnchant config,
                        @NotNull NamespacedKey combatKey) {
        this.enchant = enchant;
        this.config = config;
        this.combatKey = combatKey;
    }

    public boolean markCombatIfApplicable(@NotNull Player player) {
        if (enchant == null) {
            return false;
        }
        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return false;
        }
        ItemStack chest = equipment.getChestplate();
        if (chest == null) {
            return false;
        }
        int level = EnchantCache.getLevel(chest, enchant);
        if (level <= 0) {
            return false;
        }
        PerformanceUtils.setWindowUntilTicks(player.getPersistentDataContainer(), combatKey, config.getCombatWindowTicks());
        return true;
    }

    public boolean applyRegenPenalty(@NotNull Player player, double amount, @NotNull java.util.function.DoubleConsumer scaledAmountConsumer) {
        int level = chestLevel(player);
        if (level <= 0) {
            return false;
        }
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!PerformanceUtils.isWindowActive(pdc, combatKey)) {
            pdc.remove(combatKey);
            return false;
        }
        double reduction = config.getRegenReductionPerLevel() * level;
        if (reduction <= 0) {
            return false;
        }
        double scale = Math.max(0.0d, 1.0d - reduction);
        if (amount <= 0.0d) {
            return false;
        }
        scaledAmountConsumer.accept(amount * scale);
        return true;
    }

    public @Nullable Integer adjustedFoodLevel(@NotNull Player player, int newFoodLevel) {
        int level = chestLevel(player);
        if (level <= 0) {
            return null;
        }
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!PerformanceUtils.isWindowActive(pdc, combatKey)) {
            pdc.remove(combatKey);
            return null;
        }
        int currentFood = player.getFoodLevel();
        if (newFoodLevel >= currentFood) {
            return null;
        }
        int extraLoss = config.getExtraHungerLossPerLevel() * level;
        if (extraLoss <= 0) {
            return null;
        }
        return Math.max(0, newFoodLevel - extraLoss);
    }

    private int chestLevel(@NotNull Player player) {
        if (enchant == null) {
            return 0;
        }
        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return 0;
        }
        ItemStack chest = equipment.getChestplate();
        if (chest == null) {
            return 0;
        }
        return EnchantCache.getLevel(chest, enchant);
    }
}
