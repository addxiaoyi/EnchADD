package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantListDisplayMetadataSupportTest {

    @Test
    void weightLabelMatchesRebasedScale() {
        assertEquals("常见", EnchantListDisplayMetadataSupport.weightLabel(3));
        assertEquals("稀有", EnchantListDisplayMetadataSupport.weightLabel(2));
        assertEquals("极稀有", EnchantListDisplayMetadataSupport.weightLabel(1));
    }
}
