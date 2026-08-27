package net.enchadd.commands;

import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

final class EnchantListResultComposer {

    private EnchantListResultComposer() {
    }

    static List<Component> composeFindResults(List<EnchADDEnchant> enchants) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.find.results", "搜索结果")));
        if (enchants == null || enchants.isEmpty()) {
            lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.find.empty", "没有找到符合条件的附魔")));
            lines.addAll(EnchantListFilterSummarySupport.searchEmptyHints());
            return lines;
        }
        for (EnchADDEnchant enchant : enchants) {
            String name = EnchantListFormatSupport.resolveName(enchant);
            lines.add(Component.text("• ").append(Component.text(name)).append(Component.space()).append(EnchantListDisplayMetadataSupport.compactSummary(enchant)));
        }
        return lines;
    }

    static List<Component> composeListResults(List<EnchADDEnchant> enchants, int page, int size, boolean adjusted, EnchantListOptions options) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.list.title", "附魔百科")));
        lines.add(EnchantListFilterSummarySupport.summaryLine(options, enchants == null ? 0 : enchants.size()));
        if (enchants == null || enchants.isEmpty()) {
            lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.list.empty", "没有符合条件的附魔")));
            lines.addAll(EnchantListFilterSummarySupport.emptyHints(options));
            return lines;
        }
        if (adjusted) {
            lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.list.adjusted", "页码或每页数量已调整")));
        }
        int totalPages = (int) Math.ceil(Math.max(enchants.size(), 1) / (double) size);
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.list.page", "页码 ") + page + "/" + totalPages));
        List<EnchADDEnchant> pageItems = EnchantListPaginationSupport.pageItems(enchants, page, size);
        for (EnchADDEnchant enchant : pageItems) {
            String name = EnchantListFormatSupport.resolveName(enchant);
            if (options.keywords != null && !options.keywords.isEmpty()) {
                lines.add(Component.text("• ").append(EnchantListHighlightSupport.highlightName(name, options.keywords)).append(Component.space()).append(EnchantListDisplayMetadataSupport.compactSummary(enchant)));
            } else {
                lines.add(Component.text("• ").append(Component.text(name)).append(Component.space()).append(EnchantListDisplayMetadataSupport.compactSummary(enchant)));
            }
        }
        return lines;
    }
}
