package net.enchadd.listeners.support;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public final class ListenerDispatchGuard {

    private final ThreadLocal<Boolean> active = ThreadLocal.withInitial(() -> false);

    public boolean isActive() {
        return active.get();
    }

    public <T> T execute(@NotNull Supplier<T> action) {
        boolean previous = active.get();
        active.set(true);
        try {
            return action.get();
        } finally {
            active.set(previous);
        }
    }
}
