package com.enadd.gui;

import com.enadd.core.conflict.EnchantmentConflictManager;
import com.enadd.gui.search.SearchInputHandler;
import com.enadd.gui.simulation.EnchantmentSimulator;
import com.enadd.util.ColorFormatter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.enadd.enchantments.BaseEnchantment;
import com.enadd.enchantments.Rarity;
import java.util.stream.Collectors;
import com.enadd.core.cache.CacheManager;
import com.enadd.core.cache.CacheManager.Cache;


@SuppressWarnings({"unused", "deprecation", "removal"})
public final class EnchantmentGuiManager implements Listener {

    private static final int GUI_ROWS = 6;
    private static final int GUI_SIZE = GUI_ROWS * 9;
    private static final int PAGE_SIZE = 28;
    private static final int[] SLOT_ARRAY = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    private static final int[] BORDER_SLOTS = {
        0, 1, 2, 3, 4, 5, 6, 7, 8,
        9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53
    };
    private static final String[] CATEGORIES = {"武器", "护甲", "工具", "弓", "三叉戟", "十字弓", "钓鱼竿", "通用"};

    private final Map<Player, Inventory> openGuis = new ConcurrentHashMap<>();
    private final Map<Player, Integer> playerPage = new ConcurrentHashMap<>();
    private final Map<Player, List<EnchantmentData>> playerFilteredEnchantments = new ConcurrentHashMap<>();
    private final Map<Player, String> playerSearchQuery = new ConcurrentHashMap<>();
    private final Map<Player, Set<String>> playerActiveFilters = new ConcurrentHashMap<>();
    private final Map<Player, ItemStack> cachedBorderItems = new ConcurrentHashMap<>();

    // 高性能缓存系统
    private final Cache<String, ItemStack> enchantmentItemCache;
    private final Cache<String, ItemStack> buttonCache;
    private final Cache<Material, ItemStack> borderItemCache;

    private final JavaPlugin plugin;
    private final GuiConfig config;
    private final EnchantmentSimulator simulator;

    public EnchantmentGuiManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = new GuiConfig(plugin);
        this.simulator = new EnchantmentSimulator(plugin, this);

        // 初始化缓存系统 - 5分钟TTL，减少重复渲染
        CacheManager cacheManager = CacheManager.getInstance();
        this.enchantmentItemCache = cacheManager.getCache("gui-enchantment-items", 500, 300000L);
        this.buttonCache = cacheManager.getCache("gui-buttons", 100, 300000L);
        this.borderItemCache = cacheManager.getCache("gui-border-items", 10, 600000L);

        registerCommands();
        registerEvents();
    }

    private void registerCommands() {
        try {
            org.bukkit.command.PluginCommand command = plugin.getCommand("enchatadd");
            if (command == null) {
                plugin.getLogger().warning("命令 'enchatadd' 未在plugin.yml中定义！");
                return;
            }

            command.setExecutor((sender, cmd, label, args) -> {
                try {
                    if (!(sender instanceof Player)) {
                        if (sender != null) {
                            sender.sendMessage(config.getMessage("command-only-player"));
                        }
                        return true;
                    }

                    Player player = (Player) sender;
                    if (player == null) {
                        return true;
                    }

                    if (!player.hasPermission("enchatadd.gui")) {
                        player.sendMessage(config.getMessage("no-permission"));
                        return true;
                    }

                    openMainGui(player);
                    return true;
                } catch (Exception e) {
                    plugin.getLogger().warning("执行命令时出错: " + e.getMessage());
                    if (sender != null) {
                        sender.sendMessage("§c命令执行失败，请查看控制台");
                    }
                    return true;
                }
            });

            command.setTabCompleter((sender, cmd, alias, args) -> {
                try {
                    if (args == null || args.length == 1) {
                        return Arrays.asList("gui", "reload", "help", "search");
                    }
                    return Collections.emptyList();
                } catch (Exception e) {
                    return Collections.emptyList();
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("注册命令时出错: " + e.getMessage());
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainGui(Player player) {
        if (player == null) {
            plugin.getLogger().warning("尝试为null玩家打开GUI");
            return;
        }

        if (!player.isOnline()) {
            plugin.getLogger().warning("尝试为离线玩家打开GUI: " + player.getName());
            return;
        }

        try {
            long startTime = System.currentTimeMillis();

            // Use modern Adventure API for inventory title
            String titleText = config.getGuiTitle();
            if (titleText == null || titleText.trim().isEmpty()) {
                titleText = "§6附魔预览";
            }

            net.kyori.adventure.text.Component title = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection().deserialize(format(titleText));

            Inventory gui = Bukkit.createInventory(null, GUI_SIZE, title);
            if (gui == null) {
                plugin.getLogger().severe("无法创建GUI inventory");
                player.sendMessage("§c无法打开GUI，请联系管理员");
                return;
            }

            initializeGui(gui, player, 0);
            player.openInventory(gui);
            openGuis.put(player, gui);
            playerPage.put(player, 0);

            List<EnchantmentData> enchantments = getAllEnchantments(player);
            if (enchantments == null) {
                enchantments = new ArrayList<>();
            }
            playerFilteredEnchantments.put(player, enchantments);
            playerSearchQuery.put(player, "");
            playerActiveFilters.put(player, new HashSet<>());
            
            // ✅ 启用GUI全局保护
            GUIProtectionManager protectionManager = GUIProtectionManager.getInstance();
            if (protectionManager != null) {
                protectionManager.markPlayerInGUI(player);
            }

            long loadTime = System.currentTimeMillis() - startTime;
            if (loadTime > 200) {
                plugin.getLogger().warning("GUI加载超时: " + loadTime + "ms for " + player.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("打开GUI时出错: " + e.getMessage());
            e.printStackTrace();
            if (player.isOnline()) {
                player.sendMessage("§c打开GUI时出错，请稍后重试");
            }
        }
    }
    private void initializeGui(Inventory gui, Player player, int page) {
        if (gui == null) {
            plugin.getLogger().warning("initializeGui: gui为null");
            return;
        }

        if (player == null) {
            plugin.getLogger().warning("initializeGui: player为null");
            return;
        }

        try {
            clearGui(gui);

            List<EnchantmentData> enchantments = playerFilteredEnchantments.getOrDefault(player, getAllEnchantments(player));
            if (enchantments == null) {
                enchantments = new ArrayList<>();
                plugin.getLogger().warning("getAllEnchantments返回null，使用空列表");
            }

            String searchQuery = playerSearchQuery.getOrDefault(player, "");
            if (searchQuery == null) {
                searchQuery = "";
            }

            if (!searchQuery.isEmpty()) {
                List<EnchantmentData> filtered = filterBySearch(enchantments, searchQuery);
                if (filtered != null) {
                    enchantments = filtered;
                } else {
                    plugin.getLogger().warning("filterBySearch返回null");
                }
            }

            Set<String> activeFilters = playerActiveFilters.getOrDefault(player, new HashSet<>());
            if (activeFilters == null) {
                activeFilters = new HashSet<>();
            }

            if (!activeFilters.isEmpty()) {
                List<EnchantmentData> filtered = filterByCategories(enchantments, activeFilters);
                if (filtered != null) {
                    enchantments = filtered;
                } else {
                    plugin.getLogger().warning("filterByCategories返回null");
                }
            }

            int totalPages = enchantments.size() > 0 ? (int) Math.ceil((double) enchantments.size() / PAGE_SIZE) : 1;
            int currentPage = Math.min(page, totalPages - 1);
            currentPage = Math.max(0, currentPage);

            playerPage.put(player, currentPage);
            playerFilteredEnchantments.put(player, enchantments);

            setBorder(gui, player);
            setNavigationButtons(gui, player, currentPage, totalPages);
            setFilterButtons(gui, player);
            setSearchButton(gui, player);
            setCategoryFilter(gui, player);
            setInfoPanel(gui, player, enchantments.size(), totalPages);

            int startIndex = currentPage * PAGE_SIZE;
            int endIndex = Math.min(startIndex + PAGE_SIZE, enchantments.size());

            for (int i = startIndex; i < endIndex; i++) {
                if (i < 0 || i >= enchantments.size()) {
                    continue;
                }

                EnchantmentData data = enchantments.get(i);
                if (data == null) {
                    plugin.getLogger().warning("附魔数据为null，索引: " + i);
                    continue;
                }

                int slot = calculateSlot(i - startIndex);
                if (slot >= 0 && slot < gui.getSize()) {
                    ItemStack item = createEnchantmentItem(player, data);
                    if (item != null) {
                        gui.setItem(slot, item);
                    } else {
                        plugin.getLogger().warning("createEnchantmentItem returned null for enchantment at index: " + i);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("初始化GUI时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int calculateSlot(int index) {
        return index < SLOT_ARRAY.length ? SLOT_ARRAY[index] : -1;
    }

    private void clearGui(Inventory gui) {
        if (gui == null) {
            return;
        }

        try {
            ItemStack emptyItem = createEmptyItem();
            if (emptyItem == null) {
                emptyItem = new ItemStack(Material.AIR);
            }

            for (int slot : BORDER_SLOTS) {
                if (slot >= 0 && slot < gui.getSize()) {
                    gui.setItem(slot, emptyItem);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("清理GUI时出错: " + e.getMessage());
        }
    }

    private void setBorder(Inventory gui, Player player) {
        ItemStack borderItem = borderItemCache.getOrCompute(Material.BLACK_STAINED_GLASS_PANE, material -> {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                com.enadd.util.ItemMetaHelper.setDisplayName(meta, "§8 ");
                item.setItemMeta(meta);
            }
            return item;
        });

        for (int slot : BORDER_SLOTS) {
            gui.setItem(slot, borderItem);
        }
    }

    private ItemStack getOrCreateBorderItem(Player player) {
        return cachedBorderItems.computeIfAbsent(player, p -> {
            ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                com.enadd.util.ItemMetaHelper.setDisplayName(meta, "§8 ");
                item.setItemMeta(meta);
            }
            return item;
        });
    }

    private void setNavigationButtons(Inventory gui, Player player, int currentPage, int totalPages) {
        // Bug #111: 检查参数
        if (gui == null || player == null) {
            plugin.getLogger().warning("setNavigationButtons: gui或player为null");
            return;
        }

        // Bug #112: 验证页码范围
        if (currentPage < 0) currentPage = 0;
        if (totalPages < 1) totalPages = 1;
        if (currentPage >= totalPages) currentPage = totalPages - 1;

        try {
            boolean hasPrevious = currentPage > 0;
            boolean hasNext = currentPage < totalPages - 1;

            // Bug #113: 安全地获取配置文本
            String prevButtonText = config.getPreviousPageButton();
            if (prevButtonText == null || prevButtonText.isEmpty()) {
                prevButtonText = "&7上一页";
            }

            String nextButtonText = config.getNextPageButton();
            if (nextButtonText == null || nextButtonText.isEmpty()) {
                nextButtonText = "&7下一页";
            }

            ItemStack prevButton = createButton(
                hasPrevious ? Material.ARROW : Material.GRAY_DYE,
                format(prevButtonText),
                hasPrevious ? Arrays.asList(
                    format("&7页码: &e" + (currentPage + 1) + " &7/ &e" + totalPages),
                    "",
                    format("&7点击前往 &e上一页")
                ) : Arrays.asList(format("&c已在第一页"))
            );

            ItemStack nextButton = createButton(
                hasNext ? Material.ARROW : Material.GRAY_DYE,
                format(nextButtonText),
                hasNext ? Arrays.asList(
                    format("&7页码: &e" + (currentPage + 1) + " &7/ &e" + totalPages),
                    "",
                    format("&7点击前往 &e下一页")
                ) : Arrays.asList(format("&c已在最后一页"))
            );

            // Bug #114: 检查slot范围
            if (45 < gui.getSize()) gui.setItem(45, prevButton);
            if (49 < gui.getSize()) gui.setItem(49, createPageIndicator(currentPage, totalPages));
            if (53 < gui.getSize()) gui.setItem(53, nextButton);
        } catch (Exception e) {
            plugin.getLogger().warning("设置导航按钮时出错: " + e.getMessage());
        }
    }

    private void setFilterButtons(Inventory gui, Player player) {
        // Bug #115: 检查参数
        if (gui == null || player == null) {
            plugin.getLogger().warning("setFilterButtons: gui或player为null");
            return;
        }

        try {
            // Bug #116: 安全地获取配置文本
            String clearFiltersText = config.getClearFiltersButton();
            if (clearFiltersText == null || clearFiltersText.isEmpty()) {
                clearFiltersText = "&c清除筛选";
            }

            String rarityFilterText = config.getRarityFilterButton();
            if (rarityFilterText == null || rarityFilterText.isEmpty()) {
                rarityFilterText = "&e稀有度筛选";
            }

            String sortButtonText = config.getSortButton();
            if (sortButtonText == null || sortButtonText.isEmpty()) {
                sortButtonText = "&6排序";
            }

            ItemStack clearFilters = createButton(
                Material.BUCKET,
                format(clearFiltersText),
                Arrays.asList(
                    format("&7清除所有筛选条件"),
                    "",
                    format("&e点击清除")
                )
            );

            ItemStack rarityFilter = createButton(
                Material.BOOK,
                format(rarityFilterText),
                Arrays.asList(
                    format("&7按稀有度筛选"),
                    "",
                    format("&e点击打开筛选菜单")
                )
            );

            String sortName = getCurrentSortName(player);
            if (sortName == null) sortName = "默认";

            ItemStack sortButton = createButton(
                Material.HOPPER,
                format(sortButtonText),
                Arrays.asList(
                    format("&7排序方式: &e" + sortName),
                    "",
                    format("&e点击切换排序")
                )
            );

            // Bug #117: 检查slot范围
            if (46 < gui.getSize()) gui.setItem(46, clearFilters);
            if (47 < gui.getSize()) gui.setItem(47, rarityFilter);
            if (48 < gui.getSize()) gui.setItem(48, sortButton);
        } catch (Exception e) {
            plugin.getLogger().warning("设置筛选按钮时出错: " + e.getMessage());
        }
    }

    private void setSearchButton(Inventory gui, Player player) {
        // Bug #118: 检查参数
        if (gui == null || player == null) {
            plugin.getLogger().warning("setSearchButton: gui或player为null");
            return;
        }

        try {
            String query = playerSearchQuery.getOrDefault(player, "");
            if (query == null) query = "";

            boolean hasSearch = !query.isEmpty();

            Material material = hasSearch ? Material.COMPASS : Material.NAME_TAG;

            // Bug #119: 安全地获取配置文本
            String searchButtonText = config.getSearchButton();
            if (searchButtonText == null || searchButtonText.isEmpty()) {
                searchButtonText = "&e搜索";
            }

            String displayName = hasSearch
                ? format("&6搜索: &f" + query)
                : format(searchButtonText);

            List<String> lore = hasSearch
                ? Arrays.asList(
                    format("&7当前搜索: &e" + query),
                    "",
                    format("&7点击 &e修改搜索"),
                    format("&7右键 &c清除搜索")
                )
                : Arrays.asList(
                    format("&7输入关键词搜索附魔"),
                    "",
                    format("&e点击输入搜索内容")
                );

            ItemStack searchButton = createButton(material, displayName, lore);

            // Bug #120: 检查slot范围
            if (4 < gui.getSize() && searchButton != null) {
                gui.setItem(4, searchButton);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("设置搜索按钮时出错: " + e.getMessage());
        }
    }

    private void setCategoryFilter(Inventory gui, Player player) {
        // Bug #121: 检查参数
        if (gui == null || player == null) {
            plugin.getLogger().warning("setCategoryFilter: gui或player为null");
            return;
        }

        try {
            Set<String> activeFilters = playerActiveFilters.getOrDefault(player, new HashSet<>());
            if (activeFilters == null) {
                activeFilters = new HashSet<>();
            }

            // Bug #122: 检查CATEGORIES数组
            if (CATEGORIES == null || CATEGORIES.length == 0) {
                plugin.getLogger().warning("CATEGORIES数组为null或空");
                return;
            }

            for (int i = 0; i < CATEGORIES.length && i < 7; i++) {
                String category = CATEGORIES[i];
                // Bug #123: 检查category是否为null
                if (category == null || category.isEmpty()) {
                    continue;
                }

                boolean isActive = activeFilters.contains(category);

                Material material = getCategoryMaterial(category);
                if (material == null) {
                    material = Material.BOOK;
                }

                ItemStack categoryButton = createButton(
                    material,
                    format(isActive ? "&a" + category : "&7" + category),
                    Arrays.asList(
                        format(isActive ? "&a已激活" : "&7点击筛选"),
                        "",
                        format("&e点击切换")
                    )
                );

                // Bug #124: 检查slot范围
                if (i < gui.getSize() && categoryButton != null) {
                    gui.setItem(i, categoryButton);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("设置分类筛选时出错: " + e.getMessage());
        }
    }

    private Material getCategoryMaterial(String category) {
        // Bug #125: 检查参数
        if (category == null || category.isEmpty()) {
            return Material.ENCHANTED_BOOK;
        }

        try {
            return switch (category) {
                case "武器" -> Material.DIAMOND_SWORD;
                case "护甲" -> Material.DIAMOND_CHESTPLATE;
                case "工具" -> Material.DIAMOND_PICKAXE;
                case "弓" -> Material.BOW;
                case "三叉戟" -> Material.TRIDENT;
                case "十字弓" -> Material.CROSSBOW;
                case "钓鱼竿" -> Material.FISHING_ROD;
                default -> Material.ENCHANTED_BOOK;
            };
        } catch (Exception e) {
            plugin.getLogger().warning("getCategoryMaterial出错: " + e.getMessage());
            return Material.ENCHANTED_BOOK;
        }
    }

    private void setInfoPanel(Inventory gui, Player player, int totalEnchantments, int totalPages) {
        // Bug #126: 检查参数
        if (gui == null || player == null) {
            plugin.getLogger().warning("setInfoPanel: gui或player为null");
            return;
        }

        // Bug #127: 验证数值范围
        if (totalEnchantments < 0) totalEnchantments = 0;
        if (totalPages < 1) totalPages = 1;

        try {
            int currentPage = playerPage.getOrDefault(player, 0);
            if (currentPage < 0) currentPage = 0;
            if (currentPage >= totalPages) currentPage = totalPages - 1;

            // Bug #128: 安全地获取配置文本
            String infoPanelTitle = config.getInfoPanelTitle();
            if (infoPanelTitle == null || infoPanelTitle.isEmpty()) {
                infoPanelTitle = "&6信息面板";
            }

            ItemStack infoPanel = createButton(
                Material.PAPER,
                format(infoPanelTitle),
                Arrays.asList(
                    format("&7总附魔数: &e" + totalEnchantments),
                    format("&7页数: &e" + totalPages),
                    format("&7当前页: &e" + (currentPage + 1)),
                    "",
                    format("&7查看所有可用附魔信息"),
                    "",
                    format("&e点击附魔查看详情")
                )
            );

            // Bug #129: 检查slot范围和infoPanel是否为null
            if (50 < gui.getSize() && infoPanel != null) {
                gui.setItem(50, infoPanel);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("设置信息面板时出错: " + e.getMessage());
        }
    }

    private ItemStack createEnchantmentItem(Player player, EnchantmentData data) {
        // Bug #130: 检查参数
        if (player == null || data == null) {
            plugin.getLogger().warning("createEnchantmentItem: player或data为null");
            return new ItemStack(Material.BOOK);
        }

        try {
            // 使用缓存避免重复创建相同的附魔物品
            String id = data.getId();
            Rarity rarity = data.getRarity();
            final String finalId = (id == null) ? "unknown" : id;
            final Rarity finalRarity = (rarity == null) ? Rarity.COMMON : rarity;

            String cacheKey = finalId + "_" + finalRarity.name();
            return enchantmentItemCache.getOrCompute(cacheKey, key -> {
                try {
                    Enchantment enchantment = data.getEnchantment();
                    // Bug #131: 检查enchantment是否为null
                    if (enchantment == null) {
                        plugin.getLogger().warning("附魔对象为null: " + finalId);
                        return new ItemStack(Material.BOOK);
                    }

                    Material material = getEnchantmentMaterial(enchantment);
                    final Material finalMaterial = (material == null) ? Material.BOOK : material;

                    ItemStack item = new ItemStack(finalMaterial);
                    ItemMeta meta = item.getItemMeta();

                    // Bug #132: 检查meta是否为null
                    if (meta == null) {
                        plugin.getLogger().warning("ItemMeta为null: " + finalMaterial);
                        return item;
                    }

                    final String rarityColor = getRarityColor(finalRarity);
                    final String rarityName = getRarityName(finalRarity);
                    final String finalRarityColor = (rarityColor == null) ? "&f" : rarityColor;
                    final String finalRarityName = (rarityName == null) ? "普通" : rarityName;

                    String displayName = data.getDisplayName();
                    final String finalDisplayName = (displayName == null || displayName.isEmpty()) ? "未知附魔" : displayName;

                    // Use modern Adventure API instead of deprecated methods
                    com.enadd.util.ItemMetaHelper.setDisplayName(meta, finalRarityColor + finalDisplayName);

                    List<String> lore = new ArrayList<>();
                    lore.add(format(finalRarityColor + "[" + finalRarityName + "]"));
                    lore.add(format("&7等级: &e" + data.getMaxLevel() + " 级"));
                    lore.add(format("&7权重: &e" + data.getWeight()));
                    lore.add("");

                    String description = data.getDescription();
                    if (description != null && !description.isEmpty()) {
                        String[] lines = wrapText(description, 30);
                        // Bug #133: 检查lines是否为null
                        if (lines != null && lines.length > 0) {
                            for (String line : lines) {
                                if (line != null) {
                                    lore.add(format("&7" + line));
                                }
                            }
                        }
                    }

                    lore.add("");

                    String applicableItems = data.getApplicableItems();
                    if (applicableItems == null) applicableItems = "未知";
                    lore.add(format("&7适用: &f" + applicableItems));

                    List<String> conflicts = data.getConflicts();
                    // Bug #134: 检查conflicts是否为null
                    if (conflicts != null && !conflicts.isEmpty()) {
                        lore.add("");
                        lore.add(format("&c冲突附魔:"));

                        List<String> limitedConflicts = conflicts.stream()
                            .filter(c -> c != null && !c.isEmpty())
                            .limit(3)
                            .collect(Collectors.toList());

                        for (String conflict : limitedConflicts) {
                            lore.add(format("&c  - " + conflict));
                        }

                        if (conflicts.size() > 3) {
                            lore.add(format("&c  及其他 " + (conflicts.size() - 3) + " 个"));
                        }
                    }

                    lore.add("");
                    lore.add(format("&e左键查看详情 | 右键添加到模拟栏"));

                    // Use modern Adventure API instead of deprecated methods
                    com.enadd.util.ItemMetaHelper.setLore(meta, lore);
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

                    item.setItemMeta(meta);

                    return item;
                } catch (Exception e) {
                    plugin.getLogger().warning("创建附魔物品时出错: " + e.getMessage());
                    return new ItemStack(Material.BOOK);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("createEnchantmentItem出错: " + e.getMessage());
            return new ItemStack(Material.BOOK);
        }
    }

    private ItemStack createButton(Material material, String displayName, List<String> lore) {
        // Bug #135: 检查参数
        final Material finalMaterial = (material == null) ? Material.STONE : material;
        final String finalDisplayName = (displayName == null) ? "" : displayName;

        try {
            // 使用缓存避免重复创建按钮
            // Bug #136: 改进缓存键生成，避免冲突
            String cacheKey = finalMaterial.name() + "_" + finalDisplayName + "_" + (lore != null ? lore.hashCode() : 0);
            return buttonCache.getOrCompute(cacheKey, key -> {
                try {
                    ItemStack item = new ItemStack(finalMaterial);
                    ItemMeta meta = item.getItemMeta();

                    if (meta != null) {
                        com.enadd.util.ItemMetaHelper.setDisplayName(meta, finalDisplayName);
                        if (lore != null && !lore.isEmpty()) {
                            // Bug #137: 过滤null元素
                            List<String> filteredLore = lore.stream()
                                .filter(line -> line != null)
                                .collect(Collectors.toList());
                            com.enadd.util.ItemMetaHelper.setLore(meta, filteredLore);
                        }
                        item.setItemMeta(meta);
                    }

                    return item;
                } catch (Exception e) {
                    plugin.getLogger().warning("创建按钮时出错: " + e.getMessage());
                    return new ItemStack(finalMaterial);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().warning("createButton出错: " + e.getMessage());
            return new ItemStack(finalMaterial);
        }
    }

    private ItemStack createEmptyItem() {
        return new ItemStack(Material.AIR);
    }

    private ItemStack createPageIndicator(int currentPage, int totalPages) {
        // Bug #138: 验证参数范围
        if (currentPage < 0) currentPage = 0;
        if (totalPages < 1) totalPages = 1;
        if (currentPage >= totalPages) currentPage = totalPages - 1;

        try {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                com.enadd.util.ItemMetaHelper.setDisplayName(meta,
                    format("&e第 &f" + (currentPage + 1) + " &e页 / 共 &f" + totalPages + " &e页"));
                com.enadd.util.ItemMetaHelper.setLore(meta,
                    Arrays.asList(format("&7使用 &e← → &7键或点击按钮翻页")));
                item.setItemMeta(meta);
            }

            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("createPageIndicator出错: " + e.getMessage());
            return new ItemStack(Material.PAPER);
        }
    }

    private List<EnchantmentData> getAllEnchantments(Player player) {
        return new ArrayList<>();
    }

    private List<EnchantmentData> filterBySearch(List<EnchantmentData> enchantments, String query) {
        // Bug #88: 检查enchantments参数
        if (enchantments == null) {
            return new ArrayList<>();
        }

        if (query == null || query.isEmpty()) {
            return enchantments;
        }

        try {
            String lowerQuery = query.toLowerCase();
            return enchantments.stream()
                .filter(data -> {
                    // Bug #89: 安全的空指针链式调用
                    if (data == null) return false;

                    String id = data.getId();
                    String displayName = data.getDisplayName();
                    String description = data.getDescription();
                    String applicableItems = data.getApplicableItems();

                    return (id != null && id.toLowerCase().contains(lowerQuery)) ||
                           (displayName != null && displayName.toLowerCase().contains(lowerQuery)) ||
                           (description != null && description.toLowerCase().contains(lowerQuery)) ||
                           (applicableItems != null && applicableItems.toLowerCase().contains(lowerQuery));
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            plugin.getLogger().warning("搜索过滤时出错: " + e.getMessage());
            return enchantments;
        }
    }

    private List<EnchantmentData> filterByCategories(List<EnchantmentData> enchantments, Set<String> categories) {
        // Bug #90: 检查参数
        if (enchantments == null) {
            return new ArrayList<>();
        }

        if (categories == null || categories.isEmpty()) {
            return enchantments;
        }

        try {
            return enchantments.stream()
                .filter(data -> {
                    // Bug #91: 安全检查
                    if (data == null) return false;
                    String category = data.getEnchantmentCategory();
                    return category != null && categories.contains(category);
                })
                .collect(Collectors.toList());
        } catch (Exception e) {
            plugin.getLogger().warning("分类过滤时出错: " + e.getMessage());
            return enchantments;
        }
    }

    @SuppressWarnings("unused")
    private String getEnchantmentDescription(Enchantment enchantment) {
        String name = enchantment.toString();
        if (name == null) return "";

        String id = name.replace("Enchantment[", "").replace("]", "");

        Map<String, String> descriptions = new HashMap<>();
        descriptions.put("sharpness", "锋利附魔能够显著增加武器的攻击伤害，是最基础的伤害增强附魔。");
        descriptions.put("smite", "对准亡灵生物造成额外的神圣伤害，对僵尸、骷髅等效果显著。");
        descriptions.put("bane_of_arthropods", "对节肢生物造成额外伤害，并提高攻击速度。");
        descriptions.put("knockback", "攻击时将目标击退，减少被包围的风险。");
        descriptions.put("fire_aspect", "使武器带有火焰伤害，可点燃目标并造成持续燃烧。");
        descriptions.put("looting", "击杀生物时增加掉落物品的数量和稀有度。");
        descriptions.put("efficiency", "大幅提升工具的采集速度，是所有工具的核心附魔。");
        descriptions.put("silk_touch", "使方块以完整形式掉落，而非其掉落物形式。");
        descriptions.put("fortune", "大幅增加方块的掉落概率，是采集附魔中的顶级选择。");
        descriptions.put("protection", "提供全方位的伤害减免，是护甲的基础防护附魔。");
        descriptions.put("feather_falling", "显著减少掉落伤害，保护玩家免受高空坠落的伤害。");
        descriptions.put("thorns", "攻击穿戴者时，反弹伤害给攻击者。");
        descriptions.put("unbreaking", "显著延长工具和武器的耐久度，减少损耗。");
        descriptions.put("mending", "使用经验修补装备耐久，每点经验修复2点耐久。");

        return descriptions.getOrDefault(id, "这是一个强大的附魔，提供特殊效果。");
    }

    private String getApplicableItems(Enchantment enchantment) {
        String name = enchantment.toString();
        if (name == null) return "通用";

        String id = name.replace("Enchantment[", "").replace("]", "");

        Map<String, String> applicable = new HashMap<>();
        applicable.put("sharpness", "剑");
        applicable.put("smite", "剑");
        applicable.put("bane_of_arthropods", "剑");
        applicable.put("knockback", "剑");
        applicable.put("fire_aspect", "剑");
        applicable.put("looting", "剑");
        applicable.put("efficiency", "镐、斧、铲");
        applicable.put("silk_touch", "镐、斧、铲");
        applicable.put("fortune", "镐、斧、铲");
        applicable.put("protection", "头盔、胸甲、护腿、靴子");
        applicable.put("feather_falling", "靴子");
        applicable.put("thorns", "头盔、胸甲、护腿、靴子");
        applicable.put("unbreaking", "所有工具和武器");
        applicable.put("mending", "所有装备");
        applicable.put("power", "弓");
        applicable.put("punch", "弓");
        applicable.put("flame", "弓");
        applicable.put("infinity", "弓");

        return applicable.getOrDefault(id, "通用工具");
    }

    @SuppressWarnings("unused")
    private List<String> getConflicts(Enchantment enchantment) {
        List<String> conflicts = new ArrayList<>();
        if (enchantment == null) return conflicts;

        NamespacedKey key = enchantment.getKey();
        net.minecraft.resources.ResourceLocation loc = net.minecraft.resources.ResourceLocation.tryParse(key.toString());

        if (loc != null) {
            java.util.Set<net.minecraft.resources.ResourceLocation> conflictLocations =
                EnchantmentConflictManager.getConflictsFor(loc);

            for (net.minecraft.resources.ResourceLocation conflictLoc : conflictLocations) {
                conflicts.add(formatEnchantmentName(conflictLoc.getPath()));
            }
        }

        return conflicts;
    }

    private String formatEnchantmentName(String id) {
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                  .append(part.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    private Material getEnchantmentMaterial(Enchantment enchantment) {
        // Bug #139: 检查参数
        if (enchantment == null) {
            return Material.BOOK;
        }

        try {
            String name = enchantment.toString();
            if (name == null || name.isEmpty()) {
                return Material.BOOK;
            }

            String id = name.replace("Enchantment[", "").replace("]", "").toLowerCase();

            if (id.contains("sword") || id.contains("sharpness") || id.contains("smite") ||
                id.contains("looting") || id.contains("fire_aspect")) {
                return Material.DIAMOND_SWORD;
            } else if (id.contains("pickaxe") || id.contains("efficiency") || id.contains("fortune")) {
                return Material.DIAMOND_PICKAXE;
            } else if (id.contains("axe") || id.contains("bane_of_arthropods")) {
                return Material.DIAMOND_AXE;
            } else if (id.contains("bow") || id.contains("power") || id.contains("infinity")) {
                return Material.BOW;
            } else if (id.contains("helmet") || id.contains("protection") || id.contains("thorns")) {
                return Material.DIAMOND_HELMET;
            } else if (id.contains("chestplate") || id.contains("protection")) {
                return Material.DIAMOND_CHESTPLATE;
            } else if (id.contains("leggings") || id.contains("protection")) {
                return Material.DIAMOND_LEGGINGS;
            } else if (id.contains("boots") || id.contains("feather_falling")) {
                return Material.DIAMOND_BOOTS;
            } else if (id.contains("trident") || id.contains("loyalty") || id.contains("channeling")) {
                return Material.TRIDENT;
            } else if (id.contains("crossbow") || id.contains("multishot")) {
                return Material.CROSSBOW;
            } else if (id.contains("fishing_rod") || id.contains("luck") || id.contains("lure")) {
                return Material.FISHING_ROD;
            } else if (id.contains("book")) {
                return Material.ENCHANTED_BOOK;
            }

            return Material.BOOK;
        } catch (Exception e) {
            plugin.getLogger().warning("getEnchantmentMaterial出错: " + e.getMessage());
            return Material.BOOK;
        }
    }

    private String getCurrentSortName(Player player) {
        // Bug #140: 检查参数
        if (player == null) {
            return "默认";
        }

        try {
            // FIXME: 实现排序功能时，从玩家数据中获取当前排序方式
            return "权重降序";
        } catch (Exception e) {
            plugin.getLogger().warning("getCurrentSortName出错: " + e.getMessage());
            return "默认";
        }
    }

    private String getRarityColor(Rarity rarity) {
        // Bug #141: 检查参数
        if (rarity == null) {
            return "&f";
        }

        try {
            switch (rarity) {
                case LEGENDARY: return "&6";
                case EPIC: return "&5";
                case VERY_RARE: return "&d";
                case RARE: return "&3";
                case UNCOMMON: return "&2";
                case COMMON: return "&f";
                default: return "&f";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("getRarityColor出错: " + e.getMessage());
            return "&f";
        }
    }

    private String getRarityName(Rarity rarity) {
        // Bug #142: 检查参数
        if (rarity == null) {
            return "普通";
        }

        try {
            switch (rarity) {
                case LEGENDARY: return "传奇";
                case EPIC: return "史诗";
                case VERY_RARE: return "极稀有";
                case RARE: return "稀有";
                case UNCOMMON: return "优秀";
                case COMMON: return "普通";
                default: return "普通";
            }
        } catch (Exception e) {
            plugin.getLogger().warning("getRarityName出错: " + e.getMessage());
            return "普通";
        }
    }

    private String[] wrapText(String text, int maxLength) {
        // Bug #143: 检查参数
        if (text == null || text.isEmpty()) return new String[]{""};
        if (maxLength <= 0) maxLength = 30;

        try {
            List<String> lines = new ArrayList<>();
            String[] words = text.split(" ");

            // Bug #144: 检查words数组
            if (words == null || words.length == 0) {
                return new String[]{text};
            }

            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                // Bug #145: 检查word是否为null
                if (word == null) {
                    continue;
                }

                if (currentLine.length() + word.length() + 1 > maxLength) {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                    }
                    currentLine = new StringBuilder(word);
                } else {
                    if (currentLine.length() > 0) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                }
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }

            // Bug #146: 确保至少返回一个元素
            if (lines.isEmpty()) {
                return new String[]{""};
            }

            return lines.toArray(new String[0]);
        } catch (Exception e) {
            plugin.getLogger().warning("wrapText出错: " + e.getMessage());
            return new String[]{text};
        }
    }

    private String format(String text) {
        // Bug #147: 检查参数
        if (text == null) return "";

        try {
            return ColorFormatter.format(text);
        } catch (Exception e) {
            plugin.getLogger().warning("format出错: " + e.getMessage());
            return text;
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Bug #148: 检查event参数
        if (event == null) {
            return;
        }

        try {
            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();
            if (player == null) return;

            if (!openGuis.containsKey(player)) return;

            // ✅ 修复：全局取消所有点击，防止物品复制
            event.setCancelled(true);
            
            // ✅ 修复：检查点击类型
            switch (event.getClick()) {
                case NUMBER_KEY:
                case DROP:
                case CONTROL_DROP:
                case DOUBLE_CLICK:
                    // 阻止所有特殊操作
                    return;
                case SHIFT_LEFT:
                case SHIFT_RIGHT:
                    // 阻止Shift点击
                    int rawSlot = event.getRawSlot();
                    if (rawSlot >= event.getView().getTopInventory().getSize()) {
                        // 点击了玩家背包，阻止移动到GUI
                        return;
                    }
                    break;
                default:
                    break;
            }

            Inventory clickedInventory = event.getClickedInventory();

            // Bug #149: 检查clickedInventory和topInventory
            if (clickedInventory != null) {
                Inventory topInventory = player.getOpenInventory().getTopInventory();
                if (topInventory != null && clickedInventory.equals(topInventory)) {
                    int slot = event.getSlot();
                    ItemStack clickedItem = event.getCurrentItem();

                    // Bug #150: 验证slot范围
                    if (slot < 0 || slot >= clickedInventory.getSize()) {
                        return;
                    }

                    if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

                    handleGuiClick(player, slot, clickedItem, event.isRightClick());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("处理点击事件时出错: " + e.getMessage());
        }
    }

    @SuppressWarnings("null")
    private void handleGuiClick(Player player, int slot, ItemStack item, boolean isRightClick) {
        // Bug #78: 添加player验证
        if (player == null) {
            plugin.getLogger().warning("handleGuiClick: player为null");
            return;
        }

        try {
            if (slot == 45) {
                // 上一页按钮
                int currentPage = playerPage.getOrDefault(player, 0);
                if (currentPage > 0) {
                    refreshGui(player, currentPage - 1);
                }
            } else if (slot == 53) {
                // 下一页按钮
                List<EnchantmentData> enchantments = playerFilteredEnchantments.getOrDefault(player, getAllEnchantments(player));
                // Bug #79: 检查enchantments是否为null
                if (enchantments == null || enchantments.isEmpty()) {
                    return;
                }
                // Bug #80: 防止除零
                int totalPages = Math.max(1, (int) Math.ceil((double) enchantments.size() / PAGE_SIZE));
                int currentPage = playerPage.getOrDefault(player, 0);
                if (currentPage < totalPages - 1) {
                    refreshGui(player, currentPage + 1);
                }
            } else if (slot == 46) {
                // 清除筛选按钮
                playerActiveFilters.put(player, new HashSet<String>());
                refreshGui(player, 0);
            } else if (slot == 4) {
                // 搜索按钮
                // Bug #81: 修复searchInputHandler未初始化问题
                try {
                    SearchInputHandler searchInputHandler = new SearchInputHandler(plugin, this);
                    if (searchInputHandler == null) {
                        plugin.getLogger().warning("无法创建SearchInputHandler");
                        player.sendMessage(format("&c搜索功能暂时不可用"));
                        return;
                    }

                    if (isRightClick) {
                        playerSearchQuery.remove(player);
                        player.sendMessage(format("&a搜索已清除"));
                        refreshGui(player, 0);
                    } else if (!searchInputHandler.isPendingSearch(player)) {
                        searchInputHandler.startSearch(player);
                        player.closeInventory();
                        String searchPrompt = config.getMessage("search-prompt");
                        if (searchPrompt != null && !searchPrompt.isEmpty()) {
                            player.sendMessage(format(searchPrompt));
                        }
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (player.isOnline()) {
                                player.sendMessage(format("&e请在聊天框输入搜索关键词..."));
                                player.sendMessage(format("&7输入 &e/cancel &7取消搜索"));
                            }
                        }, 1L);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("处理搜索时出错: " + e.getMessage());
                    player.sendMessage(format("&c搜索功能出错，请稍后重试"));
                }
            } else if (slot >= 10 && slot <= 43 && !isBorderSlot(slot)) {
                // 附魔物品点击
                // Bug #82: 检查inventory是否为null
                Inventory topInventory = player.getOpenInventory().getTopInventory();
                if (topInventory == null) {
                    return;
                }

                ItemStack clickedItem = topInventory.getItem(slot);
                if (clickedItem != null && clickedItem.hasItemMeta()) {
                    EnchantmentData data = extractEnchantmentData(clickedItem, player);
                    if (data != null) {
                        try {
                            if (isRightClick) {
                                // Bug #83: 检查simulator是否为null
                                if (simulator != null) {
                                    simulator.openSimulation(player, data);
                                } else {
                                    player.sendMessage(format("&c模拟功能暂时不可用"));
                                }
                            } else {
                                showEnchantmentDetails(player, data);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("处理附魔点击时出错: " + e.getMessage());
                            player.sendMessage(format("&c操作失败，请稍后重试"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("handleGuiClick出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private EnchantmentData extractEnchantmentData(ItemStack item, Player player) {
        // Bug #84: 检查item参数
        if (item == null) {
            return null;
        }

        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !com.enadd.util.ItemMetaHelper.hasLore(meta)) return null;

            List<String> lore = com.enadd.util.ItemMetaHelper.getLoreAsStrings(meta);
            if (lore == null || lore.isEmpty()) return null;

            String displayName = com.enadd.util.ItemMetaHelper.getDisplayNameAsString(meta);
            if (displayName == null || displayName.isEmpty()) return null;

            // Bug #85: 移除颜色代码以便正确匹配
            String cleanDisplayName = displayName.replaceAll("§[0-9a-fk-or]", "").trim();
            if (cleanDisplayName.isEmpty()) return null;

            List<EnchantmentData> allEnchantments = playerFilteredEnchantments.getOrDefault(player, getAllEnchantments(player));
            // Bug #86: 检查allEnchantments是否为null
            if (allEnchantments == null || allEnchantments.isEmpty()) {
                return null;
            }

            for (EnchantmentData data : allEnchantments) {
                if (data == null) continue;

                String dataName = data.getDisplayName();
                if (dataName == null || dataName.isEmpty()) continue;

                // Bug #87: 更安全的名称匹配
                String cleanDataName = dataName.replaceAll("§[0-9a-fk-or]", "").trim();
                if (cleanDisplayName.contains(cleanDataName) || cleanDataName.contains(cleanDisplayName)) {
                    return data;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("提取附魔数据时出错: " + e.getMessage());
        }

        return null;
    }

    public void setSearchQuery(Player player, String query) {
        // Bug #104: 检查参数
        if (player == null) {
            plugin.getLogger().warning("setSearchQuery: player为null");
            return;
        }

        try {
            // Bug #105: 处理null query
            if (query == null) {
                query = "";
            }

            playerSearchQuery.put(player, query);

            List<EnchantmentData> allEnchantments = getAllEnchantments(player);
            if (allEnchantments == null) {
                allEnchantments = new ArrayList<>();
            }

            List<EnchantmentData> filtered = filterBySearch(allEnchantments, query);
            if (filtered == null) {
                filtered = allEnchantments;
            }

            playerFilteredEnchantments.put(player, filtered);
        } catch (Exception e) {
            plugin.getLogger().warning("设置搜索查询时出错: " + e.getMessage());
        }
    }

    public void refreshGui(Player player) {
        // Bug #106: 检查参数
        if (player == null) {
            plugin.getLogger().warning("refreshGui: player为null");
            return;
        }
        refreshGui(player, 0);
    }

    public void refreshGui(Player player, int page) {
        // Bug #107: 检查参数
        if (player == null) {
            plugin.getLogger().warning("refreshGui: player为null");
            return;
        }

        try {
            Inventory gui = openGuis.get(player);
            // Bug #108: 检查gui是否存在
            if (gui != null) {
                // Bug #109: 验证page范围
                if (page < 0) {
                    page = 0;
                }
                initializeGui(gui, player, page);
            } else {
                plugin.getLogger().warning("refreshGui: 玩家 " + player.getName() + " 没有打开的GUI");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("刷新GUI时出错: " + e.getMessage());
        }
    }

    private boolean isBorderSlot(int slot) {
        // Bug #110: 检查slot范围
        if (slot < 0 || slot >= GUI_SIZE) {
            return true; // 超出范围的slot视为边框
        }

        try {
            return slot < 10 || (slot >= 45 && slot <= 53) || slot % 9 == 0 || slot % 9 == 8;
        } catch (Exception e) {
            plugin.getLogger().warning("isBorderSlot出错: " + e.getMessage());
            return true;
        }
    }

    private void showEnchantmentDetails(Player player, EnchantmentData data) {
        // Bug #92: 检查参数
        if (player == null || data == null) {
            if (player != null) {
                player.sendMessage(format("&c无法显示附魔详情"));
            }
            return;
        }

        if (!player.isOnline()) {
            return;
        }

        try {
            player.sendMessage("");
            player.sendMessage(format("&6╔══════════════════════════════╗"));

            String displayName = data.getDisplayName();
            if (displayName == null) displayName = "未知附魔";
            player.sendMessage(format("&6║ &e" + centerText(displayName, 24) + " &6║"));

            player.sendMessage(format("&6╠══════════════════════════════╣"));

            String id = data.getId();
            if (id == null) id = "unknown";
            player.sendMessage(format("&6║ &7ID: &f" + padText(id, 22) + " &6║"));

            String rarityName = getRarityName(data.getRarity());
            if (rarityName == null) rarityName = "普通";
            player.sendMessage(format("&6║ &7稀有度: &f" + padText(rarityName, 16) + " &6║"));

            player.sendMessage(format("&6║ &7最大等级: &f" + padText(String.valueOf(data.getMaxLevel()), 15) + " &6║"));
            player.sendMessage(format("&6║ &7权重: &f" + padText(String.valueOf(data.getWeight()), 19) + " &6║"));
            player.sendMessage(format("&6╠══════════════════════════════╣"));
            player.sendMessage(format("&6║ &7描述: " + padText("", 19) + " &6║"));

            String description = data.getDescription();
            if (description == null || description.isEmpty()) {
                description = "暂无描述";
            }

            // Bug #93: 检查wrapText返回值
            String[] descLines = wrapText(description, 28);
            if (descLines == null || descLines.length == 0) {
                descLines = new String[]{"暂无描述"};
            }

            for (String line : descLines) {
                if (line == null) line = "";
                player.sendMessage(format("&6║ &f" + padText(line, 26) + " &6║"));
            }

            player.sendMessage(format("&6╠══════════════════════════════╣"));

            String applicableItems = data.getApplicableItems();
            if (applicableItems == null) applicableItems = "未知";
            player.sendMessage(format("&6║ &7适用工具: &f" + padText(applicableItems, 14) + " &6║"));

            List<String> conflicts = data.getConflicts();
            // Bug #94: 检查conflicts是否为null
            if (conflicts != null && !conflicts.isEmpty()) {
                player.sendMessage(format("&6╠══════════════════════════════╣"));
                player.sendMessage(format("&6║ &c冲突附魔: " + padText("", 16) + " &6║"));

                String[] conflictArray = conflicts.stream()
                    .filter(c -> c != null && !c.isEmpty())
                    .limit(4)
                    .toArray(String[]::new);

                for (String conflict : conflictArray) {
                    player.sendMessage(format("&6║ &c  - " + padText(conflict, 22) + " &6║"));
                }
            }

            player.sendMessage(format("&6╚══════════════════════════════╝"));
            player.sendMessage("");
            player.sendMessage(format("&e右键附魔图标可打开模拟界面"));
        } catch (Exception e) {
            plugin.getLogger().warning("显示附魔详情时出错: " + e.getMessage());
            if (player.isOnline()) {
                player.sendMessage(format("&c显示详情时出错"));
            }
        }
    }

    private String centerText(String text, int width) {
        // Bug #95: 检查参数
        if (text == null) text = "";
        if (width <= 0) width = 1;

        try {
            if (text.length() >= width) {
                return text.substring(0, width);
            }
            int padding = (width - text.length()) / 2;
            // Bug #96: 防止负数padding
            padding = Math.max(0, padding);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < padding; i++) {
                sb.append(" ");
            }
            sb.append(text);
            while (sb.length() < width) {
                sb.append(" ");
            }
            return sb.toString();
        } catch (Exception e) {
            plugin.getLogger().warning("centerText出错: " + e.getMessage());
            return text;
        }
    }

    private String padText(String text, int width) {
        // Bug #97: 检查参数
        if (text == null) text = "";
        if (width <= 0) width = 1;

        try {
            if (text.length() >= width) {
                return text.substring(0, width);
            }
            StringBuilder sb = new StringBuilder(text);
            while (sb.length() < width) {
                sb.append(" ");
            }
            return sb.toString();
        } catch (Exception e) {
            plugin.getLogger().warning("padText出错: " + e.getMessage());
            return text;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Bug #98: 检查event参数
        if (event == null) {
            return;
        }

        try {
            if (!(event.getPlayer() instanceof Player)) return;

            Player player = (Player) event.getPlayer();
            if (player == null) return;

            // Bug #99: 安全地清理玩家数据
            openGuis.remove(player);
            playerPage.remove(player);
            playerFilteredEnchantments.remove(player);
            playerSearchQuery.remove(player);
            playerActiveFilters.remove(player);
            // Bug #100: 清理缓存的边框物品
            cachedBorderItems.remove(player);
            
            // ✅ 移除GUI全局保护
            GUIProtectionManager protectionManager = GUIProtectionManager.getInstance();
            if (protectionManager != null) {
                protectionManager.unmarkPlayerInGUI(player);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("关闭GUI时出错: " + e.getMessage());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Bug #101: 检查event参数
        if (event == null) {
            return;
        }

        try {
            if (!(event.getWhoClicked() instanceof Player)) return;

            Player player = (Player) event.getWhoClicked();
            if (player == null) return;

            if (openGuis.containsKey(player)) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("处理拖拽事件时出错: " + e.getMessage());
        }
    }

    public void reload() {
        // Bug #102: 检查config是否为null
        if (config == null) {
            plugin.getLogger().warning("config为null，无法重载");
            return;
        }

        try {
            config.reload();
            // Bug #103: 清理所有缓存
            if (enchantmentItemCache != null) {
                enchantmentItemCache.clear();
            }
            if (buttonCache != null) {
                buttonCache.clear();
            }
            if (borderItemCache != null) {
                borderItemCache.clear();
            }
            cachedBorderItems.clear();

            plugin.getLogger().info("GUI配置已重载，缓存已清理");
        } catch (Exception e) {
            plugin.getLogger().severe("重载GUI配置时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class EnchantmentData {
        private final Enchantment enchantment;
        private final String id;
        private final String displayName;
        private final String description;
        private final String applicableItems;
        private final int maxLevel;
        private final int weight;
        private final List<String> conflicts;
        private final Rarity rarity;
        private final String enchantmentCategory;

        public EnchantmentData(Enchantment enchantment, String id, String displayName, String description,
                             String applicableItems, int maxLevel, int weight, List<String> conflicts, Rarity rarity) {
            this.enchantment = enchantment;
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.applicableItems = applicableItems;
            this.maxLevel = maxLevel;
            this.weight = weight;
            this.conflicts = conflicts;
            this.rarity = rarity;
            this.enchantmentCategory = determineCategory(enchantment);
        }

        private String determineCategory(Enchantment enchantment) {
            if (enchantment instanceof BaseEnchantment base) {
                // 如果是我们的附魔类，可以尝试获取更详细的信息
                // 这里暂时返回通用，或者可以根据 key 分类
                return "通用";
            }
            return "通用";
        }

        public Enchantment getEnchantment() { return enchantment; }
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getApplicableItems() { return applicableItems; }
        public int getMaxLevel() { return maxLevel; }
        public int getWeight() { return weight; }
        public List<String> getConflicts() { return conflicts; }
        public Rarity getRarity() { return rarity; }
        public String getEnchantmentCategory() { return enchantmentCategory; }
    }

    public static class EnchantmentGuiSession {
        private final Player player;
        private final long openedAt;
        private int currentPage;
        private String searchQuery;
        private Set<String> activeFilters;

        public EnchantmentGuiSession(Player player) {
            this.player = player;
            this.openedAt = System.currentTimeMillis();
            this.currentPage = 0;
            this.searchQuery = "";
            this.activeFilters = new HashSet<>();
        }

        public Player getPlayer() { return player; }
        public long getOpenedAt() { return openedAt; }
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int page) { this.currentPage = page; }
        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String query) { this.searchQuery = query; }
        public Set<String> getActiveFilters() { return activeFilters; }
        public void setActiveFilters(Set<String> filters) { this.activeFilters = filters; }
        public long getSessionDuration() { return System.currentTimeMillis() - openedAt; }
    }
}
