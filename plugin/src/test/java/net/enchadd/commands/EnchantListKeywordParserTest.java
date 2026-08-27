package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantListKeywordParserTest {

    @Test
    void parseTrimsLowercasesAndSkipsEmptyParts() {
        assertEquals(List.of("alpha", "斩首"), EnchantListKeywordParser.parse(" Alpha ,, 斩首 "));
    }

    @Test
    void parseNullReturnsEmptyList() {
        assertEquals(List.of(), EnchantListKeywordParser.parse(null));
    }
}
