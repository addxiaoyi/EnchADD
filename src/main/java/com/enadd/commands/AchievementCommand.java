package com.enadd.commands;

import com.enadd.achievements.AchievementManager;
import com.enadd.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Command handler for achievement-related commands
 * Provides player and admin functionality for achievements
 */
public final class AchievementCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            // Validate inputs
            if (sender == null) {
                return false;
            }

            if (args == null) {
                args = new String[0];
            }

            if (args.length == 0) {
                showHelp(sender);
                return true;
            }

            String subCommand = args[0].toLowerCase().trim();

            switch (subCommand) {
                case "list":
                    return handleList(sender, args);
                case "check":
                    return handleCheck(sender, args);
                case "progress":
                    return handleProgress(sender, args);
                case "reset":
                    return handleReset(sender, args);
                case "help":
                default:
                    showHelp(sender);
                    return true;
            }
        } catch (Exception e) {
            com.enadd.util.ErrorHandler.handleException(null, "Achievement command execution", e);
            if (sender != null) {
                sender.sendMessage("§cAn error occurred while processing your command. Please try again.");
            }
            return true;
        }
    }

    private boolean handleList(CommandSender sender, @SuppressWarnings("unused") String[] args) {
        if (!(sender instanceof Player)) {
            if (sender != null) {
                sender.sendMessage("§cThis command can only be used by players.");
            }
            return true;
        }

        Player player = (Player) sender;
        AchievementManager manager = AchievementManager.getInstance();

        if (manager == null) {
            sender.sendMessage("§cAchievement system is not initialized.");
            return true;
        }

        sender.sendMessage("§5§l=== EnchAdd Achievements ===");
        sender.sendMessage("");

        // List all achievements with status
        String[] achievements = {"enchantment_master", "cursed_warrior", "one_shot_kill", "lumberjack", "marksman"};

        for (String achievementId : achievements) {
            boolean hasAchievement = manager.hasAchievement(player, achievementId);
            String status = hasAchievement ? "§a✓ Completed" : "§7○ Incomplete";
            String title = ConfigManager.getMessage("achievement." + achievementId + ".title", achievementId);
            String desc = ConfigManager.getMessage("achievement." + achievementId + ".desc", "");

            sender.sendMessage("§d" + title + " " + status);
            sender.sendMessage("  §7" + desc);

            // Show progress for incomplete achievements
            if (!hasAchievement) {
                String progress = getProgressString(player, achievementId, manager);
                if (!progress.isEmpty()) {
                    sender.sendMessage("  §e" + progress);
                }
            }
            sender.sendMessage("");
        }

        return true;
    }

    private boolean handleCheck(CommandSender sender, String[] args) {
        try {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /achievements check <achievement_id> [player]");
                return true;
            }

            String achievementId = args[1].toLowerCase().trim();
            if (achievementId.isEmpty()) {
                sender.sendMessage("§cAchievement ID cannot be empty.");
                return true;
            }

            Player target;

            if (args.length >= 3) {
                if (!sender.hasPermission("enchadd.achievements.admin")) {
                    sender.sendMessage("§cYou don't have permission to check other players' achievements.");
                    return true;
                }

                String playerName = args[2].trim();
                if (playerName.isEmpty()) {
                    sender.sendMessage("§cPlayer name cannot be empty.");
                    return true;
                }

                target = Bukkit.getPlayer(playerName);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found: " + playerName);
                    return true;
                }
            } else {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cConsole must specify a player name.");
                    return true;
                }
                target = (Player) sender;
            }

            AchievementManager manager = AchievementManager.getInstance();
            if (manager == null) {
                sender.sendMessage("§cAchievement system is not initialized.");
                return true;
            }

            boolean hasAchievement = manager.hasAchievement(target, achievementId);
            String title = ConfigManager.getMessage("achievement." + achievementId + ".title", achievementId);

            if (hasAchievement) {
                sender.sendMessage("§a" + target.getName() + " has completed: §d" + title);
            } else {
                sender.sendMessage("§7" + target.getName() + " has not completed: §d" + title);
                String progress = getProgressString(target, achievementId, manager);
                if (!progress.isEmpty()) {
                    sender.sendMessage("§e" + progress);
                }
            }

            return true;

        } catch (Exception e) {
            com.enadd.util.ErrorHandler.handleException(null, "Achievement check command", e);
            sender.sendMessage("§cError checking achievement. Please try again.");
            return true;
        }
    }

    private boolean handleProgress(CommandSender sender, @SuppressWarnings("unused") String[] args) {
        if (!(sender instanceof Player)) {
            if (sender != null) {
                sender.sendMessage("§cThis command can only be used by players.");
            }
            return true;
        }

        Player player = (Player) sender;
        AchievementManager manager = AchievementManager.getInstance();

        if (manager == null) {
            sender.sendMessage("§cAchievement system is not initialized.");
            return true;
        }

        sender.sendMessage("§5§l=== Your Progress ===");
        sender.sendMessage("§eTrees Cut: §f" + manager.getProgress(player, "trees_cut") + "§7/1000");
        sender.sendMessage("§eSniper Shots: §f" + manager.getProgress(player, "sniper_shots") + "§7/100");
        sender.sendMessage("§eBoss Kills: §f" + manager.getProgress(player, "boss_kills"));

        return true;
    }

    private boolean handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("enchadd.achievements.admin")) {
            sender.sendMessage("§cYou don't have permission to reset achievements.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /achievements reset <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return true;
        }

        AchievementManager manager = AchievementManager.getInstance();
        if (manager == null) {
            sender.sendMessage("§cAchievement system is not initialized.");
            return true;
        }

        // 使用带权限检查的方法
        if (manager.resetAchievementsWithPermission(target, sender)) {
            sender.sendMessage("§aSuccessfully reset achievements for " + target.getName());
            target.sendMessage("§eYour achievements have been reset by an administrator.");
        } else {
            sender.sendMessage("§cFailed to reset achievements. Permission denied.");
        }
        return true;
    }

    private String getProgressString(Player player, String achievementId, AchievementManager manager) {
        switch (achievementId) {
            case "lumberjack":
                int trees = manager.getProgress(player, "trees_cut");
                return "Progress: " + trees + "/1000 trees cut";
            case "marksman":
                int shots = manager.getProgress(player, "sniper_shots");
                return "Progress: " + shots + "/100 long-range hits";
            case "enchantment_master":
                return "Check your inventory and equipment for EnchAdd enchantments";
            default:
                return "";
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage("§5§l=== EnchAdd Achievements ===");
        sender.sendMessage("§d/achievements list §7- Show all achievements and your progress");
        sender.sendMessage("§d/achievements check <id> [player] §7- Check specific achievement");
        sender.sendMessage("§d/achievements progress §7- Show your current progress");

        if (sender.hasPermission("enchadd.achievements.admin")) {
            sender.sendMessage("§c/achievements reset <player> §7- Reset player achievements (Admin)");
        }

        sender.sendMessage("");
        sender.sendMessage("§7Available achievements:");
        sender.sendMessage("§e- enchantment_master §7- Obtain all 51 enchantments");
        sender.sendMessage("§e- cursed_warrior §7- Complete dungeon with all curses");
        sender.sendMessage("§e- one_shot_kill §7- Kill boss with Execution");
        sender.sendMessage("§e- lumberjack §7- Cut 1000 trees with Tree Feller");
        sender.sendMessage("§e- marksman §7- 100 long-range hits with Sniper");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        try {
            if (sender == null || args == null) {
                return completions;
            }

            if (args.length == 1) {
                completions.addAll(Arrays.asList("list", "check", "progress", "help"));
                if (sender.hasPermission("enchadd.achievements.admin")) {
                    completions.add("reset");
                }
            } else if (args.length == 2 && args[0] != null && args[0].equalsIgnoreCase("check")) {
                completions.addAll(Arrays.asList("enchantment_master", "cursed_warrior", "one_shot_kill", "lumberjack", "marksman"));
            } else if (args.length == 3 && args[0] != null && (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("reset"))) {
                try {
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (player != null && player.getName() != null) {
                            completions.add(player.getName());
                        }
                    });
                } catch (Exception e) {
                    // Silent error - tab completion should not fail
                }
            }

            // Filter completions based on what user has typed
            if (args.length > 0 && args[args.length - 1] != null) {
                String partial = args[args.length - 1].toLowerCase();
                completions.removeIf(completion ->
                    completion == null || !completion.toLowerCase().startsWith(partial));
            }

        } catch (Exception e) {
            // Silent error - tab completion should not fail
        }

        return completions;
    }
}
