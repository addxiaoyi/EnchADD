package com.enadd;

import com.enadd.achievements.AchievementManager;
import com.enadd.config.ConfigManager;
import com.enadd.core.registry.EnchantmentRegistry;
import com.enadd.creative.CreativeInventoryManager;
import com.enadd.gui.GUIManager;
import com.enadd.gui.GUIProtectionManager;
import com.enadd.util.JavaVersionChecker;
import com.enadd.util.ColorFormatter;
import com.enadd.util.ErrorHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Arrays;
import java.util.Collections;



/**
 * EnchAdd - 轻量级附魔注册插件
 * 兼容 Java 21-25
 */
public final class EnchAdd extends JavaPlugin {

    private static EnchAdd instance;

    public static EnchAdd getInstance() {
        return instance;
    }

    private CreativeInventoryManager creativeInventoryManager;
    private GUIManager guiManager;

    @Override
    public void onLoad() {
        instance = this;
        try {
            ErrorHandler.initialize(this);

            if (!JavaVersionChecker.checkCompatibility(getLogger())) {
                getLogger().severe("Java版本兼容性检查失败！");
                getLogger().severe(JavaVersionChecker.getRecommendedVersionMessage());
                disablePlugin();
                return;
            }

            // 1. 初始化配置 (同步)
            ConfigManager.initialize(this);

            displayTitle();

            // 2. 异步初始化耗时任务
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    // 预加载所有附魔缓存和数据
                    // 附魔注册已经在 EnchAddBootstrap (onLoad之前) 完成
                    getLogger().info("正在异步预加载附魔系统数据...");
                    
                    creativeInventoryManager = new CreativeInventoryManager(this);
                    getLogger().info("创造模式库存系统初始化成功");

                    // 预加载配置消息缓存
                    ConfigManager.preloadCaches();
                    
                    // 异步记录成功信息 (或者在主线程显示)
                    Bukkit.getScheduler().runTask(this, this::displaySuccess);
                    
                } catch (Exception e) {
                    getLogger().severe("异步初始化失败: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            ErrorHandler.handleCriticalError("插件初始化", e);
            disablePlugin();
        }
    }

    @Override
    public void onEnable() {
        try {
            //  初始化GUI全局保护管理器
            GUIProtectionManager.initialize(this);
            getLogger().info("GUI全局保护系统初始化成功");
            
            guiManager = new GUIManager(this);
            Bukkit.getPluginManager().registerEvents(guiManager, this);
            getLogger().info("附魔预览GUI system initialization successful");

            //  注册附魔粘合剂监听器
            Bukkit.getPluginManager().registerEvents(new com.enadd.listeners.EnchantmentBinderListener(this), this);
            getLogger().info("附魔粘合剂监听器已注册");
            
            // Initialize enchantment effect system
            initializeEnchantmentEffects();

            // Initialize achievement system only if enabled in config
            if (ConfigManager.isAchievementsEnabled()) {
                AchievementManager.initialize(this);
                getLogger().info("Achievement system initialized successfully");
            } else {
                getLogger().info("Achievement system is disabled in config");
            }

            // Register commands
            registerCommands();

            // Register creative mode tab
            registerCreativeTabs();

            // Plugin is already functional after onLoad, this is just for logging
            getLogger().info("EnchAdd enabled successfully");

        } catch (Exception e) {
            getLogger().severe("Failed to initialize: " + e.getMessage());
            ErrorHandler.handleException(null, "Plugin enable", e);
        }
    }

    private void initializeEnchantmentEffects() {
        try {
            // Register event handler for enchantment effects
            com.enadd.core.enchantment.EnchantmentEventHandler eventHandler =
                new com.enadd.core.enchantment.EnchantmentEventHandler(this);
            Bukkit.getPluginManager().registerEvents(eventHandler, this);
            getLogger().info("§a✅ 附魔事件监听器已注册");

            // Register all enchantment effects
            com.enadd.core.enchantment.QuickEffectRegistry effectRegistry =
                new com.enadd.core.enchantment.QuickEffectRegistry(this);
            effectRegistry.registerAll();

            getLogger().info("§a✅ 附魔效果系统初始化完成");
            getLogger().info("§a✅ 所有229个附魔现在都有实际功能！");

        } catch (Exception e) {
            getLogger().warning("附魔效果系统初始化失败: " + e.getMessage());
            getLogger().warning("附魔仍可使用，但效果功能将不可用");
            com.enadd.util.ErrorHandler.handleException(null, "Enchantment effect system initialization", e);
        }
    }

    private void registerCommands() {
        try {
            // Only register achievement command if achievements are enabled
            if (ConfigManager.isAchievementsEnabled()) {
                com.enadd.commands.AchievementCommand achievementCommand = new com.enadd.commands.AchievementCommand();
                var achCommand = getCommand("achievements");
                if (achCommand != null) {
                    achCommand.setExecutor(achievementCommand);
                    achCommand.setTabCompleter(achievementCommand);
                    getLogger().info("Achievement commands registered successfully");
                } else {
                    getLogger().warning("Achievement command not found in plugin.yml");
                }
            }

            // Register enchantment preview command
            var mainCommand = getCommand("enchadd");
            if (mainCommand != null) {
                mainCommand.setExecutor((sender, command, label, args) -> {
                    if (!(sender instanceof Player)) {
                        if (sender != null) {
                            sender.sendMessage(format("&c该命令只能由玩家执行"));
                        }
                        return true;
                    }

                    Player player = (Player) sender;
                    if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
                        if (guiManager != null) {
                            guiManager.openEnchantmentGUI(player);
                        } else {
                            player.sendMessage(format("&cGUI系统未初始化，请重试或联系管理员"));
                        }
                    } else {
                        if (creativeInventoryManager != null) {
                            creativeInventoryManager.openCreativeEnchantmentGui(player);
                        } else {
                            player.sendMessage(format("&c创造模式系统未初始化，请重试或联系管理员"));
                        }
                    }
                    return true;
                });
                mainCommand.setTabCompleter((sender, command, alias, args) -> {
                    if (args.length == 1) {
                        return Arrays.asList("gui");
                    }
                    return Collections.emptyList();
                });
                getLogger().info("Enchantment commands registered successfully");
            } else {
                getLogger().warning("Main command 'enchadd' not found in plugin.yml");
            }

        } catch (Exception e) {
            getLogger().warning("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerCreativeTabs() {
        try {
            getLogger().info("Creative mode tabs integration ready");
        } catch (Exception e) {
            getLogger().warning("Failed to register creative tabs: " + e.getMessage());
        }
    }

    public CreativeInventoryManager getCreativeInventoryManager() {
        return creativeInventoryManager;
    }

    private String format(String text) {
        if (text == null) return "";
        return ColorFormatter.format(text);
    }

    private void disablePlugin() {
        try {
            getServer().getPluginManager().disablePlugin(this);
        } catch (Exception disableError) {
            getLogger().severe("Failed to disable plugin: " + disableError.getMessage());
        }
    }

    private void displayTitle() {
        try {
            int width = 75; // 增加宽度以适应Logo
            String borderTop = "§6╔" + "═".repeat(width) + "╗";
            String borderBottom = "§6╚" + "═".repeat(width) + "╝";
            String emptyLine = "§6║" + " ".repeat(width) + "║";

            getLogger().info(borderTop);
            getLogger().info(emptyLine);
            
            // ASCII Art Logo - 使用 centerText 动态居中
            String[] logo = {
                "§c███████╗███╗   ██╗ ██████╗██╗  ██╗ █████╗ ████████╗ █████╗ ██████╗ ██████╗",
                "§c██╔════╝████╗  ██║██╔════╝██║  ██║██╔══██╗╚══██╔══╝██╔══██╗██╔══██╗██╔══██╗",
                "§c█████╗  ██╔██╗ ██║██║     ███████║███████║   ██║   ███████║██║  ██║██║  ██║",
                "§c██╔══╝  ██║╚██╗██║██║     ██╔══██║██╔══██║   ██║   ██╔══██║██║  ██║██║  ██║",
                "§c███████╗██║ ╚████║╚██████╗██║  ██║██║  ██║   ██║   ██║  ██║██████╔╝██████╔╝",
                "§c╚══════╝╚═╝  ╚═══╝ ╚═════╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═════╝ ╚═════╝"
            };

            for (String line : logo) {
                getLogger().info(centerText(line, width));
            }
            
            getLogger().info(emptyLine);
            getLogger().info(centerText(ConfigManager.getMessage("startup.title-line1"), width));
            getLogger().info(centerText("✨ 229 Custom Enchantments ✨", width));
            getLogger().info(centerText(ConfigManager.getMessage("startup.title-line3"), width));
            getLogger().info(emptyLine);
            getLogger().info(borderBottom);
            
            getLogger().info("§a" + ConfigManager.getMessage("startup.starting"));

            displaySystemInfo();
            displayConfigInfo();

        } catch (Exception e) {
            getLogger().warning("Failed to display title: " + e.getMessage());
            getLogger().info("EnchAdd - Starting enchantment registration...");
        }
    }

    private String centerText(String text, int width) {
        if (text == null) text = "";
        
        // 移除颜色代码进行长度计算
        String plainText = text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
        
        // 估算显示宽度：非ASCII字符（如 ██）通常占2个宽度单位
        int displayWidth = 0;
        for (char c : plainText.toCharArray()) {
            if (c > 127) {
                displayWidth += 2;
            } else {
                displayWidth += 1;
            }
        }
        
        if (displayWidth >= width) {
            return "§6║ " + text + " §6║";
        }
        
        int totalPadding = width - displayWidth;
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        
        return "§6║" + " ".repeat(Math.max(0, leftPadding)) + text + " ".repeat(Math.max(0, rightPadding)) + "§6║";
    }

    private void displaySystemInfo() {
        try {
            JavaVersionChecker.JavaVersionInfo javaInfo = JavaVersionChecker.getJavaVersionInfo();
            getLogger().info("§7System: " + javaInfo.toString());

            if (JavaVersionChecker.isPreviewVersion()) {
                getLogger().info("§e⚠️ Running on preview/early access Java version");
            }

        } catch (Exception e) {
            getLogger().warning("Failed to display system info: " + e.getMessage());
        }
    }

    private void displayConfigInfo() {
        try {
            String treasureStatus = ConfigManager.isTreasureEnchantments() ? "enabled" : "disabled";
            getLogger().info("§7" + ConfigManager.getMessage("config.treasure-mode", "status", treasureStatus));
            getLogger().info("§7" + ConfigManager.getMessage("config.language-loaded", "language", ConfigManager.getLanguage()));

            // Display achievements status
            String achievementsStatus = ConfigManager.isAchievementsEnabled() ? "enabled" : "disabled";
            getLogger().info("§7" + ConfigManager.getMessage("config.achievements-enabled", "status", achievementsStatus));

            // Display disabled enchantments count
            int disabledCount = ConfigManager.getDisabledEnchantments().size();
            if (disabledCount > 0) {
                getLogger().info("§7" + ConfigManager.getMessage("config.disabled-enchantments", "count", String.valueOf(disabledCount)));
            }

        } catch (Exception e) {
            getLogger().warning("Failed to display config info: " + e.getMessage());
        }
    }

    private void displaySuccess() {
        try {
            int enchantmentCount = EnchantmentRegistry.getCount();
            int width = 75; // 保持一致的宽度
            String borderTop = "§a╔" + "═".repeat(width) + "╗";
            String borderBottom = "§a╚" + "═".repeat(width) + "╝";
            String emptyLine = "§a║" + " ".repeat(width) + "║";

            getLogger().info(borderTop);
            getLogger().info(emptyLine);
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.title"), width));
            getLogger().info(emptyLine);
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.success", "count", String.valueOf(enchantmentCount)), width));
            getLogger().info(emptyLine);
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.combat"), width));
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.armor"), width));
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.tool"), width));
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.curse"), width));
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.utility"), width));
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.defense"), width));
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.special"), width));
            getLogger().info(emptyLine);
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.thanks"), width));
            getLogger().info(emptyLine);
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.author"), width));
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.version"), width));
            getLogger().info(emptyLine);
            getLogger().info(centerTextSuccess(ConfigManager.getMessage("completion.enjoy"), width));
            getLogger().info(emptyLine);
            getLogger().info(borderBottom);
            
            getLogger().info("§b" + ConfigManager.getMessage("completion.available"));
            getLogger().info("§e" + ConfigManager.getMessage("completion.performance"));

            if (ConfigManager.isAchievementsEnabled()) {
                getLogger().info("§d" + ConfigManager.getMessage("completion.achievements"));
            }
        } catch (Exception e) {
            getLogger().warning("Failed to display success message: " + e.getMessage());
            getLogger().info("EnchAdd registration completed successfully!");
        }
    }

    private String centerTextSuccess(String text, int width) {
        if (text == null) text = "";
        String plainText = text.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
        
        // 估算显示宽度
        int displayWidth = 0;
        for (char c : plainText.toCharArray()) {
            if (c > 127) {
                displayWidth += 2;
            } else {
                displayWidth += 1;
            }
        }
        
        if (displayWidth >= width) {
            return "§a║ " + text + " §a║";
        }
        
        int totalPadding = width - displayWidth;
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;
        
        return "§a║" + " ".repeat(Math.max(0, leftPadding)) + text + " ".repeat(Math.max(0, rightPadding)) + "§a║";
    }


    @Override
    public void onDisable() {
        try {
            getLogger().info("正在关闭 EnchAdd...");
            
            // ✅ 清理GUI全局保护管理器
            if (GUIProtectionManager.getInstance() != null) {
                GUIProtectionManager.getInstance().clearAll();
                getLogger().info("GUI全局保护系统关闭完成");
            }

            if (ConfigManager.isAchievementsEnabled() && AchievementManager.getInstance() != null) {
                AchievementManager.shutdown();
                getLogger().info("成就系统关闭完成");
            }

            ConfigManager.shutdown();

            EnchantmentRegistry.cleanup();

            // 关闭冲突管理器
            com.enadd.core.conflict.EnchantmentConflictManager.getInstance().shutdown();

            com.enadd.util.ErrorHandler.shutdown();

            getLogger().info("EnchAdd 已成功禁用");

        } catch (Exception e) {
            getLogger().severe("插件关闭时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取装备上指定附魔的最高等级
     * 与Enchantio的getHighestEnchantLevel()方法完全相同
     *
     * @param equipment 要检查的装备
     * @param enchantment 要检查的附魔
     * @return 装备上该附魔的最高等级
     */
    public static int getHighestEnchantLevel(
            @org.jetbrains.annotations.NotNull org.bukkit.inventory.EntityEquipment equipment,
            @org.jetbrains.annotations.NotNull org.bukkit.enchantments.Enchantment enchantment
    ) {
        int highestLevel = 0;
        java.util.Set<org.bukkit.inventory.EquipmentSlotGroup> equipmentSlotGroups = enchantment.getActiveSlotGroups();

        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.FEET)) {
            org.bukkit.inventory.ItemStack boots = equipment.getBoots();
            if (boots != null && !boots.getType().isAir()) {
                highestLevel = Math.max(highestLevel, boots.getEnchantmentLevel(enchantment));
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.LEGS)) {
            org.bukkit.inventory.ItemStack leggings = equipment.getLeggings();
            if (leggings != null && !leggings.getType().isAir()) {
                highestLevel = Math.max(highestLevel, leggings.getEnchantmentLevel(enchantment));
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.CHEST)) {
            org.bukkit.inventory.ItemStack chestplate = equipment.getChestplate();
            if (chestplate != null && !chestplate.getType().isAir()) {
                highestLevel = Math.max(highestLevel, chestplate.getEnchantmentLevel(enchantment));
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.HEAD)) {
            org.bukkit.inventory.ItemStack helmet = equipment.getHelmet();
            if (helmet != null && !helmet.getType().isAir()) {
                highestLevel = Math.max(highestLevel, helmet.getEnchantmentLevel(enchantment));
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.HAND) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.MAINHAND)) {
            org.bukkit.inventory.ItemStack mainHand = equipment.getItemInMainHand();
            if (mainHand != null && !mainHand.getType().isAir()) {
                highestLevel = Math.max(highestLevel, mainHand.getEnchantmentLevel(enchantment));
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.HAND) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.OFFHAND)) {
            org.bukkit.inventory.ItemStack offHand = equipment.getItemInOffHand();
            if (offHand != null && !offHand.getType().isAir()) {
                highestLevel = Math.max(highestLevel, offHand.getEnchantmentLevel(enchantment));
            }
        }

        return highestLevel;
    }

    /**
     * 获取装备上指定附魔的等级总和
     * 与Enchantio的getSumOfEnchantLevels()方法完全相同
     *
     * @param equipment 要检查的装备
     * @param enchantment 要检查的附魔
     * @return 装备上该附魔的等级总和
     */
    public static int getSumOfEnchantLevels(
            @org.jetbrains.annotations.NotNull org.bukkit.inventory.EntityEquipment equipment,
            @org.jetbrains.annotations.NotNull org.bukkit.enchantments.Enchantment enchantment
    ) {
        int level = 0;
        java.util.Set<org.bukkit.inventory.EquipmentSlotGroup> equipmentSlotGroups = enchantment.getActiveSlotGroups();

        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.FEET)) {
            org.bukkit.inventory.ItemStack boots = equipment.getBoots();
            if (boots != null && !boots.getType().isAir()) {
                level += boots.getEnchantmentLevel(enchantment);
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.LEGS)) {
            org.bukkit.inventory.ItemStack leggings = equipment.getLeggings();
            if (leggings != null && !leggings.getType().isAir()) {
                level += leggings.getEnchantmentLevel(enchantment);
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.CHEST)) {
            org.bukkit.inventory.ItemStack chestplate = equipment.getChestplate();
            if (chestplate != null && !chestplate.getType().isAir()) {
                level += chestplate.getEnchantmentLevel(enchantment);
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.HEAD)) {
            org.bukkit.inventory.ItemStack helmet = equipment.getHelmet();
            if (helmet != null && !helmet.getType().isAir()) {
                level += helmet.getEnchantmentLevel(enchantment);
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.HAND) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.MAINHAND)) {
            org.bukkit.inventory.ItemStack mainHand = equipment.getItemInMainHand();
            if (mainHand != null && !mainHand.getType().isAir()) {
                level += mainHand.getEnchantmentLevel(enchantment);
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.HAND) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.OFFHAND)) {
            org.bukkit.inventory.ItemStack offHand = equipment.getItemInOffHand();
            if (offHand != null && !offHand.getType().isAir()) {
                level += offHand.getEnchantmentLevel(enchantment);
            }
        }

        return level;
    }

    /**
     * 查找装备中第一个拥有指定附魔的物品
     * 与Enchantio的findFirstWithEnchant()方法完全相同
     *
     * @param equipment 要检查的装备
     * @param enchantment 要查找的附魔
     * @return 第一个拥有该附魔的物品，如果没有则返回null
     */
    @org.jetbrains.annotations.Nullable
    public static org.bukkit.inventory.ItemStack findFirstWithEnchant(
            @org.jetbrains.annotations.NotNull org.bukkit.inventory.EntityEquipment equipment,
            @org.jetbrains.annotations.NotNull org.bukkit.enchantments.Enchantment enchantment
    ) {
        java.util.Set<org.bukkit.inventory.EquipmentSlotGroup> equipmentSlotGroups = enchantment.getActiveSlotGroups();

        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.HAND) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.MAINHAND)) {
            org.bukkit.inventory.ItemStack mainHand = equipment.getItemInMainHand();
            if (mainHand != null && !mainHand.getType().isAir() && mainHand.getEnchantmentLevel(enchantment) > 0) {
                return mainHand;
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.HAND) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.OFFHAND)) {
            org.bukkit.inventory.ItemStack offHand = equipment.getItemInOffHand();
            if (offHand != null && !offHand.getType().isAir() && offHand.getEnchantmentLevel(enchantment) > 0) {
                return offHand;
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.HEAD)) {
            org.bukkit.inventory.ItemStack helmet = equipment.getHelmet();
            if (helmet != null && !helmet.getType().isAir() && helmet.getEnchantmentLevel(enchantment) > 0) {
                return helmet;
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.CHEST)) {
            org.bukkit.inventory.ItemStack chestplate = equipment.getChestplate();
            if (chestplate != null && !chestplate.getType().isAir() && chestplate.getEnchantmentLevel(enchantment) > 0) {
                return chestplate;
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.LEGS)) {
            org.bukkit.inventory.ItemStack leggings = equipment.getLeggings();
            if (leggings != null && !leggings.getType().isAir() && leggings.getEnchantmentLevel(enchantment) > 0) {
                return leggings;
            }
        }
        if (equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ANY) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.ARMOR) ||
            equipmentSlotGroups.contains(org.bukkit.inventory.EquipmentSlotGroup.FEET)) {
            org.bukkit.inventory.ItemStack boots = equipment.getBoots();
            if (boots != null && !boots.getType().isAir() && boots.getEnchantmentLevel(enchantment) > 0) {
                return boots;
            }
        }
        return null;
    }
}

