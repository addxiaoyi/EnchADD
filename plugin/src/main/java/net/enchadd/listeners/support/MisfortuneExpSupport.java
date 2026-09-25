package net.enchadd.listeners.support;

import net.enchadd.enchants.MisfortuneEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class MisfortuneExpSupport {

    public void apply(@NotNull EntityDeathEvent event,
                      @NotNull Enchantment enchant,
                      @NotNull MisfortuneEnchant config) {
        if (event.getDamageSource() == null) {
            return;
        }

        if (!(event.getDamageSource().getCausingEntity() instanceof Player killer)) {
            return;
        }

        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(killer);
        if (equipment == null) {
            return;
        }

        ItemStack mainHand = equipment.getItemInMainHand();
        if (mainHand == null) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(mainHand, enchant);
        if (level <= 0) {
            return;
        }

        int originalExp = event.getDroppedExp();
        if (originalExp <= 0) {
            return;
        }

        double basePenalty = Math.max(0.0, config.getXpPenaltyPerLevel() * level);
        double cappedPenalty = Math.max(0.0, Math.min(0.90, Math.min(config.getMaxXpPenalty(), basePenalty)));
        if (cappedPenalty <= 0) {
            return;
        }

        double multiplier = 1.0 - cappedPenalty;
        if (multiplier < 0.0) {
            multiplier = 0.0;
        }

        int newExp = (int) Math.round(originalExp * multiplier);
        if (newExp < 0) {
            newExp = 0;
        }

        event.setDroppedExp(newExp);
    }
}
