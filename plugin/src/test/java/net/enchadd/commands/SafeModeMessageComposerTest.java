package net.enchadd.commands;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafeModeMessageComposerTest {

    @Test
    void disabledAndUsageMessagesStayStable() {
        assertEquals(
                "[ENCHADD-SAFEMODE] 配置已禁用 safety-mode.enabled，当前不可开启。",
                plain(SafeModeMessageComposer.disabled())
        );
        assertEquals(
                "[ENCHADD-SAFEMODE] 用法: /enchadd safemode [status|on|off|toggle]",
                plain(SafeModeMessageComposer.usage())
        );
    }

    @Test
    void statusWrapsPlainTextStatus() {
        assertEquals("status-line", plain(SafeModeMessageComposer.status("status-line")));
    }

    private static String plain(net.kyori.adventure.text.Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
