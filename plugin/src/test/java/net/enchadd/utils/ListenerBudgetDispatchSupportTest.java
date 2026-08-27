package net.enchadd.utils;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListenerBudgetDispatchSupportTest {

    @Test
    void executeWithBudgetSkipsDelegateAndWarnsWhenEventTypeDoesNotMatch() throws Exception {
        TestListener listener = new TestListener();
        Method method = TestListener.class.getDeclaredMethod("handle", TestEvent.class);
        List<String> warnings = new ArrayList<>();
        CountingExecutor delegate = new CountingExecutor();

        ListenerBudgetDispatchSupport.executeWithBudget(
                "enchadd:test",
                listener,
                method,
                delegate,
                listener,
                new OtherEvent(),
                warnings::add
        );

        assertEquals(0, delegate.calls);
        assertEquals(1, warnings.size());
    }

    @Test
    void executeWithBudgetInvokesDelegateWhenEventTypeMatches() throws Exception {
        TestListener listener = new TestListener();
        Method method = TestListener.class.getDeclaredMethod("handle", TestEvent.class);
        List<String> warnings = new ArrayList<>();
        CountingExecutor delegate = new CountingExecutor();

        ListenerBudgetDispatchSupport.executeWithBudget(
                "enchadd:test",
                listener,
                method,
                delegate,
                listener,
                new TestEvent(),
                warnings::add
        );

        assertEquals(1, delegate.calls);
        assertEquals(0, warnings.size());
    }

    private static final class TestListener implements Listener {
        void handle(TestEvent event) {
        }
    }

    private static final class CountingExecutor implements EventExecutor {
        private int calls;

        @Override
        public void execute(Listener listener, Event event) {
            calls++;
        }
    }

    private static final class TestEvent extends Event {
        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }

    private static final class OtherEvent extends Event {
        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }
    }
}
