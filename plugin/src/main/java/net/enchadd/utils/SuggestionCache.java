package net.enchadd.utils;

import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 优化6：命令补全列表预生成（Suggestion Cache）
 *
 * 在 EnchADDConfig 初始化完成后（附魔全部加载后），
 * 预先生成附魔 Key 的建议列表（完整形式 "enchadd:xxx" + 简写形式 "xxx"），
 * 按字母排序以获得最佳用户体验。
 *
 * 避免每次玩家输入 Tab 补全时都遍历 ENCHANTS.keySet()，
 * 将 O(n) 实时遍历转为 O(1) 列表返回。
 */
public class SuggestionCache {

    /** 完整 Key 补全列表（"enchadd:vampirism" 等）*/
    private static List<String> fullKeys = Collections.emptyList();

    /** 简写补全列表（"vampirism" 等，无命名空间）*/
    private static List<String> shortKeys = Collections.emptyList();

    /** 合并列表（全量，含完整 + 简写）*/
    private static List<String> allKeys = Collections.emptyList();

    private SuggestionCache() {}

    /**
     * 在所有附魔加载完毕后调用（EnchADDConfig.init() 末尾或 onEnable 末尾），
     * 生成并缓存所有补全列表。
     */
    public static void build() {
        List<String> full = new ArrayList<>();
        List<String> shortList = new ArrayList<>();

        for (var entry : EnchADDConfig.ENCHANTS.entrySet()) {
            String fullKey = entry.getKey().asString();
            full.add(fullKey);
            String shortKey = fullKey.contains(":") ? fullKey.substring(fullKey.indexOf(':') + 1) : fullKey;
            shortList.add(shortKey);
        }

        Collections.sort(full);
        Collections.sort(shortList);

        List<String> all = new ArrayList<>(full.size() + shortList.size());
        all.addAll(full);
        all.addAll(shortList);
        Collections.sort(all);

        fullKeys = Collections.unmodifiableList(full);
        shortKeys = Collections.unmodifiableList(shortList);
        allKeys = Collections.unmodifiableList(all);
    }

    /**
     * 获取所有 "namespace:value" 形式的 Key 列表（已排序）。
     */
    public static List<String> getFullKeys() {
        return fullKeys;
    }

    /**
     * 获取所有简写 Key 列表（已排序）。
     */
    public static List<String> getShortKeys() {
        return shortKeys;
    }

    /**
     * 获取完整 + 简写合并列表（已排序），适用于 Tab 补全。
     */
    public static List<String> getAllKeys() {
        return allKeys;
    }

    /**
     * 根据输入前缀过滤建议列表（适用于增量补全场景）。
     *
     * @param input 当前输入
     * @return 匹配的建议列表
     */
    public static List<String> filter(String input) {
        if (input == null || input.isEmpty()) return allKeys;
        String lower = input.toLowerCase();
        List<String> result = new ArrayList<>();
        for (String key : allKeys) {
            if (key.startsWith(lower)) result.add(key);
        }
        return result;
    }

    /**
     * 重新构建缓存（热重载时使用）。
     */
    public static void invalidate() {
        fullKeys = Collections.emptyList();
        shortKeys = Collections.emptyList();
        allKeys = Collections.emptyList();
    }
}
