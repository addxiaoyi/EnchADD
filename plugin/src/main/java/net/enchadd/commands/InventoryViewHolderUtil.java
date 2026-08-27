package net.enchadd.commands;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;

import java.util.ArrayList;
import java.util.List;

final class InventoryViewHolderUtil {

    private InventoryViewHolderUtil() {
    }

    static List<Inventory> collectOpenInventories(HumanEntity player) {
        List<Inventory> inventories = new ArrayList<>();
        if (player == null) {
            return inventories;
        }
        InventoryView openInventory = player.getOpenInventory();
        if (openInventory == null) {
            return inventories;
        }
        if (openInventory.getTopInventory() != null) {
            inventories.add(openInventory.getTopInventory());
        }
        if (openInventory.getBottomInventory() != null && openInventory.getBottomInventory() != player.getInventory()) {
            inventories.add(openInventory.getBottomInventory());
        }
        return inventories;
    }
}
