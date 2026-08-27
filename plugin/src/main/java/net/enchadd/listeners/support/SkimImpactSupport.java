package net.enchadd.listeners.support;

import net.enchadd.enchants.SkimEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

public final class SkimImpactSupport {

    public void handleWallImpact(@NotNull EntityDamageEvent event,
                                 @NotNull EntityEquipment equipment,
                                 @NotNull Enchantment enchant,
                                 @NotNull SkimEnchant config) {
        int level = PerformanceUtils.getEnchantLevel(equipment.getChestplate(), enchant);
        if (level <= 0) {
            return;
        }

        double reduction = Math.min(config.getMaxDamageReduction(), level * config.getFlatDamageReductionPerLevel());
        if (reduction <= 0.0) {
            return;
        }

        double adjustedDamage = event.getDamage() - reduction;
        if (adjustedDamage <= 0.0) {
            event.setCancelled(true);
            return;
        }
        event.setDamage(adjustedDamage);
    }
}
