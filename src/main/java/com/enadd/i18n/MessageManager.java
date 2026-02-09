package com.enadd.i18n;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * 国际化消息管理器
 * 解决所有硬编码中文问题，支持UTF-8编码
 *
 * 功能：
 * - 自动加载语言文件
 * - UTF-8编码支持
 * - 消息缓存
 * - 占位符替换
 * - 颜色代码支持
 */
public final class MessageManager {
    private static final Logger LOGGER = Logger.getLogger(MessageManager.class.getName());

    private static MessageManager instance;
    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> languageConfigs = new ConcurrentHashMap<>();
    private final Map<String, String> messageCache = new ConcurrentHashMap<>();
    private String currentLanguage = "en";

    private MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new MessageManager(plugin);
            instance.loadLanguages();
        }
    }

    public static MessageManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MessageManager not initialized");
        }
        return instance;
    }

    /**
     * 加载所有语言文件
     */
    private void loadLanguages() {
        String[] languages = {"en", "zh", "zh_tw", "de", "pt", "ms", "fr"};

        for (String lang : languages) {
            try {
                loadLanguage(lang);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load language: " + lang, e);
            }
        }

        LOGGER.info("Loaded " + languageConfigs.size() + " language files");
    }

    /**
     * 加载单个语言文件（UTF-8编码）
     */
    private void loadLanguage(String language) {
        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File langFile = new File(langFolder, language + ".yml");

        // 如果文件不存在，从资源复制
        if (!langFile.exists()) {
            plugin.saveResource("languages/" + language + ".yml", false);
        }

        try {
            // 使用UTF-8编码加载
            FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);

            // 加载默认值（从jar内部）
            InputStream defConfigStream = plugin.getResource("languages/" + language + ".yml");
            if (defConfigStream != null) {
                FileConfiguration defConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
                config.setDefaults(defConfig);
            }

            languageConfigs.put(language, config);
            LOGGER.info("Loaded language: " + language);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load language file: " + language, e);
        }
    }

    /**
     * 设置当前语言
     */
    public void setLanguage(String language) {
        if (languageConfigs.containsKey(language)) {
            this.currentLanguage = language;
            messageCache.clear(); // 清除缓存
            LOGGER.info("Language changed to: " + language);
        } else {
            LOGGER.warning("Language not found: " + language + ", using default");
        }
    }

    /**
     * 获取消息（带缓存）
     */
    public String getMessage(String key) {
        String cacheKey = currentLanguage + ":" + key;

        return messageCache.computeIfAbsent(cacheKey, k -> {
            FileConfiguration config = languageConfigs.get(currentLanguage);
            if (config == null) {
                config = languageConfigs.get("en"); // 回退到英语
            }

            if (config != null && config.contains(key)) {
                return colorize(config.getString(key));
            }

            return "§c[Missing: " + key + "]";
        });
    }

    /**
     * 获取消息并替换占位符
     */
    public String getMessage(String key, Object... replacements) {
        String message = getMessage(key);

        if (replacements.length % 2 != 0) {
            LOGGER.warning("Invalid replacements for key: " + key);
            return message;
        }

        for (int i = 0; i < replacements.length; i += 2) {
            String placeholder = "{" + replacements[i] + "}";
            String value = String.valueOf(replacements[i + 1]);
            message = message.replace(placeholder, value);
        }

        return message;
    }

    /**
     * 获取消息列表
     */
    public String[] getMessageList(String key) {
        FileConfiguration config = languageConfigs.get(currentLanguage);
        if (config == null) {
            config = languageConfigs.get("en");
        }

        if (config != null && config.contains(key)) {
            return config.getStringList(key).stream()
                .map(this::colorize)
                .toArray(String[]::new);
        }

        return new String[]{"§c[Missing: " + key + "]"};
    }

    /**
     * 颜色代码转换
     */
    private String colorize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('&', '§');
    }

    /**
     * 重新加载所有语言文件
     */
    public void reload() {
        languageConfigs.clear();
        messageCache.clear();
        loadLanguages();
    }

    /**
     * 获取当前语言
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * 获取所有可用语言
     */
    public String[] getAvailableLanguages() {
        return languageConfigs.keySet().toArray(new String[0]);
    }
}
