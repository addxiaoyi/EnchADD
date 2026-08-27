package net.enchadd.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class EnchantListPermissionSupport {

    boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player p && !evaluate(true, p.hasPermission(permission))) {
            p.sendMessage(Component.translatable("EnchADD.command.no_permission"));
            return false;
        }
        return true;
    }

    static boolean evaluate(boolean playerSender, boolean hasPermission) {
        return !playerSender || hasPermission;
    }
}
