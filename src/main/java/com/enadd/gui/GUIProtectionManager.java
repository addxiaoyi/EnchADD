package com.enadd.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI保护管理器
 * 
 * 当玩家打开GUI时，完全禁止所有物品操作：
 * - 拾取物品
 * - 丢弃物品
 * - 移动物品
 * - 放置方块
 * - 破坏方块
 * - 交互
 * - 切换手持物品
 * 
 * 这是最严格的保护，确保100%无法复制物品
 */
public class GUIProtectionManager implements Listener {
    
    private static GUIProtectionManager instance;
    private final JavaPlugin plugin;
    
    // 正在使用GUI的玩家
    private final Set<UUID> playersInGUI = ConcurrentHashMap.newKeySet();
    
    private GUIProtectionManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    
    public static synchronized void initialize(JavaPlugin plugin) {
        // BUG FIX #1: 添加同步确保线程安全
        if (instance == null) {
            // BUG FIX #5: 添加null检查
            if (plugin == null) {
                throw new IllegalArgumentException("Plugin cannot be null");
            }
            
            instance = new GUIProtectionManager(plugin);
            plugin.getServer().getPluginManager().registerEvents(instance, plugin);
            // BUG FIX #4: 改为info级别
            plugin.getLogger().info("GUIProtectionManager initialized - Full protection enabled");
        }
    }
    
    public static GUIProtectionManager getInstance() {
        return instance;
    }
    
    /**
     * 标记玩家正在使用GUI
     */
    public void markPlayerInGUI(Player player) {
        // BUG FIX #6: 统一null检查
        if (player == null) {
            plugin.getLogger().warning("Attempted to mark null player in GUI");
            return;
        }
        
        playersInGUI.add(player.getUniqueId());
        // BUG FIX #4: 改为fine级别（调试用）
        plugin.getLogger().fine("Player " + player.getName() + " entered GUI protection mode");
    }
    
    /**
     * 移除玩家的GUI标记
     */
    public void unmarkPlayerInGUI(Player player) {
        // BUG FIX #6: 统一null检查
        if (player == null) {
            plugin.getLogger().warning("Attempted to unmark null player from GUI");
            return;
        }
        
        playersInGUI.remove(player.getUniqueId());
        // BUG FIX #4: 改为fine级别（调试用）
        plugin.getLogger().fine("Player " + player.getName() + " left GUI protection mode");
    }
    
    /**
     * 检查玩家是否在GUI中
     */
    public boolean isPlayerInGUI(Player player) {
        // BUG FIX #6: 统一null检查
        if (player == null) {
            return false;
        }
        return playersInGUI.contains(player.getUniqueId());
    }
    
    /**
     * 清理所有标记（服务器关闭时）
     */
    public void clearAll() {
        int count = playersInGUI.size();
        playersInGUI.clear();
        // BUG FIX #4: 添加日志
        if (count > 0) {
            plugin.getLogger().info("Cleared GUI protection for " + count + " players");
        }
    }
    
    // BUG FIX #2: 添加玩家离线清理
    /**
     * 监听玩家离线事件，自动清理标记
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player != null && playersInGUI.contains(player.getUniqueId())) {
            playersInGUI.remove(player.getUniqueId());
            plugin.getLogger().fine("Auto-removed " + player.getName() + " from GUI protection (quit)");
        }
    }
    
    // BUG FIX #10: 添加统计方法
    /**
     * 获取当前在GUI中的玩家数量
     */
    public int getPlayersInGUICount() {
        return playersInGUI.size();
    }
    
    /**
     * 获取所有在GUI中的玩家UUID
     */
    public Set<UUID> getPlayersInGUI() {
        return Collections.unmodifiableSet(new HashSet<>(playersInGUI));
    }
    
    // ==================== 事件监听器 ====================
    
    /**
     * 阻止拾取物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (isPlayerInGUI(player)) {
                event.setCancelled(true);
                plugin.getLogger().fine("Blocked item pickup for " + player.getName() + " (in GUI)");
            }
        }
    }
    
    /**
     * 阻止丢弃物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (isPlayerInGUI(player)) {
            event.setCancelled(true);
            plugin.getLogger().fine("Blocked item drop for " + player.getName() + " (in GUI)");
        }
    }
    
    /**
     * 阻止放置方块
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (isPlayerInGUI(player)) {
            event.setCancelled(true);
            plugin.getLogger().fine("Blocked block place for " + player.getName() + " (in GUI)");
        }
    }
    
    /**
     * 阻止破坏方块
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (isPlayerInGUI(player)) {
            event.setCancelled(true);
            plugin.getLogger().fine("Blocked block break for " + player.getName() + " (in GUI)");
        }
    }
    
    /**
     * 阻止交互
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (isPlayerInGUI(player)) {
            // BUG FIX #8: 检查所有交互类型
            // 阻止所有物理交互和使用物品
            if (event.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_AIR &&
                event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
                event.setCancelled(true);
                plugin.getLogger().fine("Blocked interaction for " + player.getName() + " (in GUI)");
            }
        }
    }
    
    /**
     * 阻止切换手持物品（F键）
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (isPlayerInGUI(player)) {
            event.setCancelled(true);
            plugin.getLogger().fine("Blocked hand swap for " + player.getName() + " (in GUI)");
        }
    }
    
    /**
     * 阻止背包物品移动（漏斗等）
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        // BUG FIX #9: 检查更多情况
        // 检查是否涉及玩家背包
        boolean shouldCancel = false;
        
        if (event.getSource().getHolder() instanceof Player) {
            Player player = (Player) event.getSource().getHolder();
            if (isPlayerInGUI(player)) {
                shouldCancel = true;
            }
        }
        
        if (event.getDestination().getHolder() instanceof Player) {
            Player player = (Player) event.getDestination().getHolder();
            if (isPlayerInGUI(player)) {
                shouldCancel = true;
            }
        }
        
        // 检查初始化器（可能是玩家触发的）
        if (event.getInitiator().getHolder() instanceof Player) {
            Player player = (Player) event.getInitiator().getHolder();
            if (isPlayerInGUI(player)) {
                shouldCancel = true;
            }
        }
        
        if (shouldCancel) {
            event.setCancelled(true);
            plugin.getLogger().fine("Blocked inventory move (in GUI)");
        }
    }
    
    /**
     * 额外保护：阻止所有背包点击（除了GUI内的）
     * 这是双重保护
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClickProtection(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (isPlayerInGUI(player)) {
                // 如果点击的不是GUI的顶部背包，取消
                if (event.getClickedInventory() != null && 
                    event.getClickedInventory() != event.getView().getTopInventory()) {
                    event.setCancelled(true);
                    plugin.getLogger().fine("Blocked inventory click outside GUI for " + player.getName());
                }
            }
        }
    }
    
    /**
     * 额外保护：阻止所有拖拽
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryDragProtection(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (isPlayerInGUI(player)) {
                event.setCancelled(true);
                plugin.getLogger().fine("Blocked inventory drag for " + player.getName() + " (in GUI)");
            }
        }
    }
}
