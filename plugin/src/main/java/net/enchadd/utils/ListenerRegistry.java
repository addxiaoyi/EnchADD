package net.enchadd.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 优化7：动态监听器开关（Adaptive Listener Toggling）
 *
 * 管理所有 EnchADD 监听器的生命周期：
 *
 * - 启动时：按配置决定是否注册事件监听器（Suggestion 1 已实现基础版本，此处扩展为可管理的热更新版本）
 * - reload 命令时：可以精确地注销已禁用的监听器，注册新启用的监听器
 * - 内存效率：已禁用的附魔对应的监听器不存在于事件总线中，完全零开销
 *
 * 架构：使用 WeakReference 跟踪已注册的 Listener 实例，支持精确注销。
 */
public class ListenerRegistry {

    /** 已注册监听器的快照：enchantKey -> Listener */
    private static final Map<String, Listener> registeredListeners = new ConcurrentHashMap<>();

    private static JavaPlugin plugin;

    private ListenerRegistry() {}

    /**
     * 初始化（在 onEnable 中调用，必须在所有 registerIfEnabled 调用之前）。
     *
     * @param plugin 主插件实例
     */
    public static void init(JavaPlugin plugin) {
        ListenerRegistry.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * 如果指定附魔处于启用状态，则注册对应监听器。
     * 如果监听器已注册，跳过（防止重复注册）。
     *
     * @param enchantKey 附魔 Key
     * @param supplier   监听器工厂（懒加载，避免不必要的对象创建）
     */
    public static void registerIfEnabled(Key enchantKey, Supplier<Listener> supplier) {
        if (plugin == null) {
            Bukkit.getLogger().warning("[EnchADD] ListenerRegistry 尚未初始化，跳过监听器注册: " + enchantKey.asString());
            return;
        }
        String keyStr = enchantKey.asString();
        if (!EnchADDConfig.ENCHANTS.containsKey(enchantKey)) {
            return; // 附魔未启用，不注册
        }
        if (registeredListeners.containsKey(keyStr)) {
            return; // 已注册，跳过
        }
        Listener listener = supplier.get();
        if (listener == null) {
            warn("监听器工厂返回 null，跳过注册: " + keyStr);
            return;
        }
        registerListenerWithBudget(keyStr, listener);
        registeredListeners.put(keyStr, listener);
    }

    /**
     * 注销指定附魔对应的监听器（用于热重载或管理员禁用某附魔时）。
     *
     * @param enchantKey 附魔 Key
     */
    public static void unregister(Key enchantKey) {
        Listener listener = registeredListeners.remove(enchantKey.asString());
        ListenerLifecycleSupport.unregister(listener);
    }

    /**
     * 注销所有由 ListenerRegistry 管理的监听器（onDisable 时调用）。
     * 修复: 添加对监听器清理方法的调用，防止资源泄漏
     */
    public static void unregisterAll() {
        ListenerLifecycleSupport.unregisterAll(registeredListeners, ListenerRegistry::warn);
    }

    /**
     * 热重载：对比新配置，注销新禁用的监听器，注册新启用的监听器。
     * 使用场景：/enchadd reload 命令。
     *
     * @param suppliers 当前所有附魔的监听器工厂（key -> supplier）
     */
    public static void reload(Map<Key, Supplier<Listener>> suppliers) {
        // 注销不再启用的附魔对应的监听器
        for (String keyStr : new ArrayList<>(registeredListeners.keySet())) {
            Key key = Key.key(keyStr);
            if (!EnchADDConfig.ENCHANTS.containsKey(key)) {
                unregister(key);
            }
        }
        // 注册新启用的附魔对应的监听器
        for (Map.Entry<Key, Supplier<Listener>> entry : suppliers.entrySet()) {
            registerIfEnabled(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取当前已注册的监听器数量（用于调试/状态上报）。
     */
    public static int getRegisteredCount() {
        return registeredListeners.size();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerListenerWithBudget(String enchantKey, Listener listener) {
        ListenerBudgetDispatchSupport.registerListenerWithBudget(
                enchantKey,
                listener,
                plugin,
                ListenerRegistry::executeWithBudget
        );
    }

    private static void executeWithBudget(String enchantKey,
                                          Listener listener,
                                          Method method,
                                          EventExecutor delegate,
                                          Listener ignoredListener,
                                          Event event) throws EventException {
        ListenerBudgetDispatchSupport.executeWithBudget(
                enchantKey,
                listener,
                method,
                delegate,
                ignoredListener,
                event,
                ListenerRegistry::warn
        );
    }

    private static void warn(String message) {
        if (plugin != null) {
            plugin.getLogger().warning(message);
            return;
        }
        Bukkit.getLogger().warning("[EnchADD] " + message);
    }
}
