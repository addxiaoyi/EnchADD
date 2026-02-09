package com.enadd.creative;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public final class CreativeInventoryManager implements Listener {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final int ENCHANTMENT_PAGE_SIZE = 28;
    private static final int GUI_SIZE = 54;
    private static final int[] ENCHANTMENT_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    private final JavaPlugin plugin;
    private final Map<Player, Integer> playerPage = new ConcurrentHashMap<>();
    private final Map<Player, List<CreativeEnchantmentItem>> playerEnchantments = new ConcurrentHashMap<>();
    private final Map<Player, String> playerSearchQuery = new ConcurrentHashMap<>();
    private final Map<Player, Set<String>> playerActiveFilters = new ConcurrentHashMap<>();

    private static final Map<String, String> CATEGORY_NAMES = Map.of(
        "weapon", "⚔️ 武器",
        "armor", "🛡️ 护甲",
        "tool", "⛏️ 工具",
        "bow", "🏹 弓",
        "crossbow", "§5十字弓",
        "fishing_rod", "🎣 钓鱼竿",
        "trident", "🔱 三叉戟",
        "universal", "✨ 通用"
    );

    private static final String[] CATEGORY_KEYS = {
        "weapon", "armor", "tool", "bow", "crossbow", "fishing_rod", "trident", "universal"
    };

    public CreativeInventoryManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @SuppressWarnings("deprecation")
    public void openCreativeEnchantmentGui(Player player) {
        try {
            List<CreativeEnchantmentItem> filteredEnchantments = getFilteredEnchantments();

            String title = "§5§l附魔目录 §7| §fEnchAdd";
            Inventory gui = Bukkit.createInventory(null, 54, title);

            playerPage.put(player, 0);
            playerEnchantments.put(player, filteredEnchantments);

            player.openInventory(gui);

            refreshEnchantmentGui(gui, player, 0);

        } catch (Exception e) {
            player.sendMessage(format("&c打开附魔目录失败: " + e.getMessage()));
            plugin.getLogger().warning("打开创造模式附魔GUI失败: " + e.getMessage());
        }
    }

    private void refreshEnchantmentGui(Inventory gui, Player player, int page) {
        try {
            for (int slot = 0; slot < 54; slot++) {
                gui.setItem(slot, new ItemStack(Material.AIR));
            }

            List<CreativeEnchantmentItem> enchantments = playerEnchantments.getOrDefault(player, new ArrayList<>());
            String searchQuery = playerSearchQuery.getOrDefault(player, "");
            Set<String> activeFilters = playerActiveFilters.getOrDefault(player, new HashSet<>());

            if (!searchQuery.isEmpty()) {
                enchantments = filterBySearch(enchantments, searchQuery);
            }

            if (!activeFilters.isEmpty()) {
                enchantments = enchantments.stream()
                    .filter(item -> activeFilters.contains(item.getCategory()))
                    .collect(Collectors.toList());
            }

            int totalPages = (int) Math.ceil((double) enchantments.size() / ENCHANTMENT_PAGE_SIZE);
            if (totalPages == 0) totalPages = 1;
            if (page >= totalPages) page = totalPages - 1;
            if (page < 0) page = 0;
            playerPage.put(player, page);

            int startIndex = page * ENCHANTMENT_PAGE_SIZE;
            int endIndex = Math.min(startIndex + ENCHANTMENT_PAGE_SIZE, enchantments.size());

            for (int i = startIndex; i < endIndex; i++) {
                int slotIndex = i - startIndex;
                if (slotIndex < ENCHANTMENT_SLOTS.length) {
                    ItemStack item = createEnchantmentItem(enchantments.get(i));
                    gui.setItem(ENCHANTMENT_SLOTS[slotIndex], item);
                }
            }

            gui.setItem(45, createNavigationItem(Material.ARROW, "§7← 上一页", page > 0));
            gui.setItem(49, createInfoItem(page + 1, totalPages, enchantments.size()));
            gui.setItem(53, createNavigationItem(Material.ARROW, "§7下一页 →", page < totalPages - 1));

            int col = 0;
            for (String category : CATEGORY_KEYS) {
                if (col < 8) {
                    int slot = 3 + col;
                    boolean isActive = activeFilters.isEmpty() || activeFilters.contains(category);
                    ItemStack btn = createFilterButton(category, isActive);
                    gui.setItem(slot, btn);
                }
                col++;
            }

        } catch (Exception e) {
            plugin.getLogger().warning("刷新附魔GUI失败: " + e.getMessage());
        }
    }

    private ItemStack createNavigationItem(Material material, String name, boolean enabled) {
        ItemStack item = new ItemStack(enabled ? material : Material.GRAY_DYE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LEGACY_SERIALIZER.deserialize(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem(int currentPage, int totalPages, int totalItems) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LEGACY_SERIALIZER.deserialize("§f第 " + currentPage + " 页 / 共 " + totalPages + " 页"));

            List<String> lore = new ArrayList<>();
            lore.add("§7总附魔数: §f" + totalItems);
            lore.add("§7每页显示: §f" + ENCHANTMENT_PAGE_SIZE);

            int startItem = (currentPage - 1) * ENCHANTMENT_PAGE_SIZE + 1;
            int endItem = Math.min(currentPage * ENCHANTMENT_PAGE_SIZE, totalItems);
            if (totalItems > 0) {
                lore.add("§7当前显示: §f" + startItem + " - " + endItem);
            }

            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(LEGACY_SERIALIZER.deserialize(line).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(loreComponents);

            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createFilterButton(String category, boolean isActive) {
        ItemStack item = new ItemStack(isActive ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = CATEGORY_NAMES.getOrDefault(category, category);
            meta.displayName(LEGACY_SERIALIZER.deserialize("§a" + displayName));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory topInv = event.getView().getTopInventory();
        if (topInv == null || topInv.getSize() != GUI_SIZE) return;

        Component title = event.getView().title();
        if (title == null || (!title.toString().contains("附魔目录") && !title.toString().contains("EnchAdd"))) return;

        int slot = event.getRawSlot();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (slot == 45) {
            int currentPage = playerPage.getOrDefault(player, 0);
            if (currentPage > 0) {
                refreshEnchantmentGui(event.getView().getTopInventory(), player, currentPage - 1);
            }
            event.setCancelled(true);
            return;
        }

        if (slot == 53) {
            int currentPage = playerPage.getOrDefault(player, 0);
            List<CreativeEnchantmentItem> enchantments = playerEnchantments.getOrDefault(player, new ArrayList<>());
            int totalPages = (int) Math.ceil((double) enchantments.size() / ENCHANTMENT_PAGE_SIZE);
            if (currentPage < totalPages - 1) {
                refreshEnchantmentGui(event.getView().getTopInventory(), player, currentPage + 1);
            }
            event.setCancelled(true);
            return;
        }

        if (slot >= 3 && slot <= 10) {
            int categoryIndex = slot - 3;
            if (categoryIndex < CATEGORY_KEYS.length) {
                String category = CATEGORY_KEYS[categoryIndex];
                Set<String> filters = playerActiveFilters.getOrDefault(player, new HashSet<>());

                if (filters.contains(category)) {
                    filters.remove(category);
                } else {
                    filters.add(category);
                }

                if (filters.isEmpty()) {
                    playerActiveFilters.remove(player);
                } else {
                    playerActiveFilters.put(player, filters);
                }

                refreshEnchantmentGui(event.getView().getTopInventory(), player, 0);
            }
            event.setCancelled(true);
            return;
        }

        if (Arrays.stream(ENCHANTMENT_SLOTS).anyMatch(s -> s == slot)) {
            if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                List<Component> loreComponents = clicked.getItemMeta().lore();
                if (loreComponents != null) {
                    for (Component component : loreComponents) {
                        String line = LEGACY_SERIALIZER.serialize(component);
                        if (line != null && line.contains("enchadd:")) {
                            event.setCancelled(true);
                            giveEnchantedBook(player, clicked);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void giveEnchantedBook(Player player, ItemStack clicked) {
        try {
            String enchantId = "";
            int level = 1;

            if (clicked.hasItemMeta() && clicked.getItemMeta().hasLore()) {
                List<Component> loreComponents = clicked.getItemMeta().lore();
                if (loreComponents != null) {
                    for (Component component : loreComponents) {
                        String line = LEGACY_SERIALIZER.serialize(component);
                        if (line != null && line.contains("enchadd:")) {
                            enchantId = line.replace("enchadd:", "").trim();
                            break;
                        }
                    }

                    for (Component component : loreComponents) {
                        String line = LEGACY_SERIALIZER.serialize(component);
                        if (line != null && line.contains("等级")) {
                            try {
                                String levelStr = line.replaceAll("[^0-9]", "");
                                if (!levelStr.isEmpty()) {
                                    level = Integer.parseInt(levelStr);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }

            if (!enchantId.isEmpty()) {
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                ItemMeta meta = book.getItemMeta();
                if (meta != null) {
                    String displayName = getEnchantmentDisplayName(enchantId);
                    meta.displayName(LEGACY_SERIALIZER.deserialize("§5§l" + displayName));

                    List<Component> loreComponents = new ArrayList<>();
                    loreComponents.add(LEGACY_SERIALIZER.deserialize("§7附魔等级: " + level).decoration(TextDecoration.ITALIC, false));
                    loreComponents.add(LEGACY_SERIALIZER.deserialize("§7来源: EnchAdd 创造模式").decoration(TextDecoration.ITALIC, false));
                    meta.lore(loreComponents);

                    book.setItemMeta(meta);
                }

                player.getInventory().addItem(book);
                player.sendMessage(format("&a你获得了附魔书: &f" + getEnchantmentDisplayName(enchantId)));
            }

        } catch (Exception e) {
            player.sendMessage(format("&c获取附魔失败: " + e.getMessage()));
            plugin.getLogger().warning("给予附魔书失败: " + e.getMessage());
        }
    }

    private ItemStack createEnchantmentItem(CreativeEnchantmentItem item) {
        Material baseMaterial = getBaseMaterialForCategory(item.getCategory());

        ItemStack stack = new ItemStack(baseMaterial);
        ItemMeta meta = stack.getItemMeta();

        if (meta != null) {
            String rarityColor = item.getRarityColor();
            meta.displayName(LEGACY_SERIALIZER.deserialize(rarityColor + "§l" + item.getDisplayName() + " §7(" + item.getMaxLevel() + ")"));

            List<String> lore = new ArrayList<>();
            lore.add("§7" + item.getDescription());
            lore.add("");
            lore.add("§8enchadd:" + item.getId());
            lore.add("§8分类: " + item.getCategory());
            lore.add("");
            lore.add("§7最大等级: §e" + item.getMaxLevel());
            lore.add("§7权重: §e" + item.getWeight());
            lore.add("§7类型: " + getCategoryDisplayName(item.getCategory()));
            lore.add("");
            lore.add("§8点击获取附魔书");

            List<Component> loreComponents = new ArrayList<>();
            for (String line : lore) {
                loreComponents.add(LEGACY_SERIALIZER.deserialize(line).decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(loreComponents);

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DYE);
            stack.setItemMeta(meta);
        }

        return stack;
    }

    private Material getBaseMaterialForCategory(String category) {
        return switch (category) {
            case "weapon" -> Material.DIAMOND_SWORD;
            case "armor" -> Material.DIAMOND_CHESTPLATE;
            case "tool" -> Material.DIAMOND_PICKAXE;
            case "bow" -> Material.BOW;
            case "crossbow" -> Material.CROSSBOW;
            case "fishing_rod" -> Material.FISHING_ROD;
            case "trident" -> Material.TRIDENT;
            default -> Material.ENCHANTED_BOOK;
        };
    }

    private String getCategoryDisplayName(String category) {
        return CATEGORY_NAMES.getOrDefault(category, "§f" + category);
    }

    private List<CreativeEnchantmentItem> getFilteredEnchantments() {
        List<CreativeEnchantmentItem> items = new ArrayList<>();

        items.addAll(generateWeaponEnchantments());
        items.addAll(generateArmorEnchantments());
        items.addAll(generateToolEnchantments());
        items.addAll(generateBowEnchantments());
        items.addAll(generateCrossbowEnchantments());
        items.addAll(generateTridentEnchantments());
        items.addAll(generateFishingRodEnchantments());

        items.sort(Comparator.comparingInt(CreativeEnchantmentItem::getSortPriority));

        return items;
    }

    private List<CreativeEnchantmentItem> generateWeaponEnchantments() {
        List<CreativeEnchantmentItem> items = new ArrayList<>();

        items.add(new CreativeEnchantmentItem("sharpness", "锋利", "大幅增加武器的攻击伤害", "weapon", 5, 10, "§c", 1));
        items.add(new CreativeEnchantmentItem("smite", "亡灵杀手", "对准亡灵生物造成额外神圣伤害", "weapon", 5, 8, "§c", 2));
        items.add(new CreativeEnchantmentItem("bane_of_arthropods", "节肢杀手", "对准节肢生物造成额外伤害", "weapon", 5, 8, "§c", 3));
        items.add(new CreativeEnchantmentItem("knockback", "击退", "攻击时将目标击退", "weapon", 2, 10, "§c", 4));
        items.add(new CreativeEnchantmentItem("fire_aspect", "火焰附加", "使武器带有火焰伤害", "weapon", 2, 5, "§c", 5));
        items.add(new CreativeEnchantmentItem("looting", "抢夺", "击杀生物时增加掉落物", "weapon", 3, 3, "§c", 6));
        items.add(new CreativeEnchantmentItem("sweeping", "横扫之刃", "增加横扫攻击伤害", "weapon", 3, 5, "§c", 7));

        return items;
    }

    private List<CreativeEnchantmentItem> generateArmorEnchantments() {
        List<CreativeEnchantmentItem> items = new ArrayList<>();

        items.add(new CreativeEnchantmentItem("protection", "保护", "减少所有类型的伤害", "armor", 4, 10, "§9", 1));
        items.add(new CreativeEnchantmentItem("fire_protection", "火焰保护", "专门减少火焰伤害", "armor", 4, 8, "§9", 2));
        items.add(new CreativeEnchantmentItem("blast_protection", "爆炸保护", "减少爆炸造成的伤害", "armor", 4, 5, "§9", 3));
        items.add(new CreativeEnchantmentItem("projectile_protection", "弹射物保护", "减少弹射物伤害", "armor", 4, 8, "§9", 4));
        items.add(new CreativeEnchantmentItem("feather_falling", "摔落保护", "显著减少掉落伤害", "armor", 4, 10, "§9", 5));
        items.add(new CreativeEnchantmentItem("thorns", "荆棘", "反弹伤害给攻击者", "armor", 3, 3, "§9", 6));
        items.add(new CreativeEnchantmentItem("depth_strider", "深度行走", "提高水下行走速度", "armor", 3, 8, "§9", 7));
        items.add(new CreativeEnchantmentItem("soul_speed", "灵魂速度", "在灵魂沙上移动更快", "armor", 3, 3, "§9", 8));
        items.add(new CreativeEnchantmentItem("swift_sneak", "快速潜行", "潜行时移动更快", "armor", 3, 2, "§9", 9));

        return items;
    }

    private List<CreativeEnchantmentItem> generateToolEnchantments() {
        List<CreativeEnchantmentItem> items = new ArrayList<>();

        items.add(new CreativeEnchantmentItem("efficiency", "效率", "大幅提升采集速度", "tool", 5, 15, "§e", 1));
        items.add(new CreativeEnchantmentItem("silk_touch", "精准采集", "使方块完整掉落", "tool", 1, 2, "§e", 2));
        items.add(new CreativeEnchantmentItem("fortune", "时运", "增加掉落概率和数量", "tool", 3, 1, "§e", 3));
        items.add(new CreativeEnchantmentItem("unbreaking", "耐久", "减少耐久损耗", "tool", 3, 15, "§e", 4));

        return items;
    }

    private List<CreativeEnchantmentItem> generateBowEnchantments() {
        List<CreativeEnchantmentItem> items = new ArrayList<>();

        items.add(new CreativeEnchantmentItem("power", "力量", "大幅增加弓的伤害", "bow", 5, 10, "§a", 1));
        items.add(new CreativeEnchantmentItem("punch", "冲击", "击退弓箭命中的目标", "bow", 2, 8, "§a", 2));
        items.add(new CreativeEnchantmentItem("flame", "火矢", "使箭矢带有火焰", "bow", 2, 5, "§a", 3));
        items.add(new CreativeEnchantmentItem("infinity", "无限", "无限使用箭矢", "bow", 1, 2, "§a", 4));

        return items;
    }

    private List<CreativeEnchantmentItem> generateCrossbowEnchantments() {
        List<CreativeEnchantmentItem> items = new ArrayList<>();

        items.add(new CreativeEnchantmentItem("multishot", "多重射击", "一次发射多支箭", "crossbow", 1, 2, "§5", 1));
        items.add(new CreativeEnchantmentItem("piercing", "穿透", "使箭矢穿透多个目标", "crossbow", 4, 4, "§5", 2));
        items.add(new CreativeEnchantmentItem("quick_charge", "快速装填", "缩短装填时间", "crossbow", 3, 5, "§5", 3));

        return items;
    }

    private List<CreativeEnchantmentItem> generateTridentEnchantments() {
        List<CreativeEnchantmentItem> items = new ArrayList<>();

        items.add(new CreativeEnchantmentItem("loyalty", "忠诚", "三叉戟自动飞回", "trident", 3, 4, "§b", 1));
        items.add(new CreativeEnchantmentItem("impaling", "穿刺", "对水生生物造成额外伤害", "trident", 5, 3, "§b", 2));
        items.add(new CreativeEnchantmentItem("riptide", "激流", "将玩家向前推动", "trident", 3, 3, "§b", 3));
        items.add(new CreativeEnchantmentItem("channeling", "引雷", "召唤闪电击中目标", "trident", 1, 2, "§b", 4));

        return items;
    }

    private List<CreativeEnchantmentItem> generateFishingRodEnchantments() {
        List<CreativeEnchantmentItem> items = new ArrayList<>();

        items.add(new CreativeEnchantmentItem("lure", "诱饵", "减少等待时间", "fishing_rod", 3, 6, "§3", 1));
        items.add(new CreativeEnchantmentItem("luck_of_the_sea", "海洋幸运", "增加获得宝藏几率", "fishing_rod", 3, 3, "§3", 2));
        items.add(new CreativeEnchantmentItem("line", "钓线", "防止钓线断裂", "fishing_rod", 1, 5, "§3", 3));

        return items;
    }

    private List<CreativeEnchantmentItem> filterBySearch(List<CreativeEnchantmentItem> enchantments, String query) {
        String lowerQuery = query.toLowerCase();
        return enchantments.stream()
            .filter(item ->
                item.getId().toLowerCase().contains(lowerQuery) ||
                item.getDisplayName().toLowerCase().contains(lowerQuery) ||
                item.getDescription().toLowerCase().contains(lowerQuery)
            )
            .collect(Collectors.toList());
    }

    private String getEnchantmentDisplayName(String key) {
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("sharpness", "锋利");
        nameMap.put("smite", "亡灵杀手");
        nameMap.put("bane_of_arthropods", "节肢杀手");
        nameMap.put("knockback", "击退");
        nameMap.put("fire_aspect", "火焰附加");
        nameMap.put("looting", "抢夺");
        nameMap.put("sweeping", "横扫之刃");
        nameMap.put("protection", "保护");
        nameMap.put("fire_protection", "火焰保护");
        nameMap.put("blast_protection", "爆炸保护");
        nameMap.put("projectile_protection", "弹射物保护");
        nameMap.put("feather_falling", "摔落保护");
        nameMap.put("thorns", "荆棘");
        nameMap.put("depth_strider", "深度行走");
        nameMap.put("soul_speed", "灵魂速度");
        nameMap.put("swift_sneak", "快速潜行");
        nameMap.put("efficiency", "效率");
        nameMap.put("silk_touch", "精准采集");
        nameMap.put("fortune", "时运");
        nameMap.put("unbreaking", "耐久");
        nameMap.put("power", "力量");
        nameMap.put("punch", "冲击");
        nameMap.put("flame", "火矢");
        nameMap.put("infinity", "无限");
        nameMap.put("multishot", "多重射击");
        nameMap.put("piercing", "穿透");
        nameMap.put("quick_charge", "快速装填");
        nameMap.put("loyalty", "忠诚");
        nameMap.put("impaling", "穿刺");
        nameMap.put("riptide", "激流");
        nameMap.put("channeling", "引雷");
        nameMap.put("lure", "诱饵");
        nameMap.put("luck_of_the_sea", "海洋幸运");
        nameMap.put("line", "钓线");

        return nameMap.getOrDefault(key.toLowerCase(), capitalizeWords(key.replace("_", " ")));
    }

    private String capitalizeWords(String text) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private String format(String text) {
        if (text == null) return "";
        return com.enadd.util.ColorFormatter.format(text);
    }

    public static class CreativeEnchantmentItem {
        private final String id;
        private final String displayName;
        private final String description;
        private final String category;
        private final int maxLevel;
        private final int weight;
        private final String rarityColor;
        private final int sortPriority;

        public CreativeEnchantmentItem(String id, String displayName, String description,
                                       String category, int maxLevel, int weight,
                                       String rarityColor, int sortPriority) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.category = category;
            this.maxLevel = maxLevel;
            this.weight = weight;
            this.rarityColor = rarityColor;
            this.sortPriority = sortPriority;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public int getMaxLevel() { return maxLevel; }
        public int getWeight() { return weight; }
        public String getRarityColor() { return rarityColor; }
        public int getSortPriority() { return sortPriority; }
    }
}
