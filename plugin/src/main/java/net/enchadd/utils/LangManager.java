package net.enchadd.utils;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationStore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 优化1：国际化资源管理器（i18n Resource Manager）
 *
 * 支持从插件数据目录的 languages/ 文件夹加载语言文件，
 * 内置 zh_CN / en_US 两套资源（打包在 jar 中作为默认值），
 * 允许服务器管理员通过插件目录中的同名文件覆盖翻译。
 *
 * 设计特点：
 * 1. 自动提取内置语言文件到插件目录（首次启动时）
 * 2. 支持 ResourceBundle 风格的 getString()
 * 3. 存储使用 ConcurrentHashMap（线程安全，支持热更新）
 * 4. 备用链：自定义文件 → JAR 内置 → key 本身（最终兜底）
 */
public class LangManager {

    /** 当前激活的语言代码（"zh" 或 "en"） */
    private static String activeLang = "zh";

    /** 已加载的翻译条目（key → 翻译文本） */
    private static final Map<String, String> translations = new ConcurrentHashMap<>();
    /** 已注册到 Adventure TranslationStore 的翻译 key（用于 reload/shutdown 时清理） */
    private static final Set<String> registeredRegistryKeys = ConcurrentHashMap.newKeySet();

    /** 支持的语言列表 */
    private static final List<String> SUPPORTED_LANGS = List.of("zh", "en");

    /** 语言文件对应的资源文件名 */
    private static final Map<String, String> LANG_FILE_MAP = Map.of(
            "zh", "enchadd_zh_CN.properties",
            "en", "enchadd_en_US.properties"
    );

    /** Adventure 全局翻译注册表（仅注册一次） */
    private static final TranslationStore<MessageFormat> REGISTRY =
            TranslationStore.messageFormat(Key.key("enchadd", "translations"));
    private static boolean registryRegistered = false;

    private LangManager() {}

    /**
     * 初始化语言管理器。
     * 自动从 JAR 提取语言文件到插件目录，然后加载指定语言。
     *
     * @param pluginDataDir 插件数据目录（getDataFolder().toPath()）
     * @param lang          语言代码（"zh" 或 "en"）
     */
    public static synchronized void init(Path pluginDataDir, String lang) {
        activeLang = SUPPORTED_LANGS.contains(lang) ? lang : "zh";
        Path langDir = pluginDataDir.resolve("languages");

        // 提取内置语言文件（如果不存在）
        for (Map.Entry<String, String> entry : LANG_FILE_MAP.entrySet()) {
            extractIfAbsent(pluginDataDir, langDir, entry.getValue());
        }

        // 注册 Adventure 全局翻译源
        ensureRegistryRegistered();
        clearRegistryTranslations();

        // 为了确保 zh/en 兜底链稳定，始终加载全部支持语言
        translations.clear();
        for (String supportedLang : SUPPORTED_LANGS) {
            load(langDir, supportedLang);
        }
    }

    /**
     * 获取翻译文本。
     * 查找顺序：当前语言 → en 兜底 → 返回 key 本身
     *
     * @param key 翻译 key（如 "EnchADD.enchant.vampirism"）
     * @return 翻译文本
     */
    public static String get(String key) {
        String result = translations.get(activeLang + "." + key);
        if (result == null) result = translations.get("en." + key);
        return result != null ? result : key;
    }

    /**
     * 从 TranslatableComponent 中解析翻译文本。
     *
     * @param component Adventure Component
     * @return 翻译文本，未命中则返回 fallback 或 key
     */
    public static String resolve(Component component) {
        if (component instanceof TranslatableComponent tc) {
            String key = tc.key();
            String result = get(key);
            if (result.equals(key) && tc.fallback() != null && !tc.fallback().isEmpty()) {
                return tc.fallback();
            }
            return result;
        }
        return "";
    }

    /**
     * 获取当前激活的语言代码。
     */
    public static String getActiveLang() {
        return activeLang;
    }

    /**
     * 热重载：重新从磁盘加载语言文件。
     *
     * @param pluginDataDir 插件数据目录
     */
    public static synchronized void reload(Path pluginDataDir) {
        clearRegistryTranslations();
        translations.clear();
        Path langDir = pluginDataDir.resolve("languages");
        for (String lang : SUPPORTED_LANGS) {
            load(langDir, lang);
        }
    }

    /**
     * 在插件停用时解除全局翻译源，避免热重载场景的翻译源累积。
     */
    public static synchronized void shutdown() {
        clearRegistryTranslations();
        translations.clear();
        if (!registryRegistered) {
            return;
        }
        GlobalTranslator.translator().removeSource(REGISTRY);
        registryRegistered = false;
    }

    // ─── 私有方法 ─────────────────────────────────────────────────────────────────

    private static void load(Path langDir, String lang) {
        String fileName = LANG_FILE_MAP.get(lang);
        if (fileName == null) return;

        Path file = langDir.resolve(fileName);
        Properties props = new Properties();

        // 先从 JAR 内部加载（基础翻译）
        try (InputStream jarStream = LangManager.class.getClassLoader().getResourceAsStream(fileName)) {
            if (jarStream != null) {
                props.load(new InputStreamReader(jarStream, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {}

        // 再从磁盘文件加载（覆盖 JAR 内的翻译，实现自定义）
        if (Files.exists(file)) {
            try (InputStream diskStream = Files.newInputStream(file)) {
                Properties override = new Properties();
                override.load(new InputStreamReader(diskStream, StandardCharsets.UTF_8));
                props.putAll(override);
            } catch (IOException ignored) {}
        }

        // 写入缓存（以 lang. 为前缀避免多语言串扰）
        Locale locale = localeOf(lang);
        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            translations.put(lang + "." + key, value);
            registerTranslation(key, locale, value);

            // 兼容映射：把 EnchADD.enchant.xxx 自动映射为 enchantment.enchadd.xxx
            if (key.startsWith("EnchADD.enchant.")) {
                String suffix = key.substring("EnchADD.enchant.".length());
                String minecraftKey = "enchantment.enchadd." + suffix;
                if (!props.containsKey(minecraftKey)) {
                    translations.put(lang + "." + minecraftKey, value);
                    registerTranslation(minecraftKey, locale, value);
                }
            }
        }
    }

    private static void extractIfAbsent(Path dataDir, Path langDir, String fileName) {
        Path target = langDir.resolve(fileName);
        if (Files.exists(target)) return;
        try {
            Files.createDirectories(langDir);
            try (InputStream src = LangManager.class.getClassLoader().getResourceAsStream(fileName)) {
                if (src != null) {
                    Files.copy(src, target);
                }
            }
        } catch (IOException ignored) {}
    }

    private static void ensureRegistryRegistered() {
        if (registryRegistered) return;
        GlobalTranslator.translator().addSource(REGISTRY);
        registryRegistered = true;
    }

    private static void registerTranslation(String key, Locale locale, String value) {
        REGISTRY.register(key, locale, new MessageFormat(value, locale));
        registeredRegistryKeys.add(key);
    }

    private static void clearRegistryTranslations() {
        if (registeredRegistryKeys.isEmpty()) {
            return;
        }
        for (String key : new ArrayList<>(registeredRegistryKeys)) {
            try {
                REGISTRY.unregister(key);
            } catch (RuntimeException ignored) {
                // ignore stale entry cleanup issues to keep reload path resilient
            }
        }
        registeredRegistryKeys.clear();
    }

    private static Locale localeOf(String lang) {
        return "en".equals(lang) ? Locale.US : Locale.SIMPLIFIED_CHINESE;
    }
}
