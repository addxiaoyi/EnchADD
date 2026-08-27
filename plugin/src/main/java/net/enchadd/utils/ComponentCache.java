package net.enchadd.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 优化5：消息组件预编译缓存（Component Pre-compilation Cache）
 *
 * 在插件启动时预编译所有常用的消息 Component，
 * 减少事件触发时重复调用 Adventure API 的解析开销，
 * 降低 GC 压力（避免每次事件触发都创建临时对象）。
 *
 * 线程安全（ConcurrentHashMap），支持颜色、前缀预设。
 */
public class ComponentCache {

    // ─── 系统内置快捷常量 ────────────────────────────────────────────────────────
    /** EnchADD 统一前缀 */
    public static final Component PREFIX = Component.text()
            .append(Component.text("[", NamedTextColor.DARK_GRAY))
            .append(Component.text("EnchADD", TextColor.color(0x7C6AFF)))
            .append(Component.text("] ", NamedTextColor.DARK_GRAY))
            .build();

    public static final Component NO_PERM = PREFIX.append(
            Component.text("权限不足！", NamedTextColor.RED));

    public static final Component NOT_FOUND = PREFIX.append(
            Component.text("未找到该附魔。", NamedTextColor.YELLOW));

    public static final Component EXPORT_FAILED = PREFIX.append(
            Component.text("导出失败，请检查服务器日志。", NamedTextColor.RED));

    public static final Component ON_COOLDOWN = PREFIX.append(
            Component.text("附魔冷却中，请稍后再试！", NamedTextColor.GOLD));

    // ─── 动态缓存池 ──────────────────────────────────────────────────────────────
    private static final Map<String, Component> dynamicCache = new ConcurrentHashMap<>();

    private ComponentCache() {}

    /**
     * 获取或创建带指定前缀的纯文本 Component 并缓存。
     *
     * @param key   缓存键（通常为语言Key或消息Key）
     * @param text  消息文本
     * @param color 文字颜色
     * @return 预编译的 Component（附带 EnchADD 前缀）
     */
    public static Component getOrCreate(String key, String text, NamedTextColor color) {
        return dynamicCache.computeIfAbsent(key, k ->
                PREFIX.append(Component.text(text, color))
        );
    }

    /**
     * 获取或创建纯文字 Component（不附前缀）并缓存。
     *
     * @param key  缓存键
     * @param text 消息文本
     * @return 预编译的 Component
     */
    public static Component getOrCreateText(String key, String text) {
        return dynamicCache.computeIfAbsent(key, k -> Component.text(text));
    }

    /**
     * 显式注册一个预编译好的 Component 到缓存。
     *
     * @param key       缓存键
     * @param component 要缓存的 Component
     */
    public static void register(String key, Component component) {
        dynamicCache.put(key, component);
    }

    /**
     * 按缓存键取得 Component；若未命中缓存，返回 null。
     *
     * @param key 缓存键
     * @return 缓存的 Component 或 null
     */
    public static Component get(String key) {
        return dynamicCache.get(key);
    }

    /**
     * 插件重载/热更新时清理缓存池（不清除系统常量）。
     */
    public static void invalidate() {
        dynamicCache.clear();
    }

    /**
     * 构建带剩余冷却时间的提示 Component（不走缓存，因值各异）。
     *
     * @param remainingMs 剩余毫秒数
     * @return Component
     */
    public static Component cooldownRemaining(long remainingMs) {
        String s = String.format("%.1f", remainingMs / 1000.0);
        return PREFIX.append(
                Component.text("冷却中（剩余 " + s + "s）", NamedTextColor.GOLD)
        );
    }

    /**
     * 构建导出成功消息（附路径）。
     *
     * @param path 导出路径
     * @return Component
     */
    public static Component exportDone(String path) {
        return PREFIX.append(
                Component.text("已导出至：", NamedTextColor.GREEN)
                        .append(Component.text(path, NamedTextColor.AQUA))
        );
    }
}
