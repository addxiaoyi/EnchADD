package com.enadd.core.event;

import com.enadd.config.EnchantmentConfig;
import com.enadd.util.Logger;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * 优化的事件处理器 - 支持异步事件处理和事件过滤
 *
 * <p>提供高效的事件监听机制，减少不必要的事件扫描，支持Paper API的异步事件处理。</p>
 *
 * <p><strong>核心特性：</strong></p>
 * <ul>
 *   <li>事件过滤：基于条件的预过滤，减少处理开销</li>
 *   <li>异步处理：支持Paper API异步事件，避免阻塞主线程</li>
 *   <li>批处理：批量处理高频事件，减少方法调用开销</li>
 *   <li>限流：防止事件风暴导致服务器卡顿</li>
 * </ul>
 *
 * @author ADDXIAOYI2048
 * @version 1.0.0
 * @since 1.0.0
 */
public class OptimizedEventHandler implements Listener {

    private final JavaPlugin plugin;
    private final Logger logger;

    /** 事件处理器注册表 */
    private final Map<Class<? extends Event>, EventHandlerGroup<?>> handlers = new ConcurrentHashMap<>();

    /** 异步事件执行器 */
    private final ExecutorService asyncExecutor;

    /** 事件计数器（用于限流） */
    private final AtomicInteger eventCounter = new AtomicInteger(0);

    /** 是否启用异步处理 */
    private final boolean useAsync;

    /**
     * 创建优化的事件处理器
     *
     * @param plugin 插件实例
     */
    public OptimizedEventHandler(JavaPlugin plugin) {
        this(plugin, EnchantmentConfig.EventConfig.USE_ASYNC_EVENTS);
    }

    /**
     * 创建优化的事件处理器
     *
     * @param plugin 插件实例
     * @param useAsync 是否启用异步处理
     */
    public OptimizedEventHandler(JavaPlugin plugin, boolean useAsync) {
        this.plugin = plugin;
        this.logger = Logger.getInstance();
        this.useAsync = useAsync;

        // 创建异步执行器
        this.asyncExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "OptimizedEventHandler-Async");
            t.setDaemon(true);
            return t;
        });

        // 注册到插件
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        logger.info("OptimizedEventHandler 初始化完成，异步处理: %s", useAsync);
    }

    /**
     * 注册事件处理器
     *
     * @param <T> 事件类型
     * @param eventClass 事件类
     * @param handler 处理器
     * @return 处理器构建器
     */
    public <T extends Event> EventHandlerBuilder<T> onEvent(Class<T> eventClass, Consumer<T> handler) {
        return new EventHandlerBuilder<>(this, eventClass, handler);
    }

    /**
     * 注册事件处理器（内部方法）
     */
    @SuppressWarnings("unchecked")
    <T extends Event> void registerHandler(Class<T> eventClass, EventHandlerConfig<T> config) {
        EventHandlerGroup<T> group = (EventHandlerGroup<T>) handlers.computeIfAbsent(
            eventClass,
            k -> new EventHandlerGroup<>(plugin, eventClass, useAsync, asyncExecutor)
        );

        group.addHandler(config);
        logger.debug("注册事件处理器: %s", eventClass.getSimpleName());
    }

    /**
     * 检查是否需要限流
     *
     * @return 是否超过限流阈值
     */
    public boolean shouldThrottle() {
        return eventCounter.incrementAndGet() > EnchantmentConfig.EventConfig.MAX_EVENTS_PER_TICK;
    }

    /**
     * 重置事件计数器
     */
    public void resetCounter() {
        eventCounter.set(0);
    }

    /**
     * 关闭事件处理器
     */
    public void shutdown() {
        // 注销所有监听器
        HandlerList.unregisterAll(this);

        // 关闭异步执行器
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }

        handlers.clear();
        logger.info("OptimizedEventHandler 已关闭");
    }

    // ==================== 内部类 ====================

    /**
     * 事件处理器组
     */
    private static class EventHandlerGroup<T extends Event> implements Listener {

        private final JavaPlugin plugin;
        private final Class<T> eventClass;
        private final Set<EventHandlerConfig<T>> handlerConfigs = ConcurrentHashMap.newKeySet();
        private final boolean useAsync;
        private final ExecutorService asyncExecutor;

        EventHandlerGroup(JavaPlugin plugin, Class<T> eventClass, boolean useAsync, ExecutorService asyncExecutor) {
            this.plugin = plugin;
            this.eventClass = eventClass;
            this.useAsync = useAsync;
            this.asyncExecutor = asyncExecutor;

            // 注册Bukkit事件监听
            registerBukkitListener();
        }

        void addHandler(EventHandlerConfig<T> config) {
            handlerConfigs.add(config);
        }

        @SuppressWarnings("unchecked")
        private void registerBukkitListener() {
            EventExecutor executor = (listener, event) -> {
                if (!eventClass.isInstance(event)) {
                    return;
                }

                T typedEvent = (T) event;
                handleEvent(typedEvent);
            };

            plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                this,
                EventPriority.NORMAL,
                executor,
                plugin,
                false
            );
        }

        void handleEvent(T event) {
            // 检查冷却
            long now = System.currentTimeMillis();

            for (EventHandlerConfig<T> config : handlerConfigs) {
                // 检查冷却
                if (now - config.lastExecution < config.cooldownMs) {
                    continue;
                }

                // 检查过滤条件
                if (config.filter != null && !config.filter.test(event)) {
                    continue;
                }

                // 更新最后执行时间
                config.lastExecution = now;

                // 执行处理器
                if (config.async && useAsync) {
                    asyncExecutor.submit(() -> {
                        try {
                            config.handler.accept(event);
                        } catch (Exception e) {
                            plugin.getLogger().warning("异步事件处理异常: " + e.getMessage());
                        }
                    });
                } else {
                    try {
                        config.handler.accept(event);
                    } catch (Exception e) {
                        plugin.getLogger().warning("事件处理异常: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 事件处理器配置
     */
    private static class EventHandlerConfig<T extends Event> {
        final Consumer<T> handler;
        final Predicate<T> filter;
        final long cooldownMs;
        final boolean async;
        volatile long lastExecution = 0;

        EventHandlerConfig(Consumer<T> handler, Predicate<T> filter, long cooldownMs, boolean async) {
            this.handler = handler;
            this.filter = filter;
            this.cooldownMs = cooldownMs;
            this.async = async;
        }
    }

    /**
     * 事件处理器构建器
     */
    public static class EventHandlerBuilder<T extends Event> {

        private final OptimizedEventHandler parent;
        private final Class<T> eventClass;
        private final Consumer<T> handler;

        private Predicate<T> filter;
        private long cooldownMs = 0;
        private boolean async = false;

        EventHandlerBuilder(OptimizedEventHandler parent, Class<T> eventClass, Consumer<T> handler) {
            this.parent = parent;
            this.eventClass = eventClass;
            this.handler = handler;
        }

        /**
         * 设置过滤条件
         *
         * @param filter 过滤器
         * @return 构建器
         */
        public EventHandlerBuilder<T> filter(Predicate<T> filter) {
            this.filter = filter;
            return this;
        }

        /**
         * 设置冷却时间
         *
         * @param cooldownMs 冷却时间（毫秒）
         * @return 构建器
         */
        public EventHandlerBuilder<T> cooldown(long cooldownMs) {
            this.cooldownMs = cooldownMs;
            return this;
        }

        /**
         * 启用异步处理
         *
         * @return 构建器
         */
        public EventHandlerBuilder<T> async() {
            this.async = true;
            return this;
        }

        /**
         * 注册处理器
         */
        public void register() {
            EventHandlerConfig<T> config = new EventHandlerConfig<>(handler, filter, cooldownMs, async);
            parent.registerHandler(eventClass, config);
        }
    }
}
