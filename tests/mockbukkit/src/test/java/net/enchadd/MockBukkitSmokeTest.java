package net.enchadd;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MockBukkitSmokeTest {

    @Test
    void serverStartsAndSyntheticPluginCanRegisterEvents() {
        try {
            ServerMock server = MockBukkit.mock();
            Plugin plugin = TestPluginSupport.enabledPlugin(server, "smoke-plugin");
            assertNotNull(server);
            assertNotNull(plugin);
            assertDoesNotThrow(() -> server.getPluginManager().registerEvents(new Listener() {}, plugin));
        } finally {
            TestPluginSupport.unregisterAllHandlers();
            MockBukkit.unmock();
        }
    }
}
