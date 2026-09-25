package net.enchadd.listeners.support;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import net.enchadd.enchants.BraceEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class BraceKnockbackSupport {

    public void applyReduction(@NotNull EntityKnockbackEvent event,
                               @NotNull Player player,
                               @NotNull Enchantment enchant,
                               @NotNull BraceEnchant config) {
        if (event.isCancelled()) return;
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInOffHand(), enchant);
        if (level <= 0) {
            return;
        }

        Vector reduced = KnockbackRules.reduce(event.getKnockback(), level, config.getMaxLevel(),
                config.getKnockbackReductionPerLevel(), config.getMaxReduction(), 0.8);
        if (reduced != null) event.setKnockback(reduced);
    }
}
