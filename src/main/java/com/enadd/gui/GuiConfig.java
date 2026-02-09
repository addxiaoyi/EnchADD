package com.enadd.gui;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public final class GuiConfig {

    private final JavaPlugin plugin;
    private final File configFile;
    private YamlConfiguration config;

    private static final String DEFAULT_CONFIG =
        "# EnCh Add GUI 配置文件\n" +
        "# 为附魔管理菜单提供自定义选项\n\n" +
        "gui:\n" +
        "  # GUI标题\n" +
        "  title: \"&5&l附魔与诅咒管理 &7| &fEnCh Add\"\n" +
        "  \n" +
        "  # GUI行数 (4-6)\n" +
        "  rows: 6\n" +
        "  \n" +
        "  # 每页显示附魔数量\n" +
        "  items-per-page: 28\n" +
        "  \n" +
        "  # 主题颜色\n" +
        "  colors:\n" +
        "    background: \"BLACK\"\n" +
        "    border: \"PURPLE\"\n" +
        "    highlight: \"GOLD\"\n" +
        "    \n" +
        "  # 稀有度颜色\n" +
        "  rarity-colors:\n" +
        "    legendary: \"GOLD\"\n" +
        "    epic: \"DARK_PURPLE\"\n" +
        "    rare: \"BLUE\"\n" +
        "    uncommon: \"GREEN\"\n" +
        "    common: \"WHITE\"\n" +
        "    \n" +
        "  # 按钮文本\n" +
        "  buttons:\n" +
        "    previous-page: \"&e&l← 上一页\"\n" +
        "    next-page: \"&e&l下一页 →\"\n" +
        "    clear-filters: \"&c&l清除筛选\"\n" +
        "    rarity-filter: \"&6&l按稀有度\"\n" +
        "    sort: \"&b&l排序方式\"\n" +
        "    search: \"&a&l🔍 搜索\"\n" +
        "    info-panel: \"&5&lℹ 信息面板\"\n" +
        "    \n" +
        "  # 界面元素\n" +
        "  elements:\n" +
        "    show-lore: true\n" +
        "    show-conflicts: true\n" +
        "    show-rarity: true\n" +
        "    show-weight: true\n" +
        "    enable-glow: true\n" +
        "    enable-animations: true\n" +
        "\n" +
        "messages:\n" +
        "  command-only-player: \"&c该命令只能由玩家执行\"\n" +
        "  no-permission: \"&c你没有权限使用此命令\"\n" +
        "  search-prompt: \"&a请在聊天框输入搜索关键词...\"\n" +
        "  no-results: \"&c未找到匹配的附魔\"\n" +
        "  enchantment-details: \"&6=== 附魔详情 ===\"\n" +
        "  \n" +
        "# 分类筛选配置\n" +
        "categories:\n" +
        "  weapon:\n" +
        "    enabled: true\n" +
        "    name: \"&c武器\"\n" +
        "    icon: \"DIAMOND_SWORD\"\n" +
        "  armor:\n" +
        "    enabled: true\n" +
        "    name: \"&9护甲\"\n" +
        "    icon: \"DIAMOND_CHESTPLATE\"\n" +
        "  tool:\n" +
        "    enabled: true\n" +
        "    name: \"&e工具\"\n" +
        "    icon: \"DIAMOND_PICKAXE\"\n" +
        "  bow:\n" +
        "    enabled: true\n" +
        "    name: \"&a弓\"\n" +
        "    icon: \"BOW\"\n" +
        "  trident:\n" +
        "    enabled: true\n" +
        "    name: \"&b三叉戟\"\n" +
        "    icon: \"TRIDENT\"\n" +
        "  crossbow:\n" +
        "    enabled: true\n" +
        "    name: \"&5十字弓\"\n" +
        "    icon: \"CROSSBOW\"\n" +
        "  fishing_rod:\n" +
        "    enabled: true\n" +
        "    name: \"&3钓鱼竿\"\n" +
        "    icon: \"FISHING_ROD\"\n" +
        "  utility:\n" +
        "    enabled: true\n" +
        "    name: \"&f通用\"\n" +
        "    icon: \"ENCHANTED_BOOK\"\n" +
        "\n" +
        "# 性能配置\n" +
        "performance:\n" +
        "  # 最大加载时间(ms)\n" +
        "  max-load-time: 200\n" +
        "  # 启用缓存\n" +
        "  enable-cache: true\n" +
        "  # 缓存过期时间(秒)\n" +
        "  cache-expire-time: 300\n" +
        "\n" +
        "# 兼容性配置\n" +
        "compatibility:\n" +
        "  # 支持的插件前缀\n" +
        "  supported-plugins:\n" +
        "    - \"PlaceholderAPI\"\n" +
        "    - \"Vault\"\n" +
        "  # 字体缩放支持\n" +
        "  font-scale-support: true\n" +
        "  # 高分辨率支持\n" +
        "  high-resolution-support: true\n";

    public GuiConfig(JavaPlugin plugin) {
        // Bug #382: 验证plugin参数
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin不能为null");
        }

        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "gui.yml");

        // Bug #383: 验证configFile创建成功
        if (this.configFile == null) {
            throw new IllegalStateException("无法创建配置文件对象");
        }

        loadConfig();
    }

    private void loadConfig() {
        try {
            // Bug #384: 检查configFile是否为null
            if (configFile == null) {
                plugin.getLogger().severe("配置文件对象为null");
                config = new YamlConfiguration();
                return;
            }

            if (!configFile.exists()) {
                // Bug #385: 检查getDataFolder()返回值
                File dataFolder = plugin.getDataFolder();
                if (dataFolder == null) {
                    plugin.getLogger().severe("无法获取数据文件夹");
                    config = new YamlConfiguration();
                    return;
                }

                // Bug #386: 检查mkdirs()返回值
                if (!dataFolder.exists() && !dataFolder.mkdirs()) {
                    plugin.getLogger().warning("无法创建数据文件夹: " + dataFolder.getPath());
                }

                // Bug #387: 检查DEFAULT_CONFIG是否为null
                if (DEFAULT_CONFIG == null || DEFAULT_CONFIG.isEmpty()) {
                    plugin.getLogger().warning("默认配置为空");
                    config = new YamlConfiguration();
                    return;
                }

                // Bug #388: 检查文件写入是否成功
                try {
                    Files.write(configFile.toPath(), DEFAULT_CONFIG.getBytes());
                    plugin.getLogger().info("已创建默认GUI配置文件");
                } catch (IOException e) {
                    plugin.getLogger().severe("无法写入默认配置: " + e.getMessage());
                    config = new YamlConfiguration();
                    return;
                }
            }

            // Bug #389: 检查loadConfiguration返回值
            config = YamlConfiguration.loadConfiguration(configFile);
            if (config == null) {
                plugin.getLogger().warning("配置加载返回null，使用空配置");
                config = new YamlConfiguration();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("加载GUI配置文件时出错: " + e.getMessage());
            e.printStackTrace();
            config = new YamlConfiguration();
        }
    }

    public void reload() {
        // Bug #390: 添加异常处理
        try {
            loadConfig();
            plugin.getLogger().info("GUI配置已重载");
        } catch (Exception e) {
            plugin.getLogger().severe("重载GUI配置时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getGuiTitle() {
        // Bug #391: 检查config是否为null
        if (config == null) {
            return "&5&l附魔与诅咒管理";
        }

        try {
            String title = config.getString("gui.title", "&5&l附魔与诅咒管理");
            // Bug #392: 检查返回值是否为null
            return title != null ? title : "&5&l附魔与诅咒管理";
        } catch (Exception e) {
            plugin.getLogger().warning("获取GUI标题时出错: " + e.getMessage());
            return "&5&l附魔与诅咒管理";
        }
    }

    public int getGuiRows() {
        // Bug #393: 检查config是否为null
        if (config == null) {
            return 6;
        }

        try {
            int rows = config.getInt("gui.rows", 6);
            // Bug #394: 更严格的范围验证
            return Math.min(6, Math.max(4, rows));
        } catch (Exception e) {
            plugin.getLogger().warning("获取GUI行数时出错: " + e.getMessage());
            return 6;
        }
    }

    public int getItemsPerPage() {
        // Bug #395: 检查config是否为null
        if (config == null) {
            return 28;
        }

        try {
            int items = config.getInt("gui.items-per-page", 28);
            // Bug #396: 更严格的范围验证
            return Math.min(28, Math.max(10, items));
        } catch (Exception e) {
            plugin.getLogger().warning("获取每页物品数时出错: " + e.getMessage());
            return 28;
        }
    }

    public String getPreviousPageButton() {
        // Bug #397: 添加null检查和异常处理
        if (config == null) return "&e&l← 上一页";
        try {
            String text = config.getString("gui.buttons.previous-page", "&e&l← 上一页");
            return text != null ? text : "&e&l← 上一页";
        } catch (Exception e) {
            return "&e&l← 上一页";
        }
    }

    public String getNextPageButton() {
        // Bug #398: 添加null检查和异常处理
        if (config == null) return "&e&l下一页 →";
        try {
            String text = config.getString("gui.buttons.next-page", "&e&l下一页 →");
            return text != null ? text : "&e&l下一页 →";
        } catch (Exception e) {
            return "&e&l下一页 →";
        }
    }

    public String getClearFiltersButton() {
        // Bug #399: 添加null检查和异常处理
        if (config == null) return "&c&l清除筛选";
        try {
            String text = config.getString("gui.buttons.clear-filters", "&c&l清除筛选");
            return text != null ? text : "&c&l清除筛选";
        } catch (Exception e) {
            return "&c&l清除筛选";
        }
    }

    public String getRarityFilterButton() {
        // Bug #400: 添加null检查和异常处理
        if (config == null) return "&6&l按稀有度";
        try {
            String text = config.getString("gui.buttons.rarity-filter", "&6&l按稀有度");
            return text != null ? text : "&6&l按稀有度";
        } catch (Exception e) {
            return "&6&l按稀有度";
        }
    }

    public String getSortButton() {
        // Bug #401: 添加null检查和异常处理
        if (config == null) return "&b&l排序方式";
        try {
            String text = config.getString("gui.buttons.sort", "&b&l排序方式");
            return text != null ? text : "&b&l排序方式";
        } catch (Exception e) {
            return "&b&l排序方式";
        }
    }

    public String getSearchButton() {
        // Bug #402: 添加null检查和异常处理
        if (config == null) return "&a&l🔍 搜索";
        try {
            String text = config.getString("gui.buttons.search", "&a&l🔍 搜索");
            return text != null ? text : "&a&l🔍 搜索";
        } catch (Exception e) {
            return "&a&l🔍 搜索";
        }
    }

    public String getInfoPanelTitle() {
        // Bug #403: 添加null检查和异常处理
        if (config == null) return "&5&lℹ 信息面板";
        try {
            String text = config.getString("gui.elements.info-panel", "&5&lℹ 信息面板");
            return text != null ? text : "&5&lℹ 信息面板";
        } catch (Exception e) {
            return "&5&lℹ 信息面板";
        }
    }

    public String getMessage(String key) {
        // Bug #404: 添加key参数验证
        if (key == null || key.isEmpty()) {
            plugin.getLogger().warning("getMessage: key为null或空");
            return "";
        }

        // Bug #405: 添加null检查和异常处理
        if (config == null) return "";
        try {
            String message = config.getString("messages." + key, "");
            return message != null ? message : "";
        } catch (Exception e) {
            plugin.getLogger().warning("获取消息时出错，key=" + key + ": " + e.getMessage());
            return "";
        }
    }

    public boolean isLoreEnabled() {
        // Bug #406: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("gui.elements.show-lore", true);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isConflictsEnabled() {
        // Bug #407: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("gui.elements.show-conflicts", true);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isRarityEnabled() {
        // Bug #408: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("gui.elements.show-rarity", true);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isWeightEnabled() {
        // Bug #409: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("gui.elements.show-weight", true);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isGlowEnabled() {
        // Bug #410: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("gui.elements.enable-glow", true);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isAnimationsEnabled() {
        // Bug #411: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("gui.elements.enable-animations", true);
        } catch (Exception e) {
            return true;
        }
    }

    public int getMaxLoadTime() {
        // Bug #412: 添加null检查和异常处理
        if (config == null) return 200;
        try {
            int time = config.getInt("performance.max-load-time", 200);
            // Bug #413: 验证范围
            return Math.max(50, Math.min(5000, time));
        } catch (Exception e) {
            return 200;
        }
    }

    public boolean isCacheEnabled() {
        // Bug #414: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("performance.enable-cache", true);
        } catch (Exception e) {
            return true;
        }
    }

    public int getCacheExpireTime() {
        // Bug #415: 添加null检查和异常处理
        if (config == null) return 300;
        try {
            int time = config.getInt("performance.cache-expire-time", 300);
            // Bug #416: 验证范围
            return Math.max(60, Math.min(3600, time));
        } catch (Exception e) {
            return 300;
        }
    }

    public boolean isCategoryEnabled(String category) {
        // Bug #417: 添加category参数验证
        if (category == null || category.isEmpty()) {
            plugin.getLogger().warning("isCategoryEnabled: category为null或空");
            return true;
        }

        // Bug #418: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("categories." + category + ".enabled", true);
        } catch (Exception e) {
            plugin.getLogger().warning("检查分类启用状态时出错，category=" + category);
            return true;
        }
    }

    public String getCategoryName(String category) {
        // Bug #419: 添加category参数验证
        if (category == null || category.isEmpty()) {
            plugin.getLogger().warning("getCategoryName: category为null或空");
            return "未知";
        }

        // Bug #420: 添加null检查和异常处理
        if (config == null) return category;
        try {
            String name = config.getString("categories." + category + ".name", category);
            return name != null ? name : category;
        } catch (Exception e) {
            plugin.getLogger().warning("获取分类名称时出错，category=" + category);
            return category;
        }
    }

    public String getCategoryIcon(String category) {
        // Bug #421: 添加category参数验证
        if (category == null || category.isEmpty()) {
            plugin.getLogger().warning("getCategoryIcon: category为null或空");
            return "BOOK";
        }

        // Bug #422: 添加null检查和异常处理
        if (config == null) return "BOOK";
        try {
            String icon = config.getString("categories." + category + ".icon", "BOOK");
            return icon != null ? icon : "BOOK";
        } catch (Exception e) {
            plugin.getLogger().warning("获取分类图标时出错，category=" + category);
            return "BOOK";
        }
    }

    public List<String> getSupportedPlugins() {
        // Bug #423: 添加null检查和异常处理
        if (config == null) return new ArrayList<>();
        try {
            List<String> plugins = config.getStringList("compatibility.supported-plugins");
            // Bug #424: 检查返回值是否为null
            return plugins != null ? plugins : new ArrayList<>();
        } catch (Exception e) {
            plugin.getLogger().warning("获取支持的插件列表时出错");
            return new ArrayList<>();
        }
    }

    public boolean isFontScaleSupported() {
        // Bug #425: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("compatibility.font-scale-support", true);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isHighResolutionSupported() {
        // Bug #426: 添加null检查和异常处理
        if (config == null) return true;
        try {
            return config.getBoolean("compatibility.high-resolution-support", true);
        } catch (Exception e) {
            return true;
        }
    }
}
