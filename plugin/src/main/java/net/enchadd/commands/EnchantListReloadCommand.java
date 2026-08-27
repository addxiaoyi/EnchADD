package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.enchadd.utils.LangManager;
import net.enchadd.utils.RuntimeHealthMonitor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

final class EnchantListReloadCommand {

    private EnchantListReloadCommand() {
    }

    static boolean handle(JavaPlugin plugin, CommandSender sender) {
        boolean loaded = EnchADDConfig.reloadRuntimeSettings(plugin.getDataFolder().toPath());
        if (!loaded) {
            sender.sendMessage(ReloadMessageComposer.failure());
            return true;
        }
        LangManager.reload(plugin.getDataFolder().toPath());
        RuntimeHealthMonitor.start(plugin);
        String message = buildReloadMessage();
        plugin.getLogger().info(message);
        sender.sendMessage(ReloadMessageComposer.success(message));
        return true;
    }

    static String buildReloadMessage() {
        return ReloadStatusComposer.success();
    }
}
