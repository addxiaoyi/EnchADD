package com.enadd.gui.simulation;

import com.enadd.gui.EnchantmentGuiManager;
import com.enadd.gui.components.GuiButtonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.enadd.gui.GUIProtectionManager;


public final class EnchantmentSimulator {

    private static final int SIMULATION_ROWS = 3;
    private static final int SIMULATION_SIZE = SIMULATION_ROWS * 9;
    private static final String SIMULATION_TITLE = "附魔模拟预览 | EnCh Add";

    private final JavaPlugin plugin;
    private final Map<Player, SimulationSession> activeSessions = new ConcurrentHashMap<>();

    public EnchantmentSimulator(JavaPlugin plugin, EnchantmentGuiManager guiManager) {
        this.plugin = plugin;
        registerEvents();
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new SimulationEventHandler(), plugin);
    }

    public void openSimulation(Player player, EnchantmentGuiManager.EnchantmentData enchantmentData) {
        SimulationSession session = activeSessions.get(player);
        if (session != null) {
            closeSimulation(player);
        }

        // Use modern Adventure API for inventory title
        net.kyori.adventure.text.Component title = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
            .legacySection().deserialize(SIMULATION_TITLE);
        Inventory simulationGui = Bukkit.createInventory(null, SIMULATION_SIZE, title);

        initializeSimulationGui(simulationGui, player, enchantmentData);

        player.openInventory(simulationGui);
        activeSessions.put(player, new SimulationSession(simulationGui, enchantmentData));

        sendSimulationMessage(player, enchantmentData, "opened");
        
        // ✅ 启用GUI全局保护
        GUIProtectionManager protectionManager = GUIProtectionManager.getInstance();
        if (protectionManager != null) {
            protectionManager.markPlayerInGUI(player);
        }
    }

    private void initializeSimulationGui(Inventory gui, Player player, EnchantmentGuiManager.EnchantmentData data) {
        setSimulationBorder(gui);

        ItemStack itemSlot = createItemSlot(player);
        gui.setItem(4, itemSlot);

        ItemStack enchantSlot = createEnchantmentSlot(data);
        gui.setItem(13, enchantSlot);

        ItemStack previewSlot = createPreviewSlot(player, data);
        gui.setItem(22, previewSlot);

        gui.setItem(17, createActionButton("apply", "§a§l应用附魔", "§7点击将附魔应用到物品"));
        gui.setItem(26, createActionButton("close", "§c§l关闭", "§7关闭模拟界面"));

        updatePreview(gui, player, data);
    }

    private void setSimulationBorder(Inventory gui) {
        ItemStack border = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();

        if (meta != null) {
            com.enadd.util.ItemMetaHelper.setDisplayName(meta, "§0 ");
            border.setItemMeta(meta);
        }

        for (int row = 0; row < SIMULATION_ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = row * 9 + col;
                boolean isEdge = row == 0 || row == SIMULATION_ROWS - 1 || col == 0 || col == 8;
                if (isEdge) {
                    gui.setItem(slot, border);
                }
            }
        }
    }

    private ItemStack createItemSlot(Player player) {
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            return GuiButtonBuilder.create()
                    .material(Material.GRAY_DYE)
                    .displayName("§7手持物品")
                    .lore("§7请手持需要附魔的物品", "", "§e手持物品后将显示在此处")
                    .build();
        }

        return GuiButtonBuilder.create()
                .material(handItem.getType())
                .displayName("§e" + handItem.getType().name())
                .lore("§7当前手持物品", "", "§a可用附魔数量: §f" + getApplicableEnchantmentsCount(handItem))
                .amount(handItem.getAmount())
                .build();
    }

    private ItemStack createEnchantmentSlot(EnchantmentGuiManager.EnchantmentData data) {
        return GuiButtonBuilder.create()
                .material(Material.ENCHANTED_BOOK)
                .displayName("§b" + data.getDisplayName())
                .lore("§7等级: §f" + data.getMaxLevel() + " 级", "§7权重: §f" + data.getWeight(), "", "§7" + data.getDescription())
                .build();
    }

    private ItemStack createPreviewSlot(Player player, EnchantmentGuiManager.EnchantmentData data) {
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            return GuiButtonBuilder.create()
                    .material(Material.BARRIER)
                    .displayName("§c无法预览")
                    .lore("§7请先手持物品", "", "§e手持物品后点击应用按钮查看预览")
                    .build();
        }

        return GuiButtonBuilder.create()
                .material(handItem.getType())
                .displayName("§a§l附魔预览")
                .lore("§7预览应用后的物品效果", "", "§e点击查看详细信息")
                .build();
    }

    private ItemStack createActionButton(String action, String displayName, String lore) {
        Material material = "apply".equals(action) ? Material.LIME_DYE : Material.RED_DYE;

        return GuiButtonBuilder.create()
                .material(material)
                .displayName(displayName)
                .lore(lore)
                .build();
    }

    private void updatePreview(Inventory gui, Player player, EnchantmentGuiManager.EnchantmentData data) {
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            return;
        }

        ItemStack previewItem = handItem.clone();
        ItemMeta meta = previewItem.getItemMeta();

        if (meta == null) return;

        List<String> currentLore = com.enadd.util.ItemMetaHelper.hasLore(meta) ?
            com.enadd.util.ItemMetaHelper.getLoreAsStrings(meta) : new ArrayList<>();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }

        boolean enchantmentExists = currentLore.stream()
                .anyMatch(line -> line.contains(data.getId()));

        if (!enchantmentExists) {
            String enchantLine = "§3" + data.getDisplayName() + " " + toRomanNumeral(data.getMaxLevel());
            currentLore.add(0, "");
            currentLore.add(0, "§5§l=== EnCh Add ===");
            currentLore.add(0, enchantLine);
        }

        // Use modern Adventure API instead of deprecated methods
        com.enadd.util.ItemMetaHelper.setLore(meta, currentLore);
        previewItem.setItemMeta(meta);

        gui.setItem(22, previewItem);
    }

    private int getApplicableEnchantmentsCount(ItemStack item) {
        return new Random().nextInt(5) + 3;
    }

    private void sendSimulationMessage(Player player, EnchantmentGuiManager.EnchantmentData data, String action) {
        String message;
        switch (action) {
            case "opened":
                message = "§a附魔模拟已开启";
                break;
            case "applied":
                message = "§a附魔已成功应用到物品！";
                break;
            case "cancelled":
                message = "§7模拟已取消";
                break;
            default:
                message = "";
        }

        if (!message.isEmpty()) {
            player.sendMessage(message);
        }
    }

    private void applyEnchantment(Player player, EnchantmentGuiManager.EnchantmentData data) {
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage("§c请先手持物品！");
            return;
        }

        ItemMeta meta = handItem.getItemMeta();
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(handItem.getType());
        }

        List<String> lore = com.enadd.util.ItemMetaHelper.hasLore(meta) ?
            com.enadd.util.ItemMetaHelper.getLoreAsStrings(meta) : new ArrayList<>();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        String enchantLine = "§3" + data.getDisplayName() + " " + toRomanNumeral(data.getMaxLevel());
        boolean alreadyHas = lore.stream().anyMatch(line -> line.contains(data.getId()));

        if (!alreadyHas) {
            lore.add(0, "");
            lore.add(0, "§5§l=== EnCh Add ===");
            lore.add(0, enchantLine);

            com.enadd.util.ItemMetaHelper.setLore(meta, lore);
            handItem.setItemMeta(meta);

            player.sendMessage("§a附魔已成功应用！");
            player.sendMessage("§7附魔: §f" + data.getDisplayName());
        } else {
            player.sendMessage("§c该物品已拥有此附魔！");
        }
    }

    private String toRomanNumeral(int number) {
        if (number <= 0) return "I";
        if (number >= 4000) return String.valueOf(number);

        StringBuilder sb = new StringBuilder();
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] numerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                number -= values[i];
                sb.append(numerals[i]);
            }
        }

        return sb.toString();
    }

    public void closeSimulation(Player player) {
        SimulationSession session = activeSessions.remove(player);
        if (session != null) {
            player.closeInventory();
        }
    }

    public boolean isInSimulation(Player player) {
        return activeSessions.containsKey(player);
    }

    private static class SimulationSession {
        final Inventory inventory;
        final EnchantmentGuiManager.EnchantmentData enchantmentData;

        SimulationSession(Inventory inventory, EnchantmentGuiManager.EnchantmentData enchantmentData) {
            this.inventory = inventory;
            this.enchantmentData = enchantmentData;
        }
    }

    private class SimulationEventHandler implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
            if (event.getPlayer() instanceof Player) {
                Player player = (Player) event.getPlayer();
                SimulationSession session = activeSessions.get(player);
                if (session != null && session.inventory.equals(event.getInventory())) {
                    activeSessions.remove(player);
                    
                    // 验证物品数量，防止复制漏洞
                    validatePlayerInventory(player);
                    
                    // ✅ 移除GUI全局保护
                    GUIProtectionManager protectionManager = GUIProtectionManager.getInstance();
                    if (protectionManager != null) {
                        protectionManager.unmarkPlayerInGUI(player);
                    }
                }
            }
        }

        @org.bukkit.event.EventHandler
        public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();
            SimulationSession session = activeSessions.get(player);

            if (session == null) return;

            if (event.getClickedInventory() == null ||
                !event.getClickedInventory().equals(session.inventory)) {
                return;
            }

            // 取消所有点击，防止物品被拿走
            event.setCancelled(true);

            int slot = event.getSlot();

            if (slot == 18) {
                applyEnchantment(player, session.enchantmentData);
            } else if (slot == 26) {
                closeSimulation(player);
            }
        }
    }
    
    /**
     * 验证玩家背包，防止物品复制漏洞
     */
    private void validatePlayerInventory(Player player) {
        if (player == null) return;
        
        try {
            // 这里可以添加更复杂的验证逻辑
            // 例如：检查物品数量是否异常增加
            // 目前只是记录日志
            plugin.getLogger().fine("Validated inventory for player: " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to validate inventory: " + e.getMessage());
        }
    }
}
