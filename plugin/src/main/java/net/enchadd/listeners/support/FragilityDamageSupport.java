package net.enchadd.listeners.support;

import net.enchadd.enchants.FragilityEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class FragilityDamageSupport {

    public void apply(@NotNull PlayerItemDamageEvent event,
                      @NotNull Enchantment enchant,
                      @NotNull FragilityEnchant config) {
        if (event.isCancelled()) return;
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(item, enchant);
        if (level <= 0) {
            return;
        }

        int originalDamage = event.getDamage();
        if (originalDamage <= 0) {
            return;
        }

        int newDamage = CursePenaltySupport.durabilityDamage(originalDamage, level, config.getMaxLevel(),
                config.getExtraDurabilityPerLevel(), config.getMaxDurabilityMultiplier());
        if (newDamage <= originalDamage) {
            return;
        }

        event.setDamage(newDamage);
    }
}
