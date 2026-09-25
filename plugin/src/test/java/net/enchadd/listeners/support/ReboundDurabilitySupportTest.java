package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReboundDurabilitySupportTest {
    @Test
    void defaultLevelsRetainTwelvePercentSteps() {
        assertEquals(0.12, ReboundDurabilitySupport.refundChance(1, 1, 3, 0.12), 1.0e-12);
        assertEquals(0.24, ReboundDurabilitySupport.refundChance(1, 2, 3, 0.12), 1.0e-12);
        assertEquals(0.36, ReboundDurabilitySupport.refundChance(1, 3, 3, 0.12), 1.0e-12);
    }

    @Test
    void unsafeItemLevelsCannotBypassConfiguredMaximum() {
        assertEquals(0.36, ReboundDurabilitySupport.refundChance(1, Integer.MAX_VALUE, 3, 0.12), 1.0e-12);
        assertEquals(0.12, ReboundDurabilitySupport.refundChance(1, 3, 1, 0.12), 1.0e-12);
    }

    @Test
    void noDurabilityLossNeverRollsForRefund() {
        assertEquals(0.0, ReboundDurabilitySupport.refundChance(0, 3, 3, 0.12));
        assertEquals(0.0, ReboundDurabilitySupport.refundChance(-1, 3, 3, 0.12));
        assertEquals(0.36, ReboundDurabilitySupport.refundChance(Integer.MAX_VALUE, 3, 3, 0.12), 1.0e-12);
    }

    @Test
    void invalidConfigurationDisablesRefund() {
        for (double rate : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0.0, ReboundDurabilitySupport.refundChance(1, 3, 3, rate));
        }
        assertEquals(0.0, ReboundDurabilitySupport.refundChance(1, 0, 3, 0.12));
        assertEquals(0.0, ReboundDurabilitySupport.refundChance(1, 3, 0, 0.12));
        assertEquals(0.0, ReboundDurabilitySupport.refundChance(1, -1, -1, 0.12));
    }

    @Test
    void largeFiniteRatesRespectExistingChanceCeiling() {
        assertEquals(0.45, ReboundDurabilitySupport.refundChance(1, 3, 3, 0.5));
        assertEquals(0.45, ReboundDurabilitySupport.refundChance(1, Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE));
    }
}
