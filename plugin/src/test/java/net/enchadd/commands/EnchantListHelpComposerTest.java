package net.enchadd.commands;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantListHelpComposerTest {

    @Test
    void includesEveryDocumentedSubcommandWithProvidedLabel() {
        List<String> lines = EnchantListHelpComposer.compose("enchadd").stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .toList();

        assertEquals("用法:", lines.getFirst());
        assertTrue(lines.contains("/enchadd export"));
        assertTrue(lines.contains("/enchadd exportjson"));
        assertTrue(lines.contains("/enchadd exportcsv"));
        assertTrue(lines.contains("/enchadd info <键>"));
        assertTrue(lines.contains("/enchadd find <名称关键字>"));
        assertTrue(lines.contains("常用示例:"));
        assertTrue(lines.contains("1. 搜索附魔: /enchadd find 斩首"));
        assertTrue(lines.contains("2. 查看详情: /enchadd info enchadd:beheading"));
        assertTrue(lines.contains("3. 筛选列表: /enchadd list sort=weight order=asc slot=MAINHAND"));
        assertTrue(lines.contains("4. 来源筛选: /enchadd list source=treasure_only"));
        assertTrue(lines.contains("提示: 权重越高越常见，宝藏/诅咒会单独标出。"));
        assertTrue(lines.contains("/enchadd ci"));
        assertTrue(lines.contains("/enchadd perf [触发次数] [粒子次数]"));
        assertTrue(lines.contains("/enchadd verify"));
        assertTrue(lines.contains("/enchadd balance"));
        assertTrue(lines.contains("/enchadd safemode [status|on|off|toggle]"));
        assertTrue(lines.contains("/enchadd reload"));
        assertTrue(lines.contains("/enchadd legacyscan"));
    }
}
