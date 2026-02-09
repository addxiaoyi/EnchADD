package com.enadd.enchantments.decorative;

import com.enadd.achievements.AchievementManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;









public final class DecorativeEnchantmentManager {

    private final JavaPlugin plugin;
    private final ParticleEffectConfig particleConfig;
    private final ParticleEffectRenderer renderer;
    private final DecorativeEnchantmentConflictManager conflictManager;
    private final PerformanceOptimizer performanceOptimizer;

    public DecorativeEnchantmentManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.particleConfig = new ParticleEffectConfig(plugin);
        this.renderer = new ParticleEffectRenderer(plugin, particleConfig);
        this.conflictManager = new DecorativeEnchantmentConflictManager(plugin);
        this.performanceOptimizer = new PerformanceOptimizer(plugin, particleConfig);
        registerCommands();
    }

    private void registerCommands() {
        Objects.requireNonNull(plugin.getCommand("enchant")).setExecutor(this::handleCommand);

        Objects.requireNonNull(plugin.getCommand("enchant")).setTabCompleter((sender, command, alias, args) -> {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                completions.add("apply");
                completions.add("remove");
                completions.add("list");
                completions.add("info");
                completions.add("preview");
                completions.add("reload");
                return completions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
            } else if (args.length == 2) {
                if ("apply".equalsIgnoreCase(args[0]) || "info".equalsIgnoreCase(args[0])) {
                    return Arrays.stream(DecorativeEnchantmentType.values())
                        .map(DecorativeEnchantmentType::getId)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
            } else if (args.length == 3) {
                if ("apply".equalsIgnoreCase(args[0])) {
                    return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
                }
            }
            return Collections.emptyList();
        });
    }

    private boolean handleCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用此命令");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "apply":
            case "add":
                return handleApply(player, args);
            case "remove":
            case "delete":
                return handleRemove(player, args);
            case "list":
            case "ls":
                return handleList(player);
            case "info":
            case "show":
                return handleInfo(player, args);
            case "preview":
            case "test":
                return handlePreview(player, args);
            case "reload":
            case "rl":
                return handleReload(player);
            case "help":
            case "?":
                sendHelpMessage(player);
                return true;
            default:
                sendHelpMessage(player);
                return true;
        }
    }

    private boolean handleApply(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /enchant apply <附魔类型> [等级]");
            return true;
        }

        String typeId = args[1];
        DecorativeEnchantmentType type = DecorativeEnchantmentType.fromId(typeId);

        if (type == null) {
            player.sendMessage("§c未找到附魔类型: " + typeId);
            sendEnchantmentList(player);
            return true;
        }

        int level = 1;
        if (args.length >= 3) {
            try {
                level = Math.max(1, Math.min(10, Integer.parseInt(args[2])));
            } catch (NumberFormatException e) {
                player.sendMessage("§c无效的等级: " + args[2]);
                return true;
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§c请手持要附魔的物品");
            return true;
        }

        if (!conflictManager.canApplyEnchantment(item, type)) {
            player.sendMessage(conflictManager.getConflictErrorMessage());
            return true;
        }

        DecorativeEnchantment enchantment = new DecorativeEnchantment(type, level);

        if (conflictManager.applyEnchantment(item, enchantment)) {
            player.sendMessage("§a成功应用装饰性附魔: §f" + enchantment.getDisplayName());
            player.sendMessage("§7描述: §f" + type.getDescription());
            renderer.startEffect(player.getLocation(), type, player);
        } else {
            player.sendMessage("§c应用附魔失败");
        }

        return true;
    }

    private boolean handleRemove(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage("§c请手持要移除附魔的物品");
            return true;
        }

        if (!conflictManager.hasDecorativeEnchantment(item)) {
            player.sendMessage("§c该物品没有装饰性附魔");
            return true;
        }

        if (conflictManager.removeDecorativeEnchantment(item)) {
            player.sendMessage("§a已成功移除装饰性附魔");
        } else {
            player.sendMessage("§c移除附魔失败");
        }

        return true;
    }

    private boolean handleList(Player player) {
        player.sendMessage("§6§l=== 装饰性附魔列表 ===");
        player.sendMessage("");

        int index = 1;
        for (DecorativeEnchantmentType type : DecorativeEnchantmentType.values()) {
            String status = particleConfig.getEffectConfig(type.getId()).isEnabled() ? "§a启用" : "§c禁用";
            player.sendMessage(String.format("§7%d. §e%s §7- §f%s §7(%s)",
                index++, type.getDisplayName(), type.getDescription().substring(0, Math.min(30, type.getDescription().length())) + "...", status));
        }

        player.sendMessage("");
        player.sendMessage("§7使用 §e/enchant apply <名称> [等级] §7应用附魔");
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /enchant info <附魔类型>");
            return true;
        }

        String typeId = args[1];
        DecorativeEnchantmentType type = DecorativeEnchantmentType.fromId(typeId);

        if (type == null) {
            player.sendMessage("§c未找到附魔类型: " + typeId);
            return true;
        }

        if (AchievementManager.getInstance() != null) {
            AchievementManager.getInstance().trackSeenEnchantment(player, type.getId());
        }
        sendEnchantmentInfo(player, type);
        return true;
    }

    private boolean handlePreview(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§c用法: /enchant preview <附魔类型>");
            return true;
        }

        String typeId = args[1];
        DecorativeEnchantmentType type = DecorativeEnchantmentType.fromId(typeId);

        if (type == null) {
            player.sendMessage("§c未找到附魔类型: " + typeId);
            return true;
        }

        player.sendMessage("§a正在预览: §f" + type.getDisplayName());
        renderer.startEffect(player.getLocation(), type, player);

        return true;
    }

    private boolean handleReload(Player player) {
        particleConfig.reload();
        player.sendMessage("§a配置已重载");
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§5§l=== 装饰性附魔系统 ===");
        player.sendMessage("§7/enchant apply <类型> [等级] §e- 应用装饰性附魔");
        player.sendMessage("§7/enchant remove §e- 移除装饰性附魔");
        player.sendMessage("§7/enchant list §e- 查看所有可用附魔");
        player.sendMessage("§7/enchant info <类型> §e- 查看附魔详细信息");
        player.sendMessage("§7/enchant preview <类型> §e- 预览附魔效果");
        player.sendMessage("§7/enchant reload §e- 重载配置");
    }

    private void sendEnchantmentList(Player player) {
        player.sendMessage("§7可用附魔类型:");
        StringBuilder sb = new StringBuilder();
        for (DecorativeEnchantmentType type : DecorativeEnchantmentType.values()) {
            if (sb.length() > 0) sb.append("§7, ");
            sb.append("§e").append(type.getId());
        }
        player.sendMessage(sb.toString());
    }

    private void sendEnchantmentInfo(Player player, DecorativeEnchantmentType type) {
        player.sendMessage("");
        player.sendMessage("§6╔════════════════════════════════╗");
        player.sendMessage("§6║ §e" + centerText(type.getDisplayName(), 26) + " §6║");
        player.sendMessage("§6╠════════════════════════════════╣");
        player.sendMessage("§6║ §7ID: §f" + padText(type.getId(), 22) + " §6║");
        player.sendMessage("§6║ §7粒子类型: §f" + padText(getParticleTypeNames(type), 14) + " §6║");
        player.sendMessage("§6╠════════════════════════════════╣");
        player.sendMessage("§6║ §7描述: " + padText("", 19) + " §6║");

        String[] descLines = wrapText(type.getDescription(), 28);
        for (String line : descLines) {
            player.sendMessage("§6║ §f" + padText(line, 26) + " §6║");
        }

        player.sendMessage("§6╠════════════════════════════════╣");
        player.sendMessage("§6║ §7持续时间: §f" + padText(type.getDuration() + "ms", 16) + " §6║");
        player.sendMessage("§6║ §7效果半径: §f" + padText(String.format("%.1f", type.getEffectRadius()), 16) + " §6║");
        player.sendMessage("§6╚════════════════════════════════╝");
        player.sendMessage("");
        player.sendMessage("§7触发概率 (1-10级):");
        for (int i = 1; i <= 10; i += 2) {
            double prob1 = calculateProbability(i);
            double prob2 = calculateProbability(i + 1);
            player.sendMessage(String.format("§7  %d级: %.0f%%  |  %d级: %.0f%%",
                i, prob1 * 100, Math.min(i + 1, 10), prob2 * 100));
        }
    }

    private String getParticleTypeNames(DecorativeEnchantmentType type) {
        return type.getParticleTypes().stream()
            .map(ParticleType::getDisplayName)
            .limit(2)
            .collect(Collectors.joining(", "));
    }

    private double calculateProbability(int level) {
        if (level < 1) return 0.1;
        if (level > 10) return 0.8;

        double baseProbability = 0.1;
        double maxProbability = 0.8;
        double growthExponent = 1.5;

        double normalizedLevel = (level - 1) / 9.0;
        return baseProbability + (maxProbability - baseProbability) * Math.pow(normalizedLevel, growthExponent);
    }

    private String centerText(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) {
            sb.append(" ");
        }
        sb.append(text);
        while (sb.length() < width) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private String padText(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < width) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private String[] wrapText(String text, int maxLength) {
        if (text == null || text.isEmpty()) return new String[]{""};

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines.toArray(new String[0]);
    }

    public void reload() {
        particleConfig.reload();
    }

    public DecorativeEnchantmentConflictManager getConflictManager() {
        return conflictManager;
    }

    public PerformanceOptimizer getPerformanceOptimizer() {
        return performanceOptimizer;
    }
}




