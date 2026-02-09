package com.enadd.gui.pages;

import com.enadd.gui.EnchantmentGuiManager;
import com.enadd.util.ColorFormatter;
import com.enadd.enchantments.Rarity;
import java.util.ArrayList;
import java.util.List;


public final class LoreRenderer {

    private static final int MAX_LINE_LENGTH = 35;

    public static List<String> renderEnchantmentLore(EnchantmentGuiManager.EnchantmentData data) {
        List<String> lore = new ArrayList<>();

        lore.add(renderRarityLine(data.getRarity()));
        lore.add(renderLevelLine(data.getMaxLevel()));
        lore.add(renderWeightLine(data.getWeight()));
        lore.add("");

        String[] descriptionLines = wrapText(data.getDescription(), MAX_LINE_LENGTH);
        for (String line : descriptionLines) {
            lore.add(format("&7" + line));
        }

        lore.add("");
        lore.add(format("&7适用: &f" + data.getApplicableItems()));

        if (!data.getConflicts().isEmpty()) {
            lore.add("");
            lore.add(format("&c冲突:"));
            List<String> limitedConflicts = data.getConflicts().size() > 3
                ? data.getConflicts().subList(0, 3)
                : data.getConflicts();
            for (String conflict : limitedConflicts) {
                lore.add(format("&c  - " + conflict));
            }
            if (data.getConflicts().size() > 3) {
                lore.add(format("&c  及其他 " + (data.getConflicts().size() - 3) + " 个"));
            }
        }

        lore.add("");
        lore.add(format("&e左键: 详细信息 | 右键: 模拟应用"));

        return lore;
    }

    public static List<String> renderEnchantmentDetails(EnchantmentGuiManager.EnchantmentData data) {
        List<String> details = new ArrayList<>();

        details.add(format("&6╔══════════════════════════════╗"));
        details.add(format("&6║ &e" + centerText(data.getDisplayName(), 26) + " &6║"));
        details.add(format("&6╠══════════════════════════════╣"));
        details.add(format("&6║ &7ID: &f" + padText(data.getId(), 22) + " &6║"));
        details.add(format("&6║ &7稀有度: &f" + padText(getRarityName(data.getRarity()), 16) + " &6║"));
        details.add(format("&6║ &7最大等级: &f" + padText(String.valueOf(data.getMaxLevel()), 15) + " &6║"));
        details.add(format("&6║ &7权重: &f" + padText(String.valueOf(data.getWeight()), 19) + " &6║"));
        details.add(format("&6╠══════════════════════════════╣"));
        details.add(format("&6║ &7描述: " + padText("", 19) + " &6║"));

        String[] descLines = wrapText(data.getDescription(), 28);
        for (String line : descLines) {
            details.add(format("&6║ &f" + padText(line, 26) + " &6║"));
        }

        details.add(format("&6╠══════════════════════════════╣"));
        details.add(format("&6║ &7适用工具: &f" + padText(data.getApplicableItems(), 14) + " &6║"));

        if (!data.getConflicts().isEmpty()) {
            details.add(format("&6╠══════════════════════════════╣"));
            details.add(format("&6║ &c冲突附魔: " + padText("", 16) + " &6║"));
            for (String conflict : data.getConflicts().stream().limit(4).toArray(String[]::new)) {
                details.add(format("&6║ &c  - " + padText(conflict, 22) + " &6║"));
            }
        }

        details.add(format("&6╚══════════════════════════════╝"));

        return details;
    }

    private static String renderRarityLine(com.enadd.enchantments.Rarity rarity) {
        String color = getRarityColor(rarity);
        String name = getRarityName(rarity);
        return format(color + "[" + name + "]" + " &7稀有度");
    }

    private static String renderLevelLine(int maxLevel) {
        StringBuilder bars = new StringBuilder();
        int filledBars = (int) Math.ceil(maxLevel / 5.0 * 10);
        for (int i = 0; i < 10; i++) {
            if (i < filledBars) {
                bars.append("▇");
            } else {
                bars.append("▁");
            }
        }
        return format("&7等级: &e" + maxLevel + " &8" + bars.toString());
    }

    private static String renderWeightLine(int weight) {
        StringBuilder dots = new StringBuilder();
        int filledDots = (int) Math.ceil(weight / 100.0 * 5);
        for (int i = 0; i < 5; i++) {
            if (i < filledDots) {
                dots.append("●");
            } else {
                dots.append("○");
            }
        }
        return format("&7权重: &e" + weight + " &8(" + dots.toString() + " &8)");
    }

    public static String format(String text) {
        if (text == null) return "";
        return ColorFormatter.format(text);
    }

    public static String[] wrapText(String text, int maxLength) {
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

    private static String centerText(String text, int width) {
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

    private static String padText(String text, int width) {
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

    private static String getRarityColor(com.enadd.enchantments.Rarity rarity) {
        switch (rarity) {
            case LEGENDARY: return "&6";
            case EPIC: return "&5";
            case VERY_RARE: return "&d";
            case RARE: return "&3";
            case UNCOMMON: return "&2";
            case COMMON: return "&f";
            default: return "&f";
        }
    }

    private static String getRarityName(com.enadd.enchantments.Rarity rarity) {
        switch (rarity) {
            case LEGENDARY: return "传奇";
            case EPIC: return "史诗";
            case VERY_RARE: return "极稀有";
            case RARE: return "稀有";
            case UNCOMMON: return "优秀";
            case COMMON: return "普通";
            default: return "普通";
        }
    }
}
