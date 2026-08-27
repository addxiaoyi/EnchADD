package net.enchadd;

import net.enchadd.utils.ListenerRegistry;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ListenerRegistryCompatibilityTest {

    @AfterEach
    void tearDown() {
        TestPluginSupport.unregisterAllHandlers();
    }

    @Test
    void budgetDispatchSkipsMismatchedEventTypeInsteadOfThrowing() throws Exception {
        initRegistryWithMockPlugin();
        SampleListener listener = new SampleListener();
        Method handler = SampleListener.class.getDeclaredMethod("onExpected", ExpectedEvent.class);
        handler.setAccessible(true);
        EventExecutor delegate = EventExecutor.create(handler, ExpectedEvent.class);
        Method execute = resolveExecuteWithBudgetMethod();

        assertDoesNotThrow(() -> invokeExecute(execute, listener, handler, delegate, new OtherEvent()));
        assertEquals(0, listener.invocations, "Mismatched event should be skipped before listener invocation");
    }

    @Test
    void budgetDispatchStillInvokesMatchingEventType() throws Exception {
        initRegistryWithMockPlugin();
        SampleListener listener = new SampleListener();
        Method handler = SampleListener.class.getDeclaredMethod("onExpected", ExpectedEvent.class);
        handler.setAccessible(true);
        EventExecutor delegate = EventExecutor.create(handler, ExpectedEvent.class);
        Method execute = resolveExecuteWithBudgetMethod();

        assertDoesNotThrow(() -> invokeExecute(execute, listener, handler, delegate, new ExpectedEvent()));
        assertEquals(1, listener.invocations, "Matching event should still execute listener logic");
    }

    private static Method resolveExecuteWithBudgetMethod() throws NoSuchMethodException {
        Method execute = ListenerRegistry.class.getDeclaredMethod(
                "executeWithBudget",
                String.class,
                Listener.class,
                Method.class,
                EventExecutor.class,
                Listener.class,
                Event.class
        );
        execute.setAccessible(true);
        return execute;
    }

    private static void initRegistryWithMockPlugin() {
        JavaPlugin plugin = Mockito.mock(JavaPlugin.class);
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("listener-registry-test"));
        ListenerRegistry.init(plugin);
    }

    private static void invokeExecute(Method execute,
                                      SampleListener listener,
                                      Method handler,
                                      EventExecutor delegate,
                                      Event event) throws Throwable {
        try {
            execute.invoke(null, "enchadd:test", listener, handler, delegate, listener, event);
        } catch (InvocationTargetException wrapped) {
            Throwable cause = wrapped.getCause();
            if (cause != null) {
                throw cause;
            }
            throw wrapped;
        }
    }

    private static final class SampleListener implements Listener {
        private int invocations = 0;

        @EventHandler
        public void onExpected(ExpectedEvent event) {
            invocations++;
        }
    }

    private static final class ExpectedEvent extends Event {
        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    private static final class OtherEvent extends Event {
        private static final HandlerList HANDLERS = new HandlerList();

        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }
}
