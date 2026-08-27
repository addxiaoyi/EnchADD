package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

final class EnchantListSafeModeCommand {

    private EnchantListSafeModeCommand() {
    }

    static boolean handle(JavaPlugin plugin, CommandSender sender, String[] args) {
        if (!EnchADDConfig.isSafetyModeEnabled()) {
            sender.sendMessage(SafeModeMessageComposer.disabled());
            return true;
        }

        if (!SafeModeActionSupport.apply(args)) {
            sender.sendMessage(SafeModeMessageComposer.usage());
            return true;
        }

        String status = buildStatus();
        plugin.getLogger().info(status);
        sender.sendMessage(SafeModeMessageComposer.status(status));
        return true;
    }

    static String buildStatus() {
        return SafeModeStatusComposer.compose();
    }
}
