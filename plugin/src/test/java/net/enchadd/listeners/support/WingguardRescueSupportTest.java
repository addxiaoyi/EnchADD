package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WingguardRescueSupportTest {
    @Test
    void defaultRescueChancesRemainFortyFiveAndNinetyPercent() {
        assertEquals(0.45, WingguardRescueSupport.triggerChance(1, 2, 0.45, 0.90));
        assertEquals(0.90, WingguardRescueSupport.triggerChance(2, 2, 0.45, 0.90));
        assertEquals(0.90, WingguardRescueSupport.triggerChance(Integer.MAX_VALUE, 2, 0.45, 0.90));
    }

    @Test
    void customLevelAndChanceLimitsAreRespected() {
        assertEquals(0.45, WingguardRescueSupport.triggerChance(2, 1, 0.45, 0.90));
        assertEquals(0.50, WingguardRescueSupport.triggerChance(2, 2, 0.45, 0.50));
        assertEquals(1.0, WingguardRescueSupport.triggerChance(Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    void invalidLevelsCannotTriggerARescue() {
        for (int invalid : new int[]{0, -1, Integer.MIN_VALUE}) {
            assertEquals(0, WingguardRescueSupport.triggerChance(invalid, 2, 0.45, 0.90));
            assertEquals(0, WingguardRescueSupport.triggerChance(2, invalid, 0.45, 0.90));
        }
    }

    @Test
    void invalidOrDisabledChanceSettingsCannotGrantAnAutomaticRescue() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, WingguardRescueSupport.triggerChance(2, 2, invalid, 0.90));
            assertEquals(0, WingguardRescueSupport.triggerChance(2, 2, 0.45, invalid));
        }
    }
}
