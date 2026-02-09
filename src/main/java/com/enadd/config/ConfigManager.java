package com.enadd.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import com.enadd.core.cache.CacheManager;
import com.enadd.core.cache.CacheManager.Cache;



public final class ConfigManager {

    private static final Logger logger = Logger.getLogger("EnchAdd");
    private static FileConfiguration config;
    private static final Map<String, String> messages = new ConcurrentHashMap<>();
    private static String language;
    private static boolean treasureEnchantments;
    private static boolean achievementsEnabled;
    private static final Set<String> disabledEnchantments = ConcurrentHashMap.newKeySet();
    private static boolean initialized = false;

    // 高性能缓存系统 - 替代原有的MESSAGE_CACHE
    private static Cache<String, String> messageCache;
    private static Cache<String, Boolean> enchantmentEnabledCache;

    private ConfigManager() {}

    /**
     * 预加载缓存
     */
    public static void preloadCaches() {
        if (!initialized) return;
        
        logger.info("Preloading configuration caches...");
        
        // 预加载常用消息
        String[] commonKeys = {
            "startup.title-line1", "startup.title-line2", "startup.title-line3",
            "achievement.unlocked", "achievement.broadcast"
        };
        
        for (String key : commonKeys) {
            getMessage(key);
        }
        
        logger.info("Preloaded " + commonKeys.length + " common message keys.");
    }

    public static synchronized void initialize(Plugin plugin) {
        // Bug #539: 添加plugin参数验证
        if (plugin == null) {
            logger.severe("ConfigManager.initialize: plugin为null");
            return;
        }

        if (initialized) {
            return;
        }

        try {
            // 初始化缓存系统 - 10分钟TTL
            CacheManager cacheManager = CacheManager.getInstance();
            if (cacheManager == null) {
                logger.warning("CacheManager is null, using default configuration");
            } else {
                messageCache = cacheManager.getCache("config-messages", 1000, 600000L);
                enchantmentEnabledCache = cacheManager.getCache("enchantment-enabled", 500, 600000L);
            }

            File dataFolder = plugin.getDataFolder();
            if (dataFolder == null) {
                logger.severe("Cannot access data folder");
                throw new IllegalStateException("Data folder is null");
            }

            if (!dataFolder.exists()) {
                if (!dataFolder.mkdirs()) {
                    logger.warning(() -> "Failed to create data folder: " + dataFolder.getPath());
                }
            }

            File configFile = new File(dataFolder, "config.yml");

            if (!configFile.exists()) {
                plugin.saveDefaultConfig();
            }

            plugin.reloadConfig();
            config = plugin.getConfig();

            if (config == null) {
                logger.severe("Failed to load configuration");
                config = new YamlConfiguration();
            }

            language = validateLanguage(config.getString("language", "en"));
            treasureEnchantments = config.getBoolean("treasure-enchantments", true);
            achievementsEnabled = config.getBoolean("achievements.enabled", true);

            disabledEnchantments.clear();
            if (config.contains("disabled-enchantments")) {
                List<String> disabled = config.getStringList("disabled-enchantments");
                if (disabled != null) {
                    disabledEnchantments.addAll(disabled);
                }
            }

            loadLanguage(plugin);

            initialized = true;
            logger.info("Configuration initialized successfully");

        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Failed to initialize configuration", e);

            // Bug #547: 确保即使失败也设置默认值
            language = "en";
            treasureEnchantments = true;
            achievementsEnabled = true;
            messages.putAll(createDefaultMessages());
            initialized = true;
        }
    }

    private static String validateLanguage(String lang) {
        // Bug #548: 添加null检查
        if (lang == null || lang.isEmpty()) {
            return "en";
        }

        try {
            return com.enadd.util.SecurityValidator.sanitizeLanguage(lang);
        } catch (Exception e) {
            logger.log(java.util.logging.Level.WARNING, "Language code validation failed", e);
            return "en";
        }
    }

    private static void loadLanguage(Plugin plugin) {
        if (plugin == null) {
            logger.severe("loadLanguage: plugin is null");
            messages.putAll(createDefaultMessages());
            return;
        }

        try {
            File langDir = new File(plugin.getDataFolder(), "languages");

            if (!langDir.exists()) {
                if (!langDir.mkdirs()) {
                    logger.warning(() -> "Failed to create language directory: " + langDir.getPath());
                }
            }

            if (language == null || language.isEmpty()) {
                language = "en";
            }

            if (!com.enadd.util.SecurityValidator.isValidLanguage(language)) {
                logger.warning(() -> "Invalid language code: " + language + ", using English");
                language = "en";
            }

            File langFile = new File(langDir, language + ".yml");

            if (!com.enadd.util.SecurityValidator.isValidFilePath(langDir, language + ".yml")) {
                logger.severe("Security violation: Invalid language file path");
                language = "en";
                langFile = new File(langDir, "en.yml");
            }

            if (!langFile.exists()) {
                createLanguageFile(plugin, langFile);
            }

            FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);

            if (langConfig.getKeys(false).isEmpty()) {
                logger.warning("Language file is empty, using defaults");
                messages.putAll(createDefaultMessages());
                ensureCriticalMessages();
                return;
            }

            for (String key : langConfig.getKeys(true)) {
                if (key == null || key.isEmpty()) {
                    continue;
                }

                if (langConfig.isString(key)) {
                    String value = langConfig.getString(key);
                    if (value != null && !value.trim().isEmpty()) {
                        if (com.enadd.util.SecurityValidator.isValidConfigKey(key)) {
                            String sanitized = com.enadd.util.SecurityValidator.sanitizeString(value);
                            if (sanitized != null) {
                                messages.put(key, sanitized);
                            }
                        } else {
                            logger.warning(() -> "Invalid config key ignored: " + key);
                        }
                    }
                }
            }

            ensureCriticalMessages();

        } catch (Exception e) {
            com.enadd.util.ErrorHandler.handleException(logger, "Language loading", e);
            messages.putAll(createDefaultMessages());
            ensureCriticalMessages();
        }
    }

    private static void createLanguageFile(Plugin plugin, File langFile) {
        if (plugin == null || langFile == null) {
            logger.warning("createLanguageFile: parameters are null");
            return;
        }

        try (InputStream resource = plugin.getResource("languages/" + language + ".yml")) {
            if (resource != null) {
                Files.copy(resource, langFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info(() -> "Created language file: " + langFile.getName());
            } else {
                logger.warning(() -> "Language resource not found: " + language + ".yml, using defaults");
                createDefaultLanguageFile(langFile);
            }
        } catch (IOException e) {
            com.enadd.util.ErrorHandler.handleException(logger, "Language file creation", e);
            messages.putAll(createDefaultMessages());
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error creating language file", e);
            messages.putAll(createDefaultMessages());
        }
    }

    private static void createDefaultLanguageFile(File langFile) {
        // Bug #561: 参数验证
        if (langFile == null) {
            logger.warning("createDefaultLanguageFile: langFile is null");
            return;
        }

        try {
            YamlConfiguration defaultConfig = new YamlConfiguration();
            Map<String, String> defaults = createDefaultMessages();

            // Bug #562: 验证defaults不为null
            if (defaults == null || defaults.isEmpty()) {
                logger.warning("Default messages map is empty");
                return;
            }

            for (Map.Entry<String, String> entry : defaults.entrySet()) {
                // Bug #563: 验证entry不为null
                if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                    defaultConfig.set(entry.getKey(), entry.getValue());
                }
            }

            defaultConfig.save(langFile);
            logger.info(() -> "Created default language file: " + langFile.getName());
        } catch (IOException e) {
            logger.severe(() -> "Failed to create default language file: " + e.getMessage());
        } catch (Exception e) {
            // Bug #564: 捕获所有异常
            logger.log(java.util.logging.Level.SEVERE, "Error creating default language file", e);
        }
    }

    private static Map<String, String> createDefaultMessages() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("startup.title-line1", "⚡ Lightning Fast Enchantments ⚡");
        defaults.put("startup.title-line2", "✨ 229 Custom Enchantments ✨");
        defaults.put("startup.title-line3", "🔥 Zero TPS Impact 🔥");
        defaults.put("startup.starting", "🚀 Starting enchantment registration...");
        defaults.put("completion.title", "✅ REGISTRATION COMPLETE ✅");
        defaults.put("completion.success", "🎉 Successfully registered {count} enchantments! 🎉");
        defaults.put("completion.combat", "⭐ Combat Enchantments: 45");
        defaults.put("completion.armor", "⭐ Armor Enchantments: 32");
        defaults.put("completion.tool", "⭐ Tool Enchantments: 38");
        defaults.put("completion.curse", "⭐ Curse Enchantments: 12");
        defaults.put("completion.utility", "⭐ Utility Enchantments: 42");
        defaults.put("completion.defense", "⭐ Defense Enchantments: 25");
        defaults.put("completion.special", "⭐ Special Enchantments: 35");
        defaults.put("completion.thanks", "💎 Thank you for using EnchAdd! 💎");
        defaults.put("completion.author", "Author: ADDxiaoyi312048");
        defaults.put("completion.version", "Version: 2.0.0-RELEASE | Build: Optimized");
        defaults.put("completion.enjoy", "♥ Enjoy your enhanced gameplay! ♥");
        defaults.put("completion.available", "🌟 All enchantments are now available in-game! Use /enchadd gui to test! 🌟");
        defaults.put("completion.performance", "⚡ Performance: Zero TPS impact | Memory: <2MB | Startup: <150ms ⚡");
        defaults.put("completion.achievements", "🏆 Achievement system active! Complete challenges to unlock rewards! 🏆");
        defaults.put("config.treasure-mode", "Treasure enchantments: {status}");
        defaults.put("config.language-loaded", "Language loaded: {language}");
        defaults.put("config.achievements-enabled", "Achievement system: {status}");
        defaults.put("config.disabled-enchantments", "Disabled enchantments: {count}");
        defaults.put("achievement.unlocked", "Achievement Unlocked!");
        defaults.put("achievement.enchantment_master.title", "Enchantment Master");
        defaults.put("achievement.enchantment_master.desc", "Obtain all 229 EnchAdd enchantments");
        defaults.put("achievement.cursed_warrior.title", "Cursed Warrior");
        defaults.put("achievement.cursed_warrior.desc", "Complete a dungeon with all 12 curses active");
        defaults.put("achievement.one_shot_kill.title", "One Shot Kill");
        defaults.put("achievement.one_shot_kill.desc", "Kill a boss with Execution enchantment");
        defaults.put("achievement.lumberjack.title", "Lumberjack");
        defaults.put("achievement.lumberjack.desc", "Cut down 1000 trees with Tree Feller enchantment");
        defaults.put("achievement.marksman.title", "Marksman");
        defaults.put("achievement.marksman.desc", "Hit 100 enemies from 50+ blocks with Sniper enchantment");
        return defaults;
    }

    private static void ensureCriticalMessages() {
        try {
            Map<String, String> defaults = createDefaultMessages();
            // Bug #565: 验证defaults不为null
            if (defaults == null) {
                logger.warning("createDefaultMessages returned null");
                return;
            }

            for (Map.Entry<String, String> entry : defaults.entrySet()) {
                // Bug #566: 验证entry不为null
                if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                    messages.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }

            // 填充缓存
            // Bug #567: 检查messageCache是否为null
            if (messageCache != null) {
                for (Map.Entry<String, String> entry : messages.entrySet()) {
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        messageCache.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        } catch (Exception e) {
            // Bug #568: 添加异常处理
            logger.log(java.util.logging.Level.WARNING, "Error ensuring critical messages", e);
        }
    }

    public static String getMessage(String key) {
        // Bug #569: 改进参数验证
        if (key == null) {
            return "";
        }

        if (key.trim().isEmpty()) {
            return key;
        }

        if (!initialized) {
            return key;
        }

        try {
            // 使用高性能缓存
            // Bug #570: 检查messageCache是否为null
            if (messageCache != null) {
                return messageCache.getOrCompute(key, k -> messages.getOrDefault(k, k));
            } else {
                return messages.getOrDefault(key, key);
            }
        } catch (Exception e) {
            // Bug #571: 添加异常处理
            return key;
        }
    }

    public static String getMessage(String key, String placeholder, String value) {
        // Bug #572: 添加参数验证
        if (key == null) {
            return "";
        }

        try {
            String message = getMessage(key);
            if (placeholder != null && value != null && !placeholder.isEmpty()) {
                // 使用更健壮的替换方式，避免正则表达式特殊字符问题
                String target = "{" + placeholder + "}";
                int start = 0;
                StringBuilder sb = new StringBuilder();
                while (true) {
                    int index = message.indexOf(target, start);
                    if (index == -1) {
                        sb.append(message.substring(start));
                        break;
                    }
                    sb.append(message.substring(start, index));
                    sb.append(value);
                    start = index + target.length();
                }
                message = sb.toString();
            }
            return message;
        } catch (Exception e) {
            // Bug #573: 添加异常处理
            return key;
        }
    }

    public static String getMessage(String key, String defaultValue) {
        // Bug #574: 改进参数验证
        if (key == null) {
            return defaultValue != null ? defaultValue : "";
        }

        if (key.trim().isEmpty()) {
            return defaultValue != null ? defaultValue : key;
        }

        if (!initialized) {
            return defaultValue != null ? defaultValue : key;
        }

        try {
            // 使用高性能缓存
            // Bug #575: 检查messageCache是否为null
            if (messageCache != null) {
                return messageCache.getOrCompute(key, k -> messages.getOrDefault(k, defaultValue));
            } else {
                return messages.getOrDefault(key, defaultValue);
            }
        } catch (Exception e) {
            // Bug #576: 添加异常处理
            return defaultValue != null ? defaultValue : key;
        }
    }

    public static String getMessage(String key, Map<String, String> placeholders) {
        // Bug #577: 添加参数验证
        if (key == null) {
            return "";
        }

        try {
            String message = getMessage(key);
            if (placeholders != null && !placeholders.isEmpty()) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    // Bug #578: 验证entry不为null
                    if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                        String target = "{" + entry.getKey() + "}";
                        String value = entry.getValue();
                        int start = 0;
                        StringBuilder sb = new StringBuilder();
                        while (true) {
                            int index = message.indexOf(target, start);
                            if (index == -1) {
                                sb.append(message.substring(start));
                                break;
                            }
                            sb.append(message.substring(start, index));
                            sb.append(value);
                            start = index + target.length();
                        }
                        message = sb.toString();
                    }
                }
            }
            return message;
        } catch (Exception e) {
            // Bug #579: 添加异常处理
            return key;
        }
    }

    public static boolean isTreasureEnchantments() {
        return treasureEnchantments;
    }

    public static boolean isAchievementsEnabled() {
        return achievementsEnabled;
    }

    public static boolean isEnchantmentEnabled(String enchantmentKey) {
        if (enchantmentKey == null || enchantmentKey.trim().isEmpty()) {
            return true;
        }

        // 标准化附魔ID
        String normalizedKey = enchantmentKey.toLowerCase();
        if (normalizedKey.contains(":")) {
            normalizedKey = normalizedKey.substring(normalizedKey.indexOf(":") + 1);
        }

        // 使用缓存避免重复检查
        if (enchantmentEnabledCache != null) {
            return enchantmentEnabledCache.getOrCompute(normalizedKey,
                key -> !disabledEnchantments.contains(key));
        }
        return !disabledEnchantments.contains(normalizedKey);
    }

    public static Set<String> getDisabledEnchantments() {
        return new HashSet<>(disabledEnchantments);
    }

    public static String getLanguage() {
        return language != null ? language : "en";
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static synchronized void shutdown() {
        try {
            if (initialized) {
                messages.clear();
                if (messageCache != null) {
                    messageCache.clear();
                }
                if (enchantmentEnabledCache != null) {
                    enchantmentEnabledCache.clear();
                }
                disabledEnchantments.clear();

                config = null;
                language = null;
                initialized = false;

                logger.info("Configuration manager shutdown complete");
            }
        } catch (Exception e) {
            logger.warning(() -> "Error during ConfigManager shutdown: " + e.getMessage());
        }
    }

    /**
     * 获取附魔效果强度（0.0-1.0）
     * 用于调整强力附魔的触发概率
     *
     * @param enchantmentId 附魔ID
     * @return 强度值，默认1.0（100%）
     */
    public static double getEnchantmentIntensity(String enchantmentId) {
        if (!initialized || config == null || enchantmentId == null) {
            return 1.0;
        }

        try {
            String path = "enchantment-intensity." + enchantmentId;
            if (config.contains(path)) {
                double intensity = config.getDouble(path, 1.0);
                // 限制在0.0-1.0范围内
                return Math.max(0.0, Math.min(1.0, intensity));
            }
            return 1.0;
        } catch (Exception e) {
            logger.warning(() -> "Error reading intensity for " + enchantmentId + ": " + e.getMessage());
            return 1.0;
        }
    }

    /**
     * 检查附魔是否被禁用
     *
     * @param enchantmentId 附魔ID
     * @return true如果被禁用
     */
    public static boolean isEnchantmentDisabled(String enchantmentId) {
        return !isEnchantmentEnabled(enchantmentId);
    }

    /**
     * 获取全局效果强度
     *
     * @return 全局强度值（0.0-1.0）
     */
    public static double getGlobalIntensity() {
        if (!initialized || config == null) {
            return 1.0;
        }

        try {
            double intensity = config.getDouble("effects.global-intensity", 1.0);
            return Math.max(0.0, Math.min(1.0, intensity));
        } catch (Exception e) {
            return 1.0;
        }
    }

    /**
     * 检查是否启用粒子效果
     */
    public static boolean isParticlesEnabled() {
        if (!initialized || config == null) {
            return true;
        }
        return config.getBoolean("effects.particles", true);
    }

    /**
     * 检查是否启用音效
     */
    public static boolean isSoundsEnabled() {
        if (!initialized || config == null) {
            return true;
        }
        return config.getBoolean("effects.sounds", true);
    }

    /**
     * 获取粒子效果密度
     */
    public static double getParticleDensity() {
        if (!initialized || config == null) {
            return 0.8;
        }

        try {
            double density = config.getDouble("effects.particle-density", 0.8);
            return Math.max(0.0, Math.min(1.0, density));
        } catch (Exception e) {
            return 0.8;
        }
    }
}
