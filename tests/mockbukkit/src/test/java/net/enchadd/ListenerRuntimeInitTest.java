package net.enchadd;

import net.enchadd.listeners.DispelListener;
import net.enchadd.listeners.PurifyListener;
import net.enchadd.listeners.QuellListener;
import net.enchadd.listeners.VolleyListener;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ListenerRuntimeInitTest {

    private ServerMock server;

    @AfterEach
    void tearDown() {
        TestPluginSupport.unregisterAllHandlers();
        MockBukkit.unmock();
    }

    @Test
    void criticalListenersCanBeConstructedAndRegisteredUnderMockBukkit() {
        server = MockBukkit.mock();
        Plugin plugin = TestPluginSupport.enabledPlugin(server, "runtime-init");

        assertDoesNotThrow(() -> register(plugin, new PurifyListener()));
        assertDoesNotThrow(() -> register(plugin, new DispelListener()));
        assertDoesNotThrow(() -> register(plugin, new QuellListener()));
        assertDoesNotThrow(() -> register(plugin, new VolleyListener()));
    }

    private void register(Plugin plugin, Listener listener) {
        server.getPluginManager().registerEvents(listener, plugin);
    }
}
