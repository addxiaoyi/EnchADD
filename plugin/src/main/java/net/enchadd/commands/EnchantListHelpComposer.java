package net.enchadd.commands;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

final class EnchantListHelpComposer {

    private EnchantListHelpComposer() {
    }

    static List<Component> compose(String label) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.help.usage", "用法:")));
        lines.add(Component.text("/" + label + " list [页码] [每页数量] [sort=name|weight|max] [order=asc|desc] [slot=<SLOT>] [source=<" + String.join("|", EnchantListDisplayMetadataSupport.sourceTierFilterValues()) + ">] [minWeight=<n>] [maxWeight=<n>] [minLevel=<n>] [maxLevel=<n>] [namespace=<ns>] [keywords=k1,k2]"));
        lines.add(Component.text("/" + label + " export"));
        lines.add(Component.text("/" + label + " exportjson"));
        lines.add(Component.text("/" + label + " exportcsv"));
        lines.add(Component.text("/" + label + " info <键>"));
        lines.add(Component.text("/" + label + " find <名称关键字>"));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.help.examples", "常用示例:")));
        lines.add(Component.text("1. " + EnchantListTextSupport.localized("EnchADD.command.help.example.find", "搜索附魔: ") + "/" + label + " find 斩首"));
        lines.add(Component.text("2. " + EnchantListTextSupport.localized("EnchADD.command.help.example.info", "查看详情: ") + "/" + label + " info enchadd:beheading"));
        lines.add(Component.text("3. " + EnchantListTextSupport.localized("EnchADD.command.help.example.list", "筛选列表: ") + "/" + label + " list sort=weight order=asc slot=MAINHAND"));
        lines.add(Component.text("4. " + EnchantListTextSupport.localized("EnchADD.command.help.example.source", "来源筛选: ") + "/" + label + " list source=treasure_only"));
        lines.add(Component.text(EnchantListTextSupport.localized("EnchADD.command.help.tip", "提示: 权重越高越常见，宝藏/诅咒会单独标出。")));
        lines.add(Component.text("/" + label + " ci"));
        lines.add(Component.text("/" + label + " perf [触发次数] [粒子次数]"));
        lines.add(Component.text("/" + label + " verify"));
        lines.add(Component.text("/" + label + " balance"));
        lines.add(Component.text("/" + label + " safemode [status|on|off|toggle]"));
        lines.add(Component.text("/" + label + " reload"));
        lines.add(Component.text("/" + label + " legacyscan"));
        return lines;
    }
}
