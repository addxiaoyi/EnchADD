package net.enchadd.hooks;

import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.utils.EnchantStats;
import net.kyori.adventure.key.Key;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

/**
 * 优化10：PlaceholderAPI 兼容钩子（PlaceholderAPI Hook）
 *
 * 当服务器安装了 PlaceholderAPI 时，自动注册 %enchadd_...% 系列变量，
 * 供计分板、TAB 显示、公告插件使用。
 *
 * 可用变量列表：
 * - %enchadd_count%               → 当前插件已加载的附魔总数
 * - %enchadd_stats_<key>%         → 指定附魔的本次会话触发次数（e.g. %enchadd_stats_vampirism%）
 * - %enchadd_exists_<key>%        → 指定附魔是否存在（true/false）
 * - %enchadd_maxlevel_<key>%      → 指定附魔的最大等级
 * - %enchadd_weight_<key>%        → 指定附魔的权重
 * - %enchadd_top_trigger%         → 触发次数最多的附魔名称
 *
 * 注意：此类只有在 PlaceholderAPI 存在时才会被加载（动态 Class.forName 检查）。
 * 不引入编译期依赖，通过反射安全导入。
 */
public class PlaceholderAPIHook {

    private static volatile boolean registered = false;
    private static volatile EnchADDExpansion expansion = null;

    private PlaceholderAPIHook() {}

    /**
     * 尝试注册 PlaceholderAPI 扩展。
     * 仅当服务器已安装且启用了 PlaceholderAPI 时才会注册。
     *
     * @param plugin 主插件实例
     * @return 是否成功注册
     */
    public static synchronized boolean tryRegister(JavaPlugin plugin) {
        if (registered) {
            return true;
        }
        Plugin placeholderApi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderApi == null || !placeholderApi.isEnabled()) {
            return false;
        }
        try {
            // 通过反射动态加载，防止编译期依赖
            Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion");
            EnchADDExpansion candidate = new EnchADDExpansion(plugin);
            if (!candidate.register()) {
                plugin.getLogger().warning("[EnchADD] PlaceholderAPI 钩子注册失败（register 返回 false）。");
                return false;
            }
            expansion = candidate;
            registered = true;
            plugin.getLogger().info("[EnchADD] PlaceholderAPI 钩子已注册 ✓");
            return true;
        } catch (ClassNotFoundException | LinkageError | RuntimeException e) {
            plugin.getLogger().warning("[EnchADD] PlaceholderAPI 钩子注册失败: " + e.getMessage());
            return false;
        }
    }

    public static synchronized void unregister(JavaPlugin plugin) {
        EnchADDExpansion current = expansion;
        expansion = null;
        registered = false;
        if (current == null) {
            return;
        }
        try {
            current.unregister();
            plugin.getLogger().info("[EnchADD] PlaceholderAPI 钩子已卸载。");
        } catch (RuntimeException e) {
            plugin.getLogger().warning("[EnchADD] PlaceholderAPI 钩子卸载失败: " + e.getMessage());
        }
    }

    public static synchronized void onPlaceholderApiDisabled(JavaPlugin plugin) {
        if (!registered && expansion == null) {
            return;
        }
        expansion = null;
        registered = false;
        plugin.getLogger().info("[EnchADD] 检测到 PlaceholderAPI 关闭，已重置钩子状态。");
    }

    public static boolean isRegistered() {
        return registered;
    }

    /**
     * 内部扩展实现（PlaceholderExpansion 子类）。
     *
     * 通过内部类延迟加载，确保 PlaceholderAPIHook 本身不触发
     * PlaceholderExpansion 的类加载（在 PAPI 不存在时安全）。
     */
    static class EnchADDExpansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {

        private final JavaPlugin plugin;

        EnchADDExpansion(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getIdentifier() { return "enchadd"; }

        @Override
        public String getAuthor() { return "EnchADD Team"; }

        @Override
        public String getVersion() { return plugin.getPluginMeta().getVersion(); }

        @Override
        public boolean persist() { return true; }

        @Override
        public String onRequest(OfflinePlayer player, String params) {
            String p = params.toLowerCase(Locale.ROOT);

            // %enchadd_count%
            if (p.equals("count")) {
                return String.valueOf(EnchADDConfig.ENCHANTS.size());
            }

            // %enchadd_top_trigger%
            if (p.equals("top_trigger")) {
                String topKey = null;
                long topCount = -1;
                for (var entry : EnchADDConfig.ENCHANTS.entrySet()) {
                    long c = EnchantStats.getCount(entry.getKey());
                    if (c > topCount) {
                        topCount = c;
                        topKey = entry.getKey().value();
                    }
                }
                return topKey != null ? topKey : "无";
            }

            // %enchadd_stats_<key>%
            if (p.startsWith("stats_")) {
                String keyStr = p.substring("stats_".length());
                Key key = resolveKey(keyStr);
                if (key == null) return "0";
                return String.valueOf(EnchantStats.getCount(key));
            }

            // %enchadd_exists_<key>%
            if (p.startsWith("exists_")) {
                String keyStr = p.substring("exists_".length());
                Key key = resolveKey(keyStr);
                return key != null ? "true" : "false";
            }

            // %enchadd_maxlevel_<key>%
            if (p.startsWith("maxlevel_")) {
                String keyStr = p.substring("maxlevel_".length());
                Key key = resolveKey(keyStr);
                if (key == null) return "0";
                EnchADDEnchant e = EnchADDConfig.ENCHANTS.get(key);
                return e != null ? String.valueOf(e.getMaxLevel()) : "0";
            }

            // %enchadd_weight_<key>%
            if (p.startsWith("weight_")) {
                String keyStr = p.substring("weight_".length());
                Key key = resolveKey(keyStr);
                if (key == null) return "0";
                EnchADDEnchant e = EnchADDConfig.ENCHANTS.get(key);
                return e != null ? String.valueOf(e.getWeight()) : "0";
            }

            return null;
        }

        /**
         * 将简写 key 或完整 key 解析为匹配的 Key 对象。
         */
        private Key resolveKey(String s) {
            // 尝试完整格式（先检查再获取）
            Key fullKey = Key.key(s);
            if (EnchADDConfig.ENCHANTS.containsKey(fullKey)) {
                return fullKey;
            }
            // 尝试加上默认命名空间
            Key candidate = Key.key("enchadd", s);
            return EnchADDConfig.ENCHANTS.containsKey(candidate) ? candidate : null;
        }
    }
}
