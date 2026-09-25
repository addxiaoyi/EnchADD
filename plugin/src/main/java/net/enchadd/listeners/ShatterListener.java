package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ShatterEnchant;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class ShatterListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(ShatterEnchant.KEY);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (enchant == null) return;
        Player player = event.getPlayer();
        if (player == null) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        int level = net.enchadd.utils.EnchantCache.getLevel(item, enchant);
        if (level <= 0) return;
        if (!(EnchADDConfig.ENCHANTS.get(ShatterEnchant.KEY) instanceof ShatterEnchant ench)) return;
        double perLevel = ench.getTriggerChancePerLevel();
        if (perLevel <= 0) return;
        double chance = perLevel * level;
        if (!Double.isFinite(chance)) return;
        double maxChance = ench.getMaxTriggerChance();
        if (maxChance > 0) {
            chance = Math.min(0.75d, Math.min(maxChance, chance));
        }
        chance = Math.max(0.0d, chance);
        if (chance <= 0) return;
        if (!PerformanceUtils.rollChance(chance)) return;
        int extraPerLevel = ench.getExtraDamagePerLevel();
        if (extraPerLevel <= 0) return;
        int original = event.getDamage();
        if (original <= 0) return;
        long extra = (long) extraPerLevel * level;
        if (extra <= 0) return;
        long combined = original + extra;
        int capped = combined > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) combined;
        if (capped <= original) return;
        event.setDamage(capped);
    }
}

