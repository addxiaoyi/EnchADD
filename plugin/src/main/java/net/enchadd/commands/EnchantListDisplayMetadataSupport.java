package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.enchadd.config.EnchantAcquisitionPolicy;
import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class EnchantListDisplayMetadataSupport {

    private EnchantListDisplayMetadataSupport() {
    }

    static Component compactSummary(EnchADDEnchant enchant) {
        return Component.empty()
                .append(slotBadges(enchant))
                .append(Component.space())
                .append(weightBadge(enchant))
                .append(Component.space())
                .append(sourceTierBadge(enchant))
                .append(Component.space())
                .append(rarityBadge(enchant));
    }

    static Component slotBadges(EnchADDEnchant enchant) {
        List<Component> badges = new ArrayList<>();
        for (EquipmentSlotGroup slot : normalizeSlots(enchant.getActiveSlots())) {
            badges.add(slotBadge(slot));
        }
        if (badges.isEmpty()) {
            badges.add(slotBadge(EquipmentSlotGroup.ANY));
        }
        Component line = Component.empty();
        for (int i = 0; i < badges.size(); i++) {
            if (i > 0) {
                line = line.append(Component.space());
            }
            line = line.append(badges.get(i));
        }
        return line;
    }

    static Component weightBadge(EnchADDEnchant enchant) {
        String label = weightLabel(enchant.getWeight());
        return badge(
                label + "(" + enchant.getWeight() + ")",
                weightColor(enchant.getWeight())
        );
    }

    static Component rarityBadge(EnchADDEnchant enchant) {
        String rarity = rarityLabel(enchant.getRarity());
        return badge(rarity, rarityColor(enchant.getRarity()));
    }

    static Component sourceTierBadge(EnchADDEnchant enchant) {
        SourceTier tier = sourceTier(enchant);
        return badge(sourceTierLabel(tier), tier.color());
    }

    static Component weightLine(EnchADDEnchant enchant) {
        return Component.text(
                EnchantListTextSupport.localized("EnchADD.command.info.weight", "权重: ")
                        + weightLabel(enchant.getWeight())
                        + "(" + enchant.getWeight() + ")"
        );
    }

    static Component sourceTierLine(EnchADDEnchant enchant) {
        SourceTier tier = sourceTier(enchant);
        return Component.text(
                EnchantListTextSupport.localized("EnchADD.command.info.source_tier", "来源层级: ")
                        + sourceTierLabel(tier)
        );
    }

    static Component rarityLine(EnchADDEnchant enchant) {
        return Component.text(
                EnchantListTextSupport.localized("EnchADD.command.info.rarity", "稀有度: ")
                        + rarityLabel(enchant.getRarity())
        );
    }

    static Component acquisitionLine(EnchADDEnchant enchant) {
        return Component.text(
                EnchantListTextSupport.localized("EnchADD.command.info.acquisition", "获取难度: ")
                        + acquisitionLabel(sourceTier(enchant))
        );
    }

    static Component treasureLine(EnchADDEnchant enchant) {
        return Component.text(
                EnchantListTextSupport.localized("EnchADD.command.info.treasure", "宝藏: ")
                        + yesNo(sourceTier(enchant).treasure())
        );
    }

    static Component curseLine(EnchADDEnchant enchant) {
        return Component.text(
                EnchantListTextSupport.localized("EnchADD.command.info.curse", "诅咒: ")
                        + yesNo(sourceTier(enchant).curse())
        );
    }

    static Component playstyleLine(EnchADDEnchant enchant) {
        return Component.text(
                EnchantListTextSupport.localized("EnchADD.command.info.playstyle", "适合流派: ")
                        + playstyleLabel(enchant)
        );
    }

    static Component conflictsLine(EnchADDEnchant enchant, Iterable<EnchADDEnchant> pool) {
        List<String> names = commonConflictNames(enchant, pool);
        if (names.isEmpty()) {
            return Component.text(EnchantListTextSupport.localized("EnchADD.command.info.conflicts.none", "常见冲突: 无"));
        }
        return Component.text(
                EnchantListTextSupport.localized("EnchADD.command.info.conflicts", "常见冲突: ")
                        + String.join("、", names)
        );
    }

    static List<String> commonConflictNames(EnchADDEnchant enchant, Iterable<EnchADDEnchant> pool) {
        List<String> names = new ArrayList<>();
        if (pool == null) {
            return names;
        }
        for (EnchADDEnchant other : pool) {
            if (other == null || other == enchant) {
                continue;
            }
            Key otherKey = other.getKey();
            if (otherKey == null) {
                continue;
            }
            if (EnchADDConfig.areIncompatible(enchant.getKey(), otherKey)) {
                names.add(EnchantListFormatSupport.resolveName(other));
            }
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    static SourceTier sourceTier(EnchADDEnchant enchant) {
        String value = normalizeKeyValue(enchant.getKey());
        if (EnchantAcquisitionPolicy.CURSE_ENCHANTS.contains(value) || value.endsWith("_curse") || EnchADDConfig.isCurse(enchant.getKey())) {
            return SourceTier.CURSE_TREASURE;
        }
        if (EnchantAcquisitionPolicy.TREASURE_ONLY_ENCHANTS.contains(value)) {
            return SourceTier.TREASURE_ONLY;
        }
        if (EnchantAcquisitionPolicy.TREASURE_SPECIAL_ENCHANTS.contains(value)) {
            return SourceTier.TREASURE_SPECIAL;
        }
        if (EnchantAcquisitionPolicy.TABLE_SPECIAL_ENCHANTS.contains(value)) {
            return SourceTier.TABLE_SPECIAL;
        }
        if (EnchantAcquisitionPolicy.TABLE_COMMON_ENCHANTS.contains(value)) {
            return SourceTier.TABLE_COMMON;
        }
        return SourceTier.UNKNOWN;
    }

    static SourceTier parseSourceTier(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "table_common" -> SourceTier.TABLE_COMMON;
            case "table_special" -> SourceTier.TABLE_SPECIAL;
            case "treasure_special" -> SourceTier.TREASURE_SPECIAL;
            case "treasure_only" -> SourceTier.TREASURE_ONLY;
            case "curse", "curse_treasure" -> SourceTier.CURSE_TREASURE;
            case "unknown" -> SourceTier.UNKNOWN;
            default -> null;
        };
    }

    static List<String> sourceTierFilterValues() {
        return List.of("table_common", "table_special", "treasure_special", "treasure_only", "curse", "unknown");
    }

    static String weightLabel(int weight) {
        // Match the rebased 1-3 weight scale so the badge stays readable.
        if (weight >= 3) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.weight.common", "常见");
        }
        if (weight >= 2) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.weight.rare", "稀有");
        }
        return EnchantListTextSupport.localized("EnchADD.command.meta.weight.very_rare", "极稀有");
    }

    static String rarityLabel(String rarity) {
        String normalized = rarity == null ? "" : rarity.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "COMMON" -> EnchantListTextSupport.localized("EnchADD.command.meta.rarity.common", "普通");
            case "UNCOMMON" -> EnchantListTextSupport.localized("EnchADD.command.meta.rarity.uncommon", "不常见");
            case "RARE" -> EnchantListTextSupport.localized("EnchADD.command.meta.rarity.rare", "稀有");
            case "VERY_RARE" -> EnchantListTextSupport.localized("EnchADD.command.meta.rarity.very_rare", "极稀有");
            case "TREASURE" -> EnchantListTextSupport.localized("EnchADD.command.meta.rarity.treasure", "宝藏");
            case "CURSE" -> EnchantListTextSupport.localized("EnchADD.command.meta.rarity.curse", "诅咒");
            default -> rarity == null || rarity.isBlank()
                    ? EnchantListTextSupport.localized("EnchADD.command.meta.rarity.unknown", "未知")
                    : rarity;
        };
    }

    static String sourceTierLabel(SourceTier tier) {
        return switch (tier) {
            case TABLE_COMMON -> EnchantListTextSupport.localized("EnchADD.command.meta.source.table_common", "附魔台常见");
            case TABLE_SPECIAL -> EnchantListTextSupport.localized("EnchADD.command.meta.source.table_special", "附魔台稀有");
            case TREASURE_SPECIAL -> EnchantListTextSupport.localized("EnchADD.command.meta.source.treasure_special", "宝藏稀有");
            case TREASURE_ONLY -> EnchantListTextSupport.localized("EnchADD.command.meta.source.treasure_only", "宝藏专属");
            case CURSE_TREASURE -> EnchantListTextSupport.localized("EnchADD.command.meta.source.curse_treasure", "诅咒");
            case UNKNOWN -> EnchantListTextSupport.localized("EnchADD.command.meta.source.unknown", "未知来源");
        };
    }

    static String acquisitionLabel(SourceTier tier) {
        return switch (tier) {
            case TABLE_COMMON -> EnchantListTextSupport.localized("EnchADD.command.meta.difficulty.low", "低");
            case TABLE_SPECIAL -> EnchantListTextSupport.localized("EnchADD.command.meta.difficulty.medium", "中");
            case TREASURE_SPECIAL -> EnchantListTextSupport.localized("EnchADD.command.meta.difficulty.high", "高");
            case TREASURE_ONLY -> EnchantListTextSupport.localized("EnchADD.command.meta.difficulty.very_high", "很高");
            case CURSE_TREASURE -> EnchantListTextSupport.localized("EnchADD.command.meta.difficulty.risky", "风险高");
            case UNKNOWN -> EnchantListTextSupport.localized("EnchADD.command.meta.difficulty.unknown", "未知");
        };
    }

    static String playstyleLabel(EnchADDEnchant enchant) {
        SourceTier tier = sourceTier(enchant);
        Set<EquipmentSlotGroup> slots = new LinkedHashSet<>();
        for (EquipmentSlotGroup slot : enchant.getActiveSlots()) {
            slots.add(slot);
        }
        boolean mainHand = slots.contains(EquipmentSlotGroup.MAINHAND) || slots.contains(EquipmentSlotGroup.HAND);
        boolean armor = slots.contains(EquipmentSlotGroup.ARMOR)
                || slots.contains(EquipmentSlotGroup.BODY)
                || slots.contains(EquipmentSlotGroup.HEAD)
                || slots.contains(EquipmentSlotGroup.CHEST)
                || slots.contains(EquipmentSlotGroup.LEGS)
                || slots.contains(EquipmentSlotGroup.FEET);
        boolean offHand = slots.contains(EquipmentSlotGroup.OFFHAND);
        boolean saddle = slots.contains(EquipmentSlotGroup.SADDLE);
        boolean feet = slots.contains(EquipmentSlotGroup.FEET);
        boolean any = slots.contains(EquipmentSlotGroup.ANY);

        if (tier == SourceTier.CURSE_TREASURE) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.playstyle.risky", "风险玩法");
        }
        if (tier == SourceTier.TREASURE_ONLY || tier == SourceTier.TREASURE_SPECIAL) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.playstyle.endgame", "后期构筑");
        }
        if (any) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.playstyle.general", "泛用");
        }
        if (mainHand && armor) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.playstyle.balanced", "战斗 / 生存");
        }
        if (mainHand) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.playstyle.combat", "战斗");
        }
        if (offHand || saddle) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.playstyle.utility", "辅助");
        }
        if (feet) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.playstyle.mobility", "机动");
        }
        if (armor) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.playstyle.survival", "生存");
        }
        return EnchantListTextSupport.localized("EnchADD.command.meta.playstyle.general", "泛用");
    }

    static String yesNo(boolean value) {
        return value
                ? EnchantListTextSupport.localized("EnchADD.command.meta.yes", "是")
                : EnchantListTextSupport.localized("EnchADD.command.meta.no", "否");
    }

    static List<EquipmentSlotGroup> normalizeSlots(Iterable<EquipmentSlotGroup> slots) {
        Set<EquipmentSlotGroup> normalized = new LinkedHashSet<>();
        if (slots != null) {
            for (EquipmentSlotGroup slot : slots) {
                if (slot != null) {
                    normalized.add(slot);
                }
            }
        }
        return new ArrayList<>(normalized);
    }

    private static Component slotBadge(EquipmentSlotGroup slot) {
        return badge(
                slotIcon(slot) + " " + slotLabel(slot),
                slotColor(slot)
        );
    }

    static String slotLabel(EquipmentSlotGroup slot) {
        if (slot == EquipmentSlotGroup.ANY) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.any", "泛用");
        }
        if (slot == EquipmentSlotGroup.HAND) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.hand", "双手");
        }
        if (slot == EquipmentSlotGroup.ARMOR) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.armor", "护甲");
        }
        if (slot == EquipmentSlotGroup.BODY) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.body", "身体");
        }
        if (slot == EquipmentSlotGroup.MAINHAND) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.mainhand", "主手");
        }
        if (slot == EquipmentSlotGroup.OFFHAND) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.offhand", "副手");
        }
        if (slot == EquipmentSlotGroup.HEAD) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.head", "头部");
        }
        if (slot == EquipmentSlotGroup.CHEST) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.chest", "胸甲");
        }
        if (slot == EquipmentSlotGroup.LEGS) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.legs", "护腿");
        }
        if (slot == EquipmentSlotGroup.FEET) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.feet", "靴子");
        }
        if (slot == EquipmentSlotGroup.SADDLE) {
            return EnchantListTextSupport.localized("EnchADD.command.meta.slot.saddle", "鞍具");
        }
        return slot == null ? "" : slot.toString();
    }

    private static String slotIcon(EquipmentSlotGroup slot) {
        if (slot == EquipmentSlotGroup.ANY) {
            return "✦";
        }
        if (slot == EquipmentSlotGroup.HAND) {
            return "⚔";
        }
        if (slot == EquipmentSlotGroup.ARMOR) {
            return "🛡";
        }
        if (slot == EquipmentSlotGroup.BODY) {
            return "◉";
        }
        if (slot == EquipmentSlotGroup.MAINHAND) {
            return "⚔";
        }
        if (slot == EquipmentSlotGroup.OFFHAND) {
            return "✚";
        }
        if (slot == EquipmentSlotGroup.HEAD) {
            return "⬆";
        }
        if (slot == EquipmentSlotGroup.CHEST) {
            return "■";
        }
        if (slot == EquipmentSlotGroup.LEGS) {
            return "≈";
        }
        if (slot == EquipmentSlotGroup.FEET) {
            return "⌁";
        }
        if (slot == EquipmentSlotGroup.SADDLE) {
            return "▣";
        }
        return "?";
    }

    private static TextColor slotColor(EquipmentSlotGroup slot) {
        if (slot == EquipmentSlotGroup.ANY) {
            return NamedTextColor.GRAY;
        }
        if (slot == EquipmentSlotGroup.HAND) {
            return NamedTextColor.GOLD;
        }
        if (slot == EquipmentSlotGroup.ARMOR) {
            return NamedTextColor.AQUA;
        }
        if (slot == EquipmentSlotGroup.BODY) {
            return NamedTextColor.AQUA;
        }
        if (slot == EquipmentSlotGroup.MAINHAND) {
            return NamedTextColor.RED;
        }
        if (slot == EquipmentSlotGroup.OFFHAND) {
            return NamedTextColor.BLUE;
        }
        if (slot == EquipmentSlotGroup.HEAD) {
            return NamedTextColor.LIGHT_PURPLE;
        }
        if (slot == EquipmentSlotGroup.CHEST) {
            return NamedTextColor.GREEN;
        }
        if (slot == EquipmentSlotGroup.LEGS) {
            return NamedTextColor.YELLOW;
        }
        if (slot == EquipmentSlotGroup.FEET) {
            return NamedTextColor.GOLD;
        }
        if (slot == EquipmentSlotGroup.SADDLE) {
            return NamedTextColor.GOLD;
        }
        return NamedTextColor.GRAY;
    }

    private static Component weightBadge(int weight) {
        return badge(
                weightLabel(weight) + "(" + weight + ")",
                weightColor(weight)
        );
    }

    private static TextColor weightColor(int weight) {
        if (weight >= 3) {
            return NamedTextColor.GREEN;
        }
        if (weight >= 2) {
            return NamedTextColor.GOLD;
        }
        return NamedTextColor.LIGHT_PURPLE;
    }

    private static TextColor rarityColor(String rarity) {
        String normalized = rarity == null ? "" : rarity.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "COMMON" -> NamedTextColor.GREEN;
            case "UNCOMMON" -> NamedTextColor.YELLOW;
            case "RARE" -> NamedTextColor.GOLD;
            case "VERY_RARE" -> NamedTextColor.LIGHT_PURPLE;
            case "TREASURE" -> NamedTextColor.AQUA;
            case "CURSE" -> NamedTextColor.RED;
            default -> NamedTextColor.GRAY;
        };
    }

    private static Component badge(String label, TextColor color) {
        return Component.text("[" + label + "]", color);
    }

    private static String normalizeKeyValue(Key key) {
        if (key == null) {
            return "";
        }
        String value = key.value().toLowerCase(Locale.ROOT);
        if (value.endsWith("_curse")) {
            return value.substring(0, value.length() - "_curse".length());
        }
        return value;
    }

    enum SourceTier {
        TABLE_COMMON(NamedTextColor.GREEN, false, false),
        TABLE_SPECIAL(NamedTextColor.YELLOW, false, false),
        TREASURE_SPECIAL(NamedTextColor.GOLD, true, false),
        TREASURE_ONLY(NamedTextColor.LIGHT_PURPLE, true, false),
        CURSE_TREASURE(NamedTextColor.RED, true, true),
        UNKNOWN(NamedTextColor.GRAY, false, false);

        private final TextColor color;
        private final boolean treasure;
        private final boolean curse;

        SourceTier(TextColor color, boolean treasure, boolean curse) {
            this.color = color;
            this.treasure = treasure;
            this.curse = curse;
        }

        TextColor color() {
            return color;
        }

        boolean treasure() {
            return treasure;
        }

        boolean curse() {
            return curse;
        }
    }
}
