package com.enadd.gui;

import com.enadd.core.registry.EnchantmentRegistry;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentRegistry;
import com.enadd.enchantments.enhanced.EnhancedEnchantmentData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;


public class EnchantmentChestGUI implements org.bukkit.inventory.InventoryHolder {

    private static final int INVENTORY_SIZE = 54;
    private final int CATEGORY_SLOT = 49;
    private final int PREVIEW_SLOT = 53;

    private final Player player;
    private JavaPlugin plugin;
    private Inventory currentInventory;  // 添加当前背包引用
    private final List<GUIEnchantment> currentEnchantments;
    private final Map<String, List<GUIEnchantment>> categoryMap;
    private String currentCategory;
    private int currentPage;
    private static final int ITEMS_PER_PAGE = 45;

    public EnchantmentChestGUI(Player player, JavaPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
        this.currentEnchantments = new ArrayList<>();
        this.categoryMap = new HashMap<>();
        this.currentCategory = "all";
        this.currentPage = 0;

        initializeCategories();
        loadAllEnchantments();
    }

    private void initializeCategories() {
        categoryMap.put("all", new ArrayList<>());
        categoryMap.put("weapon", new ArrayList<>());
        categoryMap.put("armor", new ArrayList<>());
        categoryMap.put("tool", new ArrayList<>());
        categoryMap.put("curse", new ArrayList<>());
        categoryMap.put("combat_enhanced", new ArrayList<>());
        categoryMap.put("armor_enhanced", new ArrayList<>());
        categoryMap.put("tool_enhanced", new ArrayList<>());
        categoryMap.put("curse_enhanced", new ArrayList<>());
    }

    private void loadAllEnchantments() {
        Map<String, EnchantmentRegistry.EnchantmentInfo> basicEnchants =
            EnchantmentRegistry.getAllEnchantments();

        if (basicEnchants != null) {
            for (Map.Entry<String, EnchantmentRegistry.EnchantmentInfo> entry : basicEnchants.entrySet()) {
                if (entry == null || entry.getValue() == null) {
                    continue;
                }

                EnchantmentRegistry.EnchantmentInfo info = entry.getValue();
                GUIEnchantment guiEnchant = new GUIEnchantment(
                    entry.getKey(),
                    info.getName(),
                    info.getDescription(),
                    info.getRarity(),
                    info.getMaxLevel(),
                    info.getType(),
                    false
                );

                List<GUIEnchantment> allList = categoryMap.get("all");
                if (allList != null) {
                    allList.add(guiEnchant);
                }
                categorizeEnchantment(guiEnchant);
            }
        }

        Map<String, EnhancedEnchantmentData> enhancedEnchants =
            EnhancedEnchantmentRegistry.getAllEnchantments();

        if (enhancedEnchants != null) {
            for (Map.Entry<String, EnhancedEnchantmentData> entry : enhancedEnchants.entrySet()) {
                if (entry == null || entry.getValue() == null) {
                    continue;
                }

                EnhancedEnchantmentData data = entry.getValue();
                GUIEnchantment guiEnchant = new GUIEnchantment(
                    entry.getKey(),
                    data.getChineseName(),
                    data.getChineseDescription(),
                    getRarityFromCost(data.getBaseCost()),
                    data.getMaxLevel(),
                    data.getCategory() != null ? data.getCategory().name() : "UNKNOWN",
                    true
                );

                List<GUIEnchantment> allList = categoryMap.get("all");
                if (allList != null) {
                    allList.add(guiEnchant);
                }
                categorizeEnchantment(guiEnchant);
            }
        }
    }

    private void categorizeEnchantment(GUIEnchantment enchant) {
        if (enchant == null) {
            return;
        }

        String category = enchant.getCategory();
        if (category == null) {
            return;
        }

        category = category.toLowerCase();

        if (enchant.isEnhanced()) {
            if (category.contains("weapon") || category.contains("bow") || category.contains("trident")) {
                List<GUIEnchantment> list = categoryMap.get("combat_enhanced");
                if (list != null) {
                    list.add(enchant);
                }
            } else if (category.contains("armor") || category.contains("helmet")) {
                List<GUIEnchantment> list = categoryMap.get("armor_enhanced");
                if (list != null) {
                    list.add(enchant);
                }
            } else if (category.contains("digger") || category.contains("fishing") || category.contains("breakable")) {
                List<GUIEnchantment> list = categoryMap.get("tool_enhanced");
                if (list != null) {
                    list.add(enchant);
                }
            } else if (category.contains("vanish")) {
                List<GUIEnchantment> list = categoryMap.get("curse_enhanced");
                if (list != null) {
                    list.add(enchant);
                }
            }
        } else {
            if (category.contains("weapon") || category.contains("bow") || category.contains("trident")) {
                List<GUIEnchantment> list = categoryMap.get("weapon");
                if (list != null) {
                    list.add(enchant);
                }
            } else if (category.contains("armor") || category.contains("helmet")) {
                List<GUIEnchantment> list = categoryMap.get("armor");
                if (list != null) {
                    list.add(enchant);
                }
            } else if (category.contains("digger") || category.contains("fishing") || category.contains("breakable")) {
                List<GUIEnchantment> list = categoryMap.get("tool");
                if (list != null) {
                    list.add(enchant);
                }
            } else if (category.contains("vanish")) {
                List<GUIEnchantment> list = categoryMap.get("curse");
                if (list != null) {
                    list.add(enchant);
                }
            }
        }
    }

    private String getRarityFromCost(int baseCost) {
        if (baseCost >= 50) return "epic";
        if (baseCost >= 35) return "rare";
        if (baseCost >= 25) return "uncommon";
        return "common";
    }

    public void open() {
        currentPage = 0;
        updateInventory();
        
        // ✅ 启用GUI全局保护
        GUIProtectionManager protectionManager = GUIProtectionManager.getInstance();
        if (protectionManager != null) {
            protectionManager.markPlayerInGUI(player);
        }
    }

    @SuppressWarnings("deprecation")
    private void updateInventory() {
        String title = getTitle();
        Inventory inv = Bukkit.createInventory(this, INVENTORY_SIZE, title);  // 使用this作为holder
        this.currentInventory = inv;  // 保存引用

        List<GUIEnchantment> enchantments = categoryMap.getOrDefault(currentCategory, categoryMap.get("all"));
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, enchantments.size());

        currentEnchantments.clear();
        for (int i = startIndex; i < endIndex && i < enchantments.size(); i++) {
            currentEnchantments.add(enchantments.get(i));
        }

        for (int i = 0; i < currentEnchantments.size(); i++) {
            int slot = i;
            if (slot >= 9 && slot < 45) {
                int row = slot / 9;
                int col = slot % 9;
                slot = (row + 1) * 9 + col;
            }
            inv.setItem(slot, createEnchantmentItem(currentEnchantments.get(i)));
        }

        setupNavigationItems(inv, enchantments.size());

        player.openInventory(inv);
    }
    
    @Override
    public Inventory getInventory() {
        return currentInventory;
    }

    private String getTitle() {
        String categoryName = getCategoryDisplayName(currentCategory);
        return "§6附魔预览 - " + categoryName + " §7(第" + (currentPage + 1) + "页)";
    }

    private String getCategoryDisplayName(String category) {
        return switch (category) {
            case "all" -> "全部";
            case "weapon" -> "武器";
            case "armor" -> "防具";
            case "tool" -> "工具";
            case "curse" -> "诅咒";
            case "combat_enhanced" -> "战斗·增强";
            case "armor_enhanced" -> "防具·增强";
            case "tool_enhanced" -> "工具·增强";
            case "curse_enhanced" -> "诅咒·增强";
            default -> category;
        };
    }

    private ItemStack createEnchantmentItem(GUIEnchantment enchant) {
        // 使用附魔书代替铁锭等材料
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        // 移除emoji，使用纯文本
        String displayName = getRarityColor(enchant.getRarity()) + enchant.getDisplayName();
        if (enchant.isEnhanced()) {
            displayName = "§d[增强] " + displayName;
        }
        // Use modern Adventure API instead of deprecated method
        com.enadd.util.ItemMetaHelper.setDisplayName(meta, displayName);

        List<String> lore = new ArrayList<>();
        lore.add("§7类别: " + getCategoryDisplayName(enchant.getCategory()));
        lore.add("§7最大等级: " + enchant.getMaxLevel());
        lore.add("§7稀有度: " + getRarityDisplayName(enchant.getRarity()));
        lore.add("");
        lore.add("§f" + enchant.getDescription());
        lore.add("");
        if (enchant.isEnhanced()) {
            lore.add("§d★ 增强版附魔 ★");
            lore.add("");
        }
        lore.add("§8ID: " + enchant.getId());

        // Use modern Adventure API instead of deprecated method
        com.enadd.util.ItemMetaHelper.setLore(meta, lore);
        item.setItemMeta(meta);

        return item;
    }

    private String getRarityDisplayName(String rarity) {
        return switch (rarity) {
            case "LEGENDARY" -> "§6传说";
            case "EPIC" -> "§5史诗";
            case "VERY_RARE" -> "§d非常稀有";
            case "RARE" -> "§b稀有";
            case "UNCOMMON" -> "§a罕见";
            default -> "§f普通";
        };
    }

    private Material getMaterialForRarity(String rarity) {
        // 不再使用，但保留以防其他地方调用
        return Material.ENCHANTED_BOOK;
    }

    private String getRarityColor(String rarity) {
        return switch (rarity) {
            case "epic" -> "§6";
            case "rare" -> "§3";
            case "uncommon" -> "§2";
            default -> "§7";
        };
    }

    private void setupNavigationItems(Inventory inv, int totalSize) {
        inv.setItem(45, createNavigationItem("§a上一页", Material.ARROW, "prev"));
        inv.setItem(46, createCategoryItem("§e[全部]", "all"));
        inv.setItem(47, createCategoryItem("§c[武器]", "weapon"));
        inv.setItem(48, createCategoryItem("§9[防具]", "armor"));
        inv.setItem(49, createCategoryItem("§6[工具]", "tool"));
        inv.setItem(50, createCategoryItem("§8[诅咒]", "curse"));
        inv.setItem(51, createCategoryItem("§d[增强版]", "enhanced"));
        inv.setItem(52, createNavigationItem("§a下一页", Material.ARROW, "next"));

        int totalPages = (int) Math.ceil((double) totalSize / ITEMS_PER_PAGE);
        ItemStack pageInfo = new ItemStack(Material.PAPER);
        ItemMeta meta = pageInfo.getItemMeta();

        if (meta != null) {
            // Use modern Adventure API instead of deprecated method
            com.enadd.util.ItemMetaHelper.setDisplayName(meta, "§7第 " + (currentPage + 1) + " / " + Math.max(1, totalPages) + " 页");
            pageInfo.setItemMeta(meta);
        }

        inv.setItem(53, pageInfo);
    }

    private ItemStack createNavigationItem(String name, Material material, String action) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Use modern Adventure API instead of deprecated methods
            com.enadd.util.ItemMetaHelper.setDisplayName(meta, name);
            com.enadd.util.ItemMetaHelper.setLore(meta, Arrays.asList("§8点击" + action, "§8Action: " + action));
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createCategoryItem(String name, String category) {
        Material material = switch (category) {
            case "all" -> Material.BOOK;
            case "weapon" -> Material.DIAMOND_SWORD;
            case "armor" -> Material.DIAMOND_CHESTPLATE;
            case "tool" -> Material.DIAMOND_PICKAXE;
            case "curse" -> Material.PLAYER_HEAD;
            case "enhanced" -> Material.ENCHANTED_BOOK;
            default -> Material.BOOK;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Use modern Adventure API instead of deprecated methods
            com.enadd.util.ItemMetaHelper.setDisplayName(meta, name);
            com.enadd.util.ItemMetaHelper.setLore(meta, Arrays.asList("§8切换到 " + category, "§8Category: " + category));
            item.setItemMeta(meta);
        }

        return item;
    }

    public void handleClick(InventoryClickEvent event) {
        // ✅ 修复：全局取消所有点击，防止物品复制
        event.setCancelled(true);
        
        // ✅ 修复：检查点击类型，阻止所有特殊操作
        switch (event.getClick()) {
            case NUMBER_KEY:
                // 阻止数字键交换
                return;
            case DROP:
            case CONTROL_DROP:
                // 阻止丢弃物品
                return;
            case DOUBLE_CLICK:
                // 阻止双击收集
                return;
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
                // 阻止Shift点击移动物品
                if (event.getRawSlot() >= INVENTORY_SIZE) {
                    // 点击了玩家背包，阻止移动到GUI
                    return;
                }
                break;
            default:
                break;
        }
        
        int slot = event.getRawSlot();
        
        // ✅ 修复：检查是否点击了玩家自己的背包
        if (slot < 0 || slot >= INVENTORY_SIZE) {
            // 点击了玩家背包或无效槽位，直接返回
            return;
        }

        if (slot >= 0 && slot < currentEnchantments.size()) {
            showEnchantmentDetails(currentEnchantments.get(slot));
            return;
        }

        if (slot == 45 && event.getCurrentItem() != null) {
            if (currentPage > 0) {
                currentPage--;
                updateInventory();
            }
            return;
        }

        if (slot == 52 && event.getCurrentItem() != null) {
            List<GUIEnchantment> enchantments = categoryMap.getOrDefault(currentCategory, categoryMap.get("all"));
            int totalPages = (int) Math.ceil((double) enchantments.size() / ITEMS_PER_PAGE);
            if (currentPage < totalPages - 1) {
                currentPage++;
                updateInventory();
            }
            return;
        }

        if (slot >= 46 && slot <= 51 && event.getCurrentItem() != null) {
            String category = getCategoryFromSlot(slot);
            if (category != null) {
                currentCategory = category;
                currentPage = 0;
                updateInventory();
            }
            return;
        }
    }

    private String getCategoryFromSlot(int slot) {
        return switch (slot) {
            case 46 -> "all";
            case 47 -> "weapon";
            case 48 -> "armor";
            case 49 -> "tool";
            case 50 -> "curse";
            case 51 -> "enhanced";
            default -> null;
        };
    }

    private void showEnchantmentDetails(GUIEnchantment enchant) {
        StringBuilder message = new StringBuilder();
        message.append("§6═══════════════════════════════════════\n");
        message.append("  §e").append(enchant.getDisplayName()).append("\n");
        if (enchant.isEnhanced()) {
            message.append("  §d✨ 增强版附魔 ✨\n");
        }
        message.append("§6═══════════════════════════════════════\n\n");

        message.append("§7ID: §f").append(enchant.getId()).append("\n");
        message.append("§7类别: §f").append(getCategoryDisplayName(enchant.getCategory())).append("\n");
        message.append("§7稀有度: ").append(getRarityDisplayName(enchant.getRarity())).append("\n");
        message.append("§7最大等级: §f").append(enchant.getMaxLevel()).append("\n\n");

        message.append("§7描述:\n");
        message.append("§f").append(enchant.getDescription()).append("\n\n");

        message.append("§8═══════════════════════════════════════");

        player.sendMessage(message.toString());
    }

    public void handleClose(InventoryCloseEvent event) {
        // ✅ 移除GUI全局保护
        GUIProtectionManager protectionManager = GUIProtectionManager.getInstance();
        if (protectionManager != null) {
            protectionManager.unmarkPlayerInGUI(player);
        }
    }

    public static class GUIEnchantment {
        private final String id;
        private final String displayName;
        private final String description;
        private final String rarity;
        private final int maxLevel;
        private final String category;
        private final boolean isEnhanced;

        public GUIEnchantment(String id, String displayName, String description,
                             String rarity, int maxLevel, String category, boolean isEnhanced) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.rarity = rarity;
            this.maxLevel = maxLevel;
            this.category = category;
            this.isEnhanced = isEnhanced;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getRarity() { return rarity; }
        public int getMaxLevel() { return maxLevel; }
        public String getCategory() { return category; }
        public boolean isEnhanced() { return isEnhanced; }
    }
}
