package net.enchadd.listeners.support;

import net.enchadd.enchants.BreakguardEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;

public final class BreakguardDamageSupport {

    public double resolveBonusDamage(@NotNull LivingEntity attacker,
                                     @NotNull Enchantment enchant,
                                     @NotNull BreakguardEnchant config) {
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(attacker);
        if (equipment == null) {
            return 0.0;
        }

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) {
            return 0.0;
        }

        return Math.min(config.getMaxBonusDamage(), level * config.getBonusDamagePerLevel());
    }
}
