package net.enchadd.listeners;

import net.enchadd.listeners.support.LegacyEnchantSanitizerSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;

@SuppressWarnings("UnstableApiUsage")
public final class LegacyEnchantSanitizerListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final LegacyEnchantSanitizerSupport sanitizerSupport = new LegacyEnchantSanitizerSupport(registry);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        sanitizerSupport.sanitizeInventory(event.getPlayer(), "player_inventory", event.getPlayer().getInventory());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        sanitizerSupport.sanitizeInventory(event.getPlayer(), "player_inventory", event.getPlayer().getInventory());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        // Skip grindstone to prevent XP exploit
        if (event.getInventory() instanceof GrindstoneInventory grindstone) {
            if (!sanitizerSupport.addGuardCheck(grindstone)) {
                return;
            }
        }

        Player actor = event.getPlayer() instanceof Player p ? p : null;
        sanitizerSupport.sanitizeInventory(actor, "open_inventory", event.getInventory());
        HumanEntity human = event.getPlayer();
        if (human != null) {
            sanitizerSupport.sanitizeInventory(human instanceof Player p ? p : null, "player_inventory", human.getInventory());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        // Skip grindstone single-slot interactions to prevent XP exploit
        if (event.getInventory() instanceof GrindstoneInventory grindstone) {
            if (!sanitizerSupport.addGuardCheck(grindstone)) {
                return;
            }
        }

        ItemStack current = event.getCurrentItem();
        Player actor = event.getWhoClicked() instanceof Player p ? p : null;
        if (sanitizerSupport.sanitizeItem(actor, "click_current", event.getSlot(), current)) {
            event.setCurrentItem(current);
        }

        ItemStack cursor = event.getCursor();
        if (sanitizerSupport.sanitizeItem(actor, "click_cursor", -1, cursor)) {
            event.setCursor(cursor);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        // Prevent anvil_single_slot exploit: only process when both anvil slots have items
        AnvilInventory anvil = event.getInventory();
        if (!sanitizerSupport.addGuardCheck(anvil)) {
            return;
        }

        ItemStack result = event.getResult();
        if (sanitizerSupport.sanitizeItem(null, "anvil_result", 2, result)) {
            event.setResult(result);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        // Prevent smithing_single_slot exploit: only process when both smithing slots have items
        SmithingInventory smithing = event.getInventory();
        if (!sanitizerSupport.addGuardCheck(smithing)) {
            return;
        }

        ItemStack result = event.getResult();
        if (sanitizerSupport.sanitizeItem(null, "smithing_result", 3, result)) {
            event.setResult(result);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        // Prevent grindstone_single_slot exploit: only process when both grindstone
        // slots have items to avoid triggering Paper's GUI sync bug that can cause
        // infinite XP farming.
        GrindstoneInventory grindstone = event.getInventory();
        if (!sanitizerSupport.addGuardCheck(grindstone)) {
            return;
        }

        ItemStack result = event.getResult();
        if (sanitizerSupport.sanitizeItem(null, "grindstone_result", 2, result)) {
            event.setResult(result);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        ItemStack stack = event.getItem().getItemStack();
        Player player = event.getEntity() instanceof Player p ? p : null;
        if (sanitizerSupport.sanitizeItem(player, "pickup", -1, stack)) {
            event.getItem().setItemStack(stack);
        }
    }
}
