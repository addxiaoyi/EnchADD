package net.enchadd.commands;

import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

final class EnchantListInfoComposer {

    private EnchantListInfoComposer() {
    }

    static List<Component> compose(EnchADDEnchant enchant) {
        String name = EnchantListFormatSupport.resolveName(enchant);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.info.title", "附魔信息")));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.info.name", "名称: ") + name));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.info.key", "键: ") + enchant.getKey().asString()));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.info.max_level", "最大等级: ") + enchant.getMaxLevel()));
        lines.add(EnchantListDisplayMetadataSupport.weightLine(enchant));
        lines.add(EnchantListDisplayMetadataSupport.rarityLine(enchant));
        lines.add(EnchantListDisplayMetadataSupport.sourceTierLine(enchant));
        lines.add(EnchantListDisplayMetadataSupport.acquisitionLine(enchant));
        lines.add(EnchantListDisplayMetadataSupport.treasureLine(enchant));
        lines.add(EnchantListDisplayMetadataSupport.curseLine(enchant));
        lines.add(EnchantListDisplayMetadataSupport.playstyleLine(enchant));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.info.slots", "槽位: ")).append(EnchantListDisplayMetadataSupport.slotBadges(enchant)));
        int supportedCount = enchant.getSupportedItems().size();
        int tagCount = enchant.getEnchantTagKeys().size();
        lines.add(EnchantListDisplayMetadataSupport.conflictsLine(enchant, net.enchadd.EnchADDConfig.ENCHANTS.values()));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.info.supported_tags", "支持物品标签数: ") + supportedCount));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.info.enchant_tags", "附魔标签数: ") + tagCount));
        return lines;
    }
}
