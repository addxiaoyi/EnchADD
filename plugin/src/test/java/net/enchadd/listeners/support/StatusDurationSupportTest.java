package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusDurationSupportTest {
    @Test
    void defaultMeleeDurationsKeepTwoSecondSteps() {
        assertEquals(40, StatusDurationSupport.onHit(5, 1, 3, 2));
        assertEquals(80, StatusDurationSupport.onHit(5, 2, 3, 2));
        assertEquals(120, StatusDurationSupport.onHit(5, 3, 3, 2));
    }

    @Test
    void levelLimitsFollowServerConfiguration() {
        assertEquals(120, StatusDurationSupport.onHit(5, Integer.MAX_VALUE, 3, 2));
        assertEquals(40, StatusDurationSupport.onHit(5, 3, 1, 2));
        assertEquals(200, StatusDurationSupport.onHit(5, 5, 5, 2));
    }

    @Test
    void nonDamagingHitsNeverApplyStatus() {
        for (double damage : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, StatusDurationSupport.onHit(damage, 3, 3, 2));
        }
        assertEquals(120, StatusDurationSupport.onHit(0.01, 3, 3, 2));
    }

    @Test
    void disabledOrNegativeInputsDoNotBecomeOneSecondEffects() {
        for (int invalid : new int[]{0, -1, Integer.MIN_VALUE}) {
            assertEquals(0, StatusDurationSupport.onHit(5, invalid, 3, 2));
            assertEquals(0, StatusDurationSupport.onHit(5, 3, invalid, 2));
            assertEquals(0, StatusDurationSupport.onHit(5, 3, 3, invalid));
        }
    }

    @Test
    void extremeConfigurationRespectsExistingDurationCeiling() {
        assertEquals(2400, StatusDurationSupport.onHit(5, Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(2400, StatusDurationSupport.onHit(5, 3, 3, 40));
        assertEquals(2400, StatusDurationSupport.onHit(5, 3, 3, 41));
    }
}
