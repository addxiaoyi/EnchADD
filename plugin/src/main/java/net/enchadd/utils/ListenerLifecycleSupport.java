package net.enchadd.utils;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

final class ListenerLifecycleSupport {

    private ListenerLifecycleSupport() {
    }

    static void unregister(@Nullable Listener listener) {
        if (listener == null) {
            return;
        }
        HandlerList.unregisterAll(listener);
    }

    static void unregisterAll(@NotNull Map<String, Listener> registeredListeners,
                              @NotNull ListenerBudgetDispatchSupport.WarningLogger warningLogger) {
        Objects.requireNonNull(registeredListeners, "registeredListeners");
        Objects.requireNonNull(warningLogger, "warningLogger");

        for (Listener listener : registeredListeners.values()) {
            invokeCleanupIfPresent(listener, warningLogger);
            HandlerList.unregisterAll(listener);
        }
        registeredListeners.clear();
    }

    private static void invokeCleanupIfPresent(@NotNull Listener listener,
                                               @NotNull ListenerBudgetDispatchSupport.WarningLogger warningLogger) {
        try {
            java.lang.reflect.Method cleanupMethod = listener.getClass().getMethod("cleanup");
            cleanupMethod.invoke(listener);
        } catch (NoSuchMethodException ignored) {
            // 监听器没有 cleanup 方法，跳过
        } catch (Exception e) {
            warningLogger.warn("清理监听器时出错: " + listener.getClass().getName() + " - " + e.getMessage());
        }
    }
}
