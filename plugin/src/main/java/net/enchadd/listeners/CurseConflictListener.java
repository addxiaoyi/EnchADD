package net.enchadd.listeners;

import net.enchadd.listeners.support.CurseConflictSupport;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class CurseConflictListener implements Listener {

    private final CurseConflictSupport conflictSupport = new CurseConflictSupport();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        Map<Enchantment, Integer> toAdd = event.getEnchantsToAdd();
        if (!toAdd.isEmpty()) {
            conflictSupport.filterEnchantRoll(item, toAdd);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        if (inventory == null) {
            return;
        }

        ItemStack left = inventory.getFirstItem();
        ItemStack right = inventory.getSecondItem();
        ItemStack result = event.getResult();
        if (result == null) {
            return;
        }

        Map<Enchantment, Integer> resultEnchants = result.getEnchantments();
        if (!resultEnchants.isEmpty() && conflictSupport.shouldRejectAnvilResult(left, right, resultEnchants)) {
            event.setResult(null);
        }
    }
}
