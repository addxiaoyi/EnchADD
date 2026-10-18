package net.enchadd.mod.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FirebreakRulesTest {
    @Test
    void shortensCombustionUsingPaperDefaultPerLevelValues() {
        assertEquals(80, FirebreakRules.adjustBurnTicks(100, 1));
        assertEquals(40, FirebreakRules.adjustBurnTicks(100, 3));
    }

    @Test
    void clampsLevelsAndLeavesNonPositiveDurationsAlone() {
        assertEquals(40, FirebreakRules.adjustBurnTicks(100, Integer.MAX_VALUE));
        assertEquals(0, FirebreakRules.adjustBurnTicks(0, 3));
        assertEquals(-4, FirebreakRules.adjustBurnTicks(-4, 3));
    }

    @Test
    void reducesFireDamageByPaperDefaultValues() {
        assertEquals(9.2f, FirebreakRules.adjustDamage(10.0f, 1), 1.0e-6f);
        assertEquals(7.6f, FirebreakRules.adjustDamage(10.0f, 3), 1.0e-6f);
    }

    @Test
    void keepsInvalidOrNonPositiveDamageUnchanged() {
        assertEquals(0.0f, FirebreakRules.adjustDamage(0.0f, 3));
        assertEquals(-1.0f, FirebreakRules.adjustDamage(-1.0f, 3));
        assertEquals(Float.NaN, FirebreakRules.adjustDamage(Float.NaN, 3));
        assertEquals(Float.POSITIVE_INFINITY,
                FirebreakRules.adjustDamage(Float.POSITIVE_INFINITY, 3));
    }
}
