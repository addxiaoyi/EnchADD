package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ReboundEnchant;
import net.enchadd.listeners.support.ReboundDurabilitySupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("UnstableApiUsage")
public class ReboundListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(ReboundEnchant.KEY);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (enchant == null) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        int level = net.enchadd.utils.EnchantCache.getLevel(item, enchant);
        if (level <= 0) return;
        if (!(EnchADDConfig.ENCHANTS.get(ReboundEnchant.KEY) instanceof ReboundEnchant reboundEnchant)) return;
        int originalDamage = event.getDamage();
        double chance = ReboundDurabilitySupport.refundChance(originalDamage, level,
                reboundEnchant.getMaxLevel(), reboundEnchant.getRefundChancePerLevel());
        if (chance <= 0) return;
        if (!PerformanceUtils.rollChance(chance)) return;
        event.setDamage(originalDamage - 1);
    }
}
