package com.enadd.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.enadd.EnchAdd;
import java.util.HashMap;
import java.util.Map;


public class GUIManager implements Listener {

    private static final Map<String, EnchantmentChestGUI> playerGuis = new HashMap<>();
    private final JavaPlugin plugin;

    public GUIManager(EnchAdd enchAdd, JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public GUIManager(EnchAdd enchAdd) {
        this.plugin = enchAdd; // Use the EnchAdd instance as the plugin
    }

    public void openEnchantmentGUI(Player player) {
        if (playerGuis.containsKey(player.getUniqueId().toString())) {
            player.closeInventory();
        }

        EnchantmentChestGUI gui = new EnchantmentChestGUI(player, plugin);
        playerGuis.put(player.getUniqueId().toString(), gui);
        gui.open();
        
        // ✅ 启用GUI全局保护（双重保护）
        GUIProtectionManager protectionManager = GUIProtectionManager.getInstance();
        if (protectionManager != null) {
            protectionManager.markPlayerInGUI(player);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String playerId = player.getUniqueId().toString();
        if (playerGuis.containsKey(playerId)) {
            EnchantmentChestGUI gui = playerGuis.get(playerId);
            // ✅ 修复：使用InventoryHolder而不是标题检查
            if (event.getInventory().getHolder() == gui) {
                gui.handleClick(event);
            }
        }
    }
    
    @EventHandler
    public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String playerId = player.getUniqueId().toString();
        if (playerGuis.containsKey(playerId)) {
            EnchantmentChestGUI gui = playerGuis.get(playerId);
            // ✅ 修复：阻止拖拽到GUI中
            if (event.getInventory().getHolder() == gui) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String playerId = player.getUniqueId().toString();
        if (playerGuis.containsKey(playerId)) {
            EnchantmentChestGUI gui = playerGuis.get(playerId);
            gui.handleClose(event);
            playerGuis.remove(playerId);
            
            // ✅ 移除GUI全局保护（双重保护）
            GUIProtectionManager protectionManager = GUIProtectionManager.getInstance();
            if (protectionManager != null) {
                protectionManager.unmarkPlayerInGUI(player);
            }
        }
    }

    @SuppressWarnings("unused")
    public static void closeAllGUIs() {
        for (EnchantmentChestGUI gui : playerGuis.values()) {
            // GUIs will be closed automatically when players logout
        }
        playerGuis.clear();
    }
}
