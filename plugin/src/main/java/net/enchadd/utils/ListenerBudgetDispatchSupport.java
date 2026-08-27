package net.enchadd.utils;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

final class ListenerBudgetDispatchSupport {

    private ListenerBudgetDispatchSupport() {
    }

    static void registerListenerWithBudget(@NotNull String enchantKey,
                                           @NotNull Listener listener,
                                           @NotNull JavaPlugin plugin,
                                           @NotNull BudgetDispatchExecutor dispatchExecutor) {
        Objects.requireNonNull(enchantKey, "enchantKey");
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(dispatchExecutor, "dispatchExecutor");

        Set<String> signatures = new HashSet<>();
        Class<?> type = listener.getClass();
        boolean anyHandler = false;
        while (type != null && type != Object.class) {
            for (Method method : type.getDeclaredMethods()) {
                EventHandler handler = method.getAnnotation(EventHandler.class);
                if (handler == null) {
                    continue;
                }
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
                    continue;
                }
                String signature = method.getName() + "#" + params[0].getName();
                if (!signatures.add(signature)) {
                    continue;
                }
                anyHandler = true;
                method.setAccessible(true);
                @SuppressWarnings("unchecked")
                Class<? extends Event> eventClass = (Class<? extends Event>) params[0].asSubclass(Event.class);
                registerBudgetWrappedEvent(
                        enchantKey,
                        listener,
                        plugin,
                        dispatchExecutor,
                        method,
                        eventClass,
                        handler.priority(),
                        handler.ignoreCancelled()
                );
            }
            type = type.getSuperclass();
        }
        if (!anyHandler) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    private static void registerBudgetWrappedEvent(@NotNull String enchantKey,
                                                   @NotNull Listener listener,
                                                   @NotNull JavaPlugin plugin,
                                                   @NotNull BudgetDispatchExecutor dispatchExecutor,
                                                   @NotNull Method method,
                                                   @NotNull Class<? extends Event> eventClass,
                                                   @NotNull EventPriority priority,
                                                   boolean ignoreCancelled) {
        EventExecutor delegate = EventExecutor.create(method, eventClass);
        EventExecutor executor = (ignoredListener, event) -> {
            // Paper/Bukkit 某些事件层级会共享 HandlerList（例如 EntitySpawnEvent 族）。
            // 按注册时的目标事件类型做一次过滤，避免无关子类触发误调用/告警。
            if (!eventClass.isInstance(event)) {
                return;
            }
            dispatchExecutor.execute(enchantKey, listener, method, delegate, ignoredListener, event);
        };
        Bukkit.getPluginManager().registerEvent(eventClass, listener, priority, executor, plugin, ignoreCancelled);
    }

    static void executeWithBudget(@NotNull String enchantKey,
                                  @NotNull Listener listener,
                                  @NotNull Method method,
                                  @NotNull EventExecutor delegate,
                                  @NotNull Listener ignoredListener,
                                  @NotNull Event event,
                                  @NotNull WarningLogger warningLogger) throws EventException {
        Objects.requireNonNull(enchantKey, "enchantKey");
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(delegate, "delegate");
        Objects.requireNonNull(ignoredListener, "ignoredListener");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(warningLogger, "warningLogger");

        EnchantExecutionBudgetManager.ExecutionToken token = EnchantExecutionBudgetManager.enter(enchantKey);
        if (token.skipExecution()) {
            EnchantExecutionBudgetManager.exit(token, 0L);
            return;
        }
        long startNanos = System.nanoTime();
        try {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || !params[0].isInstance(event)) {
                warningLogger.warn("跳过事件调用，参数类型不匹配: listener=" + listener.getClass().getSimpleName()
                        + " method=" + method.getName()
                        + " expected=" + (params.length == 1 ? params[0].getName() : "invalid")
                        + " actual=" + event.getClass().getName());
                return;
            }
            delegate.execute(ignoredListener, event);
        } catch (IllegalArgumentException ex) {
            throw new EventException(new IllegalArgumentException(
                    "Listener invoke type mismatch: listener=" + listener.getClass().getName()
                            + ", method=" + method.toGenericString()
                            + ", event=" + event.getClass().getName(),
                    ex
            ));
        } finally {
            long elapsedNanos = System.nanoTime() - startNanos;
            EnchantExecutionBudgetManager.exit(token, elapsedNanos);
        }
    }

    @FunctionalInterface
    interface BudgetDispatchExecutor {
        void execute(@NotNull String enchantKey,
                     @NotNull Listener listener,
                     @NotNull Method method,
                     @NotNull EventExecutor delegate,
                     @NotNull Listener ignoredListener,
                     @NotNull Event event) throws EventException;
    }

    @FunctionalInterface
    interface WarningLogger {
        void warn(@NotNull String message);
    }
}
