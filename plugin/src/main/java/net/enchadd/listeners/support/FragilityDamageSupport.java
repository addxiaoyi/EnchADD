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

        double perLevel = config.getExtraDurabilityPerLevel();
        if (perLevel <= 0) {
            return;
        }

        double multiplier = 1.0 + perLevel * level;
        double maxMultiplier = config.getMaxDurabilityMultiplier();
        if (maxMultiplier > 1.0 && multiplier > maxMultiplier) {
            multiplier = maxMultiplier;
        }
        if (multiplier <= 1.0) {
            return;
        }

        double scaled = originalDamage * multiplier;
        int newDamage = (int) Math.round(scaled);
        if (newDamage <= originalDamage) {
            return;
        }

        event.setDamage(newDamage);
    }
}
