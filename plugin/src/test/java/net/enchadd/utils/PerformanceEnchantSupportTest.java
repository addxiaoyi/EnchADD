package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerformanceEnchantSupportTest {

    @Test
    void normalEquipmentStacksRetainTheirExistingCap() {
        int total = 0;
        for (int slot = 1; slot <= 6; slot++) {
            total = PerformanceEnchantSupport.addStackedLevel(total, 1);
            assertEquals(Math.min(slot, 4), total);
        }
    }

    @Test
    void unsafeLevelsCannotOverflowOrCancelEquippedEnchants() {
        int total = 0;
        for (int slot = 0; slot < 6; slot++) {
            total = PerformanceEnchantSupport.addStackedLevel(total, Integer.MAX_VALUE);
            assertEquals(4, total);
        }
        assertEquals(4, PerformanceEnchantSupport.addStackedLevel(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(3, PerformanceEnchantSupport.addStackedLevel(3, Integer.MIN_VALUE));
        assertEquals(2, PerformanceEnchantSupport.addStackedLevel(-1, 2));
        assertEquals(0, PerformanceEnchantSupport.addStackedLevel(-1, -1));
    }

    @Test
    void equipmentAggregatesReturnZeroForNullEquipmentBeforeEnchantIsUsed() {
        assertEquals(0, PerformanceEnchantSupport.getHighestEnchantLevel(null, null));
        assertEquals(0, PerformanceEnchantSupport.getSumOfEnchantLevels(null, null));
    }
}
