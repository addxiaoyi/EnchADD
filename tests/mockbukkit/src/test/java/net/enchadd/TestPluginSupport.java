package net.enchadd;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.Mockito;

import java.util.logging.Logger;

final class TestPluginSupport {

    private TestPluginSupport() {
    }

    static Plugin enabledPlugin(ServerMock server, String name) {
        Plugin plugin = Mockito.mock(Plugin.class);
        Mockito.when(plugin.isEnabled()).thenReturn(true);
        Mockito.when(plugin.getName()).thenReturn(name);
        Mockito.when(plugin.getServer()).thenReturn(server);
        Mockito.when(plugin.getPluginLoader()).thenReturn(new JavaPluginLoader(server));
        Mockito.when(plugin.getLogger()).thenReturn(Logger.getLogger(name));
        Mockito.when(plugin.getDescription()).thenReturn(new PluginDescriptionFile(name, "1.0.0", TestPluginSupport.class.getName()));
        return plugin;
    }

    static void unregisterAllHandlers() {
        HandlerList.unregisterAll();
    }
}
