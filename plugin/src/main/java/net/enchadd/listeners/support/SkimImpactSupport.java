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
        if (event.isCancelled() || event.getCause() != EntityDamageEvent.DamageCause.FLY_INTO_WALL
                || !Double.isFinite(event.getFinalDamage()) || event.getFinalDamage() <= 0) return;
        int level = PerformanceUtils.getEnchantLevel(equipment.getChestplate(), enchant);
        if (level <= 0) {
            return;
        }

        double baseDamage = event.getDamage();
        double adjustedDamage = reducedDamage(baseDamage, level, config.getMaxLevel(),
                config.getFlatDamageReductionPerLevel(), config.getMaxDamageReduction());
        if (Double.compare(adjustedDamage, baseDamage) == 0) return;
        if (adjustedDamage <= 0.0) {
            event.setCancelled(true);
            return;
        }
        event.setDamage(adjustedDamage);
    }

    static double reducedDamage(double base, int level, int maxLevel, double perLevel, double maximum) {
        if (!Double.isFinite(base) || base <= 0) return base;
        double reduction = EnchantDamageSupport.bonusDamage(Math.min(level, maxLevel), perLevel, maximum);
        return Math.max(0, base - reduction);
    }
}
