package net.enchadd.commands;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerfInjectionMessageComposerTest {

    @Test
    void errorMessagesStayStable() {
        assertEquals("[ENCHADD-PERF] 无法执行：当前没有已注册附魔。", plain(PerfInjectionMessageComposer.noEnchants()));
        assertEquals("[ENCHADD-PERF] 当前没有可用世界，仅执行触发计数注入。", plain(PerfInjectionMessageComposer.noWorlds()));
    }

    @Test
    void queuedMessageIncludesCountsKeyAndBatchSizes() {
        assertEquals(
                "[ENCHADD-PERF] queued triggers=1200 particles=600 key=enchadd:probe batch(trigger=5000,particle=1000)",
                plain(PerfInjectionMessageComposer.queued(1200, 600, Key.key("enchadd:probe"), 5000, 1000))
        );
    }

    @Test
    void doneStatusAndMessageStayStable() {
        String status = PerfInjectionMessageComposer.doneStatus(1200, 600, Key.key("enchadd:probe"));

        assertEquals("[ENCHADD-PERF] triggers=1200 particles=600 key=enchadd:probe", status);
        assertEquals(status, plain(PerfInjectionMessageComposer.done(status)));
    }

    private static String plain(net.kyori.adventure.text.Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
