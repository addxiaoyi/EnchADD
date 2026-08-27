package net.enchadd.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Security listener to prevent shulker_box_dupe and drop_item_dupe exploits.
 *
 * The shulker box dupe exploit works by:
 * 1. Placing a shulker box with items inside
 * 2. Opening the shulker box and taking items out
 * 3. While the shulker box GUI is open, placing another shulker box inside the first one
 * 4. This can cause the items to duplicate due to client-server state desync
 *
 * The drop_item_dupe exploit works by:
 * 1. Breaking a block that triggers item drops
 * 2. Using client manipulation to desync item drop count
 * 3. Items can be duplicated through rapid break/replace cycles
 *
 * This listener blocks the dangerous interaction patterns that enable these exploits.
 */
public class DupeListener implements Listener {

    // Track players who have shulker boxes open with nested shulker boxes
    private final Set<UUID> shulkerBoxOpen = ConcurrentHashMap.newKeySet();

    // Track players in synthetic drop handling to prevent drop_item_dupe
    private final Set<UUID> dropGuardActive = ConcurrentHashMap.newKeySet();

    // Track players who have used ender pearls recently (anti-dupe protection)
    private final Set<UUID> pearlCooldown = ConcurrentHashMap.newKeySet();

    // Cooldown duration in ticks for pearl protection
    private static final long PEARL_COOLDOWN_TICKS = 5;

    /**
     * Guard check to prevent shulker_box_dupe exploit.
     * Returns true if placing a shulker box is safe (no other shulker box is already open),
     * false if a shulker box is already open (potential exploit attempt).
     *
     * @param player the player attempting to interact
     * @return true if safe to place shulker box, false if exploit detected
     */
    public boolean add_shulker_guard(Player player) {
        if (shulkerBoxOpen.contains(player.getUniqueId())) {
            return false;
        }
        shulkerBoxOpen.add(player.getUniqueId());
        return true;
    }

    /**
     * Removes player from shulker box tracking when they close the inventory.
     */
    public void remove_shulker_guard(Player player) {
        shulkerBoxOpen.remove(player.getUniqueId());
    }

    /**
     * Guard check to prevent drop_item_dupe exploit.
     * Returns true if safe to process drop (no active drop handling),
     * false if already processing drops (potential exploit attempt).
     *
     * @param player the player who triggered the drop
     * @return true if safe to process drops, false if exploit detected
     */
    public boolean add_drop_guard(Player player) {
        UUID uuid = player.getUniqueId();
        if (dropGuardActive.contains(uuid)) {
            return false;
        }
        dropGuardActive.add(uuid);
        return true;
    }

    /**
     * Removes player from drop guard tracking when drop handling completes.
     */
    public void remove_drop_guard(Player player) {
        dropGuardActive.remove(player.getUniqueId());
    }

    /**
     * Checks if a player is currently in synthetic drop handling.
     *
     * @param player the player to check
     * @return true if player is in drop guard state
     */
    public boolean isDropGuardActive(Player player) {
        return dropGuardActive.contains(player.getUniqueId());
    }

    /**
     * Checks if a player has a shulker box currently open.
     *
     * @param player the player to check
     * @return true if player has a shulker box open
     */
    public boolean hasShulkerBoxOpen(Player player) {
        return shulkerBoxOpen.contains(player.getUniqueId());
    }

    /**
     * Validates that the item is a shulker box.
     *
     * @param item the item to check
     * @return true if the item is a shulker box
     */
    private boolean isShulkerBox(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        Material type = item.getType();
        return type == Material.SHULKER_BOX
            || type.name().endsWith("_SHULKER_BOX");
    }

    /**
     * Checks if the inventory contains any shulker boxes.
     *
     * @param inventory the inventory to check
     * @return true if the inventory contains at least one shulker box
     */
    private boolean inventoryContainsShulkerBox(org.bukkit.inventory.Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        for (ItemStack item : inventory.getContents()) {
            if (isShulkerBox(item)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Check if player has a shulker box already open
        if (!hasShulkerBoxOpen(player)) {
            return;
        }

        // Allow bottom inventory clicks (taking items from shulker box)
        if (event.getView().getBottomInventory() instanceof PlayerInventory) {
            return;
        }

        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Block placing a shulker box while another shulker box is open
        if (isShulkerBox(currentItem) || isShulkerBox(cursorItem)) {
            // Check if there's already a shulker box in the target inventory
            if (inventoryContainsShulkerBox(event.getInventory())) {
                event.setCancelled(true);
                logBlockedInteraction(player, "shulker_box_nested_place", event.getInventory().getType().toString());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!hasShulkerBoxOpen(player)) {
            return;
        }

        // Check if dragging a shulker box into an inventory that already has one
        for (ItemStack item : event.getNewItems().values()) {
            if (isShulkerBox(item)) {
                if (inventoryContainsShulkerBox(event.getInventory())) {
                    event.setCancelled(true);
                    logBlockedInteraction(player, "shulker_box_drag_nested", event.getInventory().getType().toString());
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (isShulkerBox(event.getItem())) {
            // Block moving shulker boxes between inventories that are part of a chain
            if (event.getSource().getHolder() != null && event.getDestination().getHolder() != null) {
                // Log potential exploit attempt
                org.bukkit.Bukkit.getLogger().warning(
                    "[EnchADD Security] Blocked shulker_box_chain_move: " + event.getItem().getType()
                );
                event.setCancelled(true);
            }
        }
    }

    /**
     * Logs blocked dupe interaction attempts.
     */
    private void logBlockedInteraction(Player player, String exploitType, String inventoryType) {
        org.bukkit.Bukkit.getLogger().warning(String.format(
            "[EnchADD Security] Blocked %s exploit - player: %s, inventory: %s",
            exploitType,
            player.getName(),
            inventoryType
        ));
    }
}
