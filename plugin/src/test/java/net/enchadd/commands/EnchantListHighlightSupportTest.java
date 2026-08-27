package net.enchadd.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantListHighlightSupportTest {

    @Test
    void keepsPlainTextStableWhenHighlighting() {
        Component highlighted = EnchantListHighlightSupport.highlightName("Alpha Strike", List.of("alpha", "strike"));

        assertEquals("Alpha Strike", PlainTextComponentSerializer.plainText().serialize(highlighted));
    }

    @Test
    void treatsKeywordsAsLiteralText() {
        Component highlighted = EnchantListHighlightSupport.highlightName("A+B Guard", List.of("a+b"));

        assertEquals("A+B Guard", PlainTextComponentSerializer.plainText().serialize(highlighted));
    }

    @Test
    void emptyKeywordsReturnUnchangedText() {
        Component highlighted = EnchantListHighlightSupport.highlightName("Beta", List.of());

        assertEquals("Beta", PlainTextComponentSerializer.plainText().serialize(highlighted));
    }
}
