package net.enchadd.commands;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReloadMessageComposerTest {

    @Test
    void failureMessageStaysStable() {
        assertEquals(
                "[ENCHADD-RELOAD] 配置重载失败，请检查控制台日志。",
                PlainTextComponentSerializer.plainText().serialize(ReloadMessageComposer.failure())
        );
    }

    @Test
    void successWrapsStatusText() {
        assertEquals(
                "[ENCHADD-RELOAD] status=OK",
                PlainTextComponentSerializer.plainText().serialize(ReloadMessageComposer.success("[ENCHADD-RELOAD] status=OK"))
        );
    }
}
