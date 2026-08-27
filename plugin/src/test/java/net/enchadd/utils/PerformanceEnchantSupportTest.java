package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerformanceEnchantSupportTest {

    @Test
    void equipmentAggregatesReturnZeroForNullEquipmentBeforeEnchantIsUsed() {
        assertEquals(0, PerformanceEnchantSupport.getHighestEnchantLevel(null, null));
        assertEquals(0, PerformanceEnchantSupport.getSumOfEnchantLevels(null, null));
    }
}
