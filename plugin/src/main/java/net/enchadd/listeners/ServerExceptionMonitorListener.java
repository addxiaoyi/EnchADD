package net.enchadd.listeners;

import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerPluginException;
import net.enchadd.utils.RuntimeErrorTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Tracks Paper runtime exceptions attributable to this plugin.
 */
public final class ServerExceptionMonitorListener implements Listener {

    private final JavaPlugin plugin;

    public ServerExceptionMonitorListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onServerException(ServerExceptionEvent event) {
        if (!(event.getException() instanceof ServerPluginException serverPluginException)) {
            return;
        }
        Plugin responsible = serverPluginException.getResponsiblePlugin();
        if (responsible == null) {
            return;
        }
        if (!plugin.getName().equalsIgnoreCase(responsible.getName())) {
            return;
        }
        RuntimeErrorTracker.recordError("ServerExceptionEvent");
    }
}
