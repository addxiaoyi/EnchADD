package net.enchadd.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * Security listener to prevent op_grant_exploit via command blocks.
 *
 * In Minecraft, command blocks and command minecarts execute commands with elevated
 * privileges (OP level) by default. Malicious players can place command blocks with
 * commands like "/op &lt;username&gt;" to grant themselves admin rights.
 *
 * This listener blocks OP-granting commands from non-console, non-player sources
 * (command blocks and command minecarts).
 */
public class CommandBlockListener implements Listener {

    private static final String[] DANGEROUS_COMMANDS = {
        "op", "deop", "stop", "reload", "restart", "plugins", "pl", "bukkit", "spigot",
        "timings", "paper", "version", "ver", "about", "icanhasbukkit", "bukkit:version",
        "spigot:reload", "worldedit:.", "worldedit:superpickaxe", "worldedit:pos1",
        "worldedit:pos2", "worldedit:hs", "worldedit:as", "worldedit:brush", "worldedit:biome"
    };

    /**
     * Blocks dangerous commands from command block sources.
     * Called before the command is processed by the server.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerCommand(ServerCommandEvent event) {
        CommandSender sender = event.getSender();

        if (block_op_commands(sender)) {
            String command = event.getCommand().toLowerCase(java.util.Locale.ROOT).trim();

            if (isDangerousCommand(command)) {
                event.setCancelled(true);
                logBlockedCommand(sender, event.getCommand());
            }
        }
    }

    /**
     * Determines whether to block OP-related commands from the given sender.
     * Returns true if the sender is a command block or command minecart.
     *
     * @param sender the command sender to check
     * @return true if commands should be blocked from this sender
     */
    public static boolean block_op_commands(CommandSender sender) {
        return sender instanceof BlockCommandSender
            || sender instanceof CommandMinecart;
    }

    /**
     * Checks if the command is potentially dangerous when executed from
     * a command block (e.g., commands that grant permissions or modify server state).
     *
     * @param command the command to check (should be lowercase, trimmed)
     * @return true if the command is considered dangerous
     */
    private static boolean isDangerousCommand(String command) {
        for (String dangerous : DANGEROUS_COMMANDS) {
            if (command.equals(dangerous) || command.startsWith(dangerous + " ")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logs blocked command attempts for server administrator review.
     */
    private void logBlockedCommand(CommandSender sender, String command) {
        String senderType = sender instanceof BlockCommandSender
            ? "BlockCommandSender"
            : sender instanceof CommandMinecart
                ? "CommandMinecart"
                : sender.getClass().getSimpleName();

        String location = "";
        if (sender instanceof BlockCommandSender blockSender) {
            var block = blockSender.getBlock();
            location = String.format(" at (%d, %d, %d) in world '%s'",
                block.getX(), block.getY(), block.getZ(),
                block.getWorld().getName());
        } else if (sender instanceof CommandMinecart minecart) {
            var loc = minecart.getLocation();
            location = String.format(" at (%d, %d, %d) in world '%s'",
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                loc.getWorld().getName());
        }

        org.bukkit.Bukkit.getLogger().warning(
            "[EnchADD Security] Blocked dangerous command from " + senderType + location + ": " + command
        );
    }

    /**
     * Utility method to check if a sender is a privileged non-player source.
     * Includes console sender as a safety measure for certain operations.
     *
     * @param sender the command sender to check
     * @return true if the sender is a privileged non-player source
     */
    public static boolean isPrivilegedNonPlayer(CommandSender sender) {
        return sender instanceof ConsoleCommandSender
            || sender instanceof BlockCommandSender
            || sender instanceof CommandMinecart;
    }
}
