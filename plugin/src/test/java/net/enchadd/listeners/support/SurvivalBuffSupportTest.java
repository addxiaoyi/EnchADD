package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SurvivalBuffSupportTest {
    @Test
    void fortitudeRetainsDefaultRegenerationAndResistanceProgression() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(level * 3, SurvivalBuffSupport.durationSeconds(level, 3, 3));
            assertEquals(level * 2, SurvivalBuffSupport.durationSeconds(level, 3, 2));
        }
    }

    @Test
    void wingguardRetainsItsFourAndEightSecondSafetyBuffs() {
        assertEquals(4, SurvivalBuffSupport.durationSeconds(1, 2, 4));
        assertEquals(8, SurvivalBuffSupport.durationSeconds(2, 2, 4));
    }

    @Test
    void disabledEffectsAndInvalidLevelsCannotCreateOneSecondBuffs() {
        for (int invalid : new int[]{0, -1, Integer.MIN_VALUE}) {
            assertEquals(0, SurvivalBuffSupport.durationSeconds(3, 3, invalid));
            assertEquals(0, SurvivalBuffSupport.durationSeconds(invalid, 3, 3));
            assertEquals(0, SurvivalBuffSupport.durationSeconds(3, invalid, 3));
        }
        assertEquals(0, ActiveBuffSupport.durationTicks(SurvivalBuffSupport.durationSeconds(2, 2, 0)));
    }

    @Test
    void unsafeEnchantLevelsCannotExtendBuffsPastConfiguredMaximum() {
        assertEquals(9, SurvivalBuffSupport.durationSeconds(Integer.MAX_VALUE, 3, 3));
        assertEquals(6, SurvivalBuffSupport.durationSeconds(Integer.MAX_VALUE, 3, 2));
        assertEquals(8, SurvivalBuffSupport.durationSeconds(Integer.MAX_VALUE, 2, 4));
        assertEquals(3, SurvivalBuffSupport.durationSeconds(3, 1, 3));
    }

    @Test
    void extremeDurationProductsRemainBoundedWithoutIntegerOverflow() {
        int seconds = SurvivalBuffSupport.durationSeconds(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(120, seconds);
        assertEquals(2400, ActiveBuffSupport.durationTicks(seconds));
        assertEquals(120, SurvivalBuffSupport.durationSeconds(2, 2, Integer.MAX_VALUE));
    }

    @Test
    void followUpBuffsDoNotReplaceExistingStrongerOrLongerProtection() {
        int ticks = ActiveBuffSupport.durationTicks(SurvivalBuffSupport.durationSeconds(2, 2, 4));
        assertFalse(ActiveBuffSupport.canUpgrade(1, 20, 0, ticks));
        assertFalse(ActiveBuffSupport.canUpgrade(0, -1, 0, ticks));
        assertFalse(ActiveBuffSupport.canUpgrade(0, ticks, 0, ticks));
        assertFalse(ActiveBuffSupport.canUpgrade(0, ticks + 1, 0, ticks));
        assertTrue(ActiveBuffSupport.canUpgrade(0, ticks - 1, 0, ticks));
    }
}
