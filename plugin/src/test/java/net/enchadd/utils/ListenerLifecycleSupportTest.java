package net.enchadd.utils;

import org.bukkit.event.Listener;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListenerLifecycleSupportTest {

    @Test
    void unregisterAllInvokesCleanupWhenPresentAndClearsRegistry() {
        Map<String, Listener> listeners = new LinkedHashMap<>();
        CleanupListener listener = new CleanupListener();
        listeners.put("cleanup", listener);

        ListenerLifecycleSupport.unregisterAll(listeners, message -> { });

        assertTrue(listener.cleaned.get());
        assertTrue(listeners.isEmpty());
    }

    @Test
    void unregisterAllWarnsWhenCleanupFailsAndStillClearsRegistry() {
        Map<String, Listener> listeners = new LinkedHashMap<>();
        listeners.put("failing", new FailingCleanupListener());
        List<String> warnings = new java.util.ArrayList<>();

        ListenerLifecycleSupport.unregisterAll(listeners, warnings::add);

        assertTrue(listeners.isEmpty());
        assertEquals(1, warnings.size());
        assertTrue(warnings.getFirst().contains("清理监听器时出错"));
        assertTrue(warnings.getFirst().contains(FailingCleanupListener.class.getName()));
    }

    public static final class CleanupListener implements Listener {
        final AtomicBoolean cleaned = new AtomicBoolean(false);

        public void cleanup() {
            cleaned.set(true);
        }
    }

    public static final class FailingCleanupListener implements Listener {
        public void cleanup() {
            throw new IllegalStateException("boom");
        }
    }
}
