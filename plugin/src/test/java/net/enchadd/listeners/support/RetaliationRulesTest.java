package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RetaliationRulesTest {
    @Test
    void defaultRiposteProgressionIsPreserved() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(level * 40, RetaliationRules.weaknessTicks(level, 3, 2));
            assertEquals(level * 0.25, RetaliationRules.riposteChance(level, 3, 0.25));
        }
    }

    @Test
    void unsafeLevelsCannotExtendWeaknessOrIncreaseChance() {
        assertEquals(120, RetaliationRules.weaknessTicks(Integer.MAX_VALUE, 3, 2));
        assertEquals(0.75, RetaliationRules.riposteChance(Integer.MAX_VALUE, 3, 0.25));
        assertEquals(2400, RetaliationRules.weaknessTicks(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(0.85, RetaliationRules.riposteChance(3, 3, Double.MAX_VALUE));
    }

    @Test
    void invalidLevelsAndDurationsDisableWeakness() {
        assertEquals(0, RetaliationRules.weaknessTicks(0, 3, 2));
        assertEquals(0, RetaliationRules.weaknessTicks(3, 0, 2));
        assertEquals(0, RetaliationRules.weaknessTicks(3, 3, 0));
        assertEquals(0, RetaliationRules.weaknessTicks(3, 3, -1));
        assertEquals(0, RetaliationRules.riposteChance(0, 3, 0.25));
        assertEquals(0, RetaliationRules.riposteChance(3, 0, 0.25));
    }

    @Test
    void invalidProbabilitiesCannotTriggerRiposte() {
        for (double chance : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, RetaliationRules.riposteChance(3, 3, chance));
        }
    }

    @Test
    void parryRequiresUsableWindowAndPositiveBonus() {
        assertTrue(RetaliationRules.canArm(3, 3, 60, 1, 2.5));
        assertFalse(RetaliationRules.canArm(3, 3, 0, 1, 2.5));
        assertFalse(RetaliationRules.canArm(3, 3, -1, 1, 2.5));
        assertFalse(RetaliationRules.canArm(0, 3, 60, 1, 2.5));
        assertFalse(RetaliationRules.canArm(3, 0, 60, 1, 2.5));
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertFalse(RetaliationRules.canArm(3, 3, 60, invalid, 2.5));
            assertFalse(RetaliationRules.canArm(3, 3, 60, 1, invalid));
        }
    }

    @Test
    void existingWeaknessMustActuallyBeImproved() {
        assertFalse(ActiveBuffSupport.canUpgrade(1, 20, 0, 120));
        assertFalse(ActiveBuffSupport.canUpgrade(0, -1, 0, 120));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 200, 0, 120));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 120, 0, 120));
        assertTrue(ActiveBuffSupport.canUpgrade(0, 20, 0, 120));
    }
}
