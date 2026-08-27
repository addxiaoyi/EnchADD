package net.enchadd.commands;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

final class EnchantListFilterSummarySupport {

    private EnchantListFilterSummarySupport() {
    }

    static Component summaryLine(EnchantListOptions options, int totalResults) {
        String summary = summarizeFilters(options);
        return Component.text(
                EnchantListTextSupport.localized("EnchADD.command.list.summary", "当前筛选: ")
                        + summary
                        + " · "
                        + EnchantListTextSupport.localized("EnchADD.command.list.count", "结果数: ")
                        + totalResults
        );
    }

    static List<Component> emptyHints(EnchantListOptions options) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.list.empty.hint1", "建议: 先放宽筛选，或改成 sort=weight order=asc slot=MAINHAND")));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.list.empty.hint2", "如果关键词太窄，可以先去掉 keywords 再试一次")));
        if (options != null) {
            if (options.slot != null || options.sourceTier != null || options.minWeight != null || options.maxWeight != null || options.minLevel != null || options.maxLevel != null) {
                lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.list.empty.hint3", "也可以先删掉槽位、来源、权重或等级条件，再逐步缩小范围")));
            }
            if (options.namespace != null && !options.namespace.isBlank()) {
                lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.list.empty.hint4", "命名空间可能太窄了，试试先不填 namespace")));
            }
        }
        return lines;
    }

    static List<Component> searchEmptyHints() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.find.empty.hint1", "建议: 试试更短的关键词，或换成附魔名/别名")));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.find.empty.hint2", "如果还是找不到，可以先用 list 浏览，再用 info 查看详情")));
        return lines;
    }

    private static String summarizeFilters(EnchantListOptions options) {
        if (options == null) {
            return EnchantListTextSupport.localized("EnchADD.command.list.summary.none", "无");
        }
        List<String> parts = new ArrayList<>();
        if (!isDefaultSort(options.sort, options.order)) {
            parts.add(describeSort(options.sort, options.order));
        }
        if (options.slot != null) {
            parts.add(EnchantListTextSupport.localized("EnchADD.command.list.filter.slot", "槽位 ")
                    + EnchantListDisplayMetadataSupport.slotLabel(options.slot));
        }
        if (options.sourceTier != null) {
            parts.add(EnchantListTextSupport.localized("EnchADD.command.list.filter.source_tier", "来源层级 ")
                    + EnchantListDisplayMetadataSupport.sourceTierLabel(options.sourceTier));
        }
        if (options.minWeight != null || options.maxWeight != null) {
            parts.add(EnchantListTextSupport.localized("EnchADD.command.list.filter.weight", "权重 ")
                    + describeRange(options.minWeight, options.maxWeight));
        }
        if (options.minLevel != null || options.maxLevel != null) {
            parts.add(EnchantListTextSupport.localized("EnchADD.command.list.filter.level", "等级 ")
                    + describeRange(options.minLevel, options.maxLevel));
        }
        if (options.namespace != null && !options.namespace.isBlank()) {
            parts.add(EnchantListTextSupport.localized("EnchADD.command.list.filter.namespace", "命名空间 ")
                    + options.namespace.trim());
        }
        if (options.keywords != null && !options.keywords.isEmpty()) {
            parts.add(EnchantListTextSupport.localized("EnchADD.command.list.filter.keywords", "关键词 ")
                    + String.join("、", options.keywords));
        }
        if (parts.isEmpty()) {
            return EnchantListTextSupport.localized("EnchADD.command.list.summary.none", "无");
        }
        StringJoiner joiner = new StringJoiner(" · ");
        for (String part : parts) {
            joiner.add(part);
        }
        return joiner.toString();
    }

    private static boolean isDefaultSort(String sort, String order) {
        return "name".equalsIgnoreCase(sort) && !"desc".equalsIgnoreCase(order);
    }

    private static String describeSort(String sort, String order) {
        String sortLabel = switch (sort == null ? "" : sort.toLowerCase(Locale.ROOT)) {
            case "weight" -> EnchantListTextSupport.localized("EnchADD.command.list.sort.weight", "权重");
            case "max" -> EnchantListTextSupport.localized("EnchADD.command.list.sort.max", "最高等级");
            default -> EnchantListTextSupport.localized("EnchADD.command.list.sort.name", "名称");
        };
        String orderLabel = "desc".equalsIgnoreCase(order)
                ? EnchantListTextSupport.localized("EnchADD.command.list.sort.desc", "降序")
                : EnchantListTextSupport.localized("EnchADD.command.list.sort.asc", "升序");
        return EnchantListTextSupport.localized("EnchADD.command.list.filter.sort", "排序 ")
                + sortLabel
                + " "
                + orderLabel;
    }

    private static String describeRange(Integer min, Integer max) {
        if (min != null && max != null) {
            return min + "-" + max;
        }
        if (min != null) {
            return ">= " + min;
        }
        if (max != null) {
            return "<= " + max;
        }
        return "";
    }
}
