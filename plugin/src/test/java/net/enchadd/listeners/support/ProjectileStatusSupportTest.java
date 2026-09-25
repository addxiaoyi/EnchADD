package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectileStatusSupportTest {
    @Test
    void harmlessOrInvalidHitsCannotApplyStatus() {
        for (double damage : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, ProjectileStatusSupport.durationTicks(damage, 3, 3, 2));
        }
        assertEquals(120, ProjectileStatusSupport.durationTicks(0.01, 3, 3, 2));
    }

    @Test
    void disabledDurationNeverBecomesOneSecond() {
        assertEquals(0, ProjectileStatusSupport.durationTicks(5, 3, 3, 0));
        assertEquals(0, ProjectileStatusSupport.durationTicks(5, 3, 3, -1));
        assertEquals(0, ProjectileStatusSupport.durationTicks(5, 0, 3, 2));
        assertEquals(0, ProjectileStatusSupport.durationTicks(5, 3, 0, 2));
    }

    @Test
    void defaultDurationsAndLevelCapsRemainConsistent() {
        assertEquals(120, ProjectileStatusSupport.durationTicks(5, 3, 3, 2));
        assertEquals(80, ProjectileStatusSupport.durationTicks(5, Integer.MAX_VALUE, 2, 2));
        assertEquals(240, ProjectileStatusSupport.durationTicks(5, Integer.MAX_VALUE, 3, 4));
        assertEquals(2400, ProjectileStatusSupport.durationTicks(5, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void fixedChanceDoesNotAccidentallyScaleWithDuration() {
        assertEquals(0.25, ProjectileStatusSupport.chance(0.25, 1, 0.75));
        assertEquals(0.30, ProjectileStatusSupport.chance(0.15, 2, 0.75), 1.0e-12);
        assertEquals(0.60, ProjectileStatusSupport.chance(0.25, 3, 0.60));
    }

    @Test
    void invalidChanceConfigurationCannotTrigger() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0.0, ProjectileStatusSupport.chance(invalid, 3, 0.75));
            assertEquals(0.0, ProjectileStatusSupport.chance(0.25, 3, invalid));
        }
        assertEquals(0.0, ProjectileStatusSupport.chance(0.25, 0, 0.75));
    }

    @Test
    void largeFiniteProbabilitiesStillRespectBothCaps() {
        assertEquals(0.75, ProjectileStatusSupport.chance(Double.MAX_VALUE, Integer.MAX_VALUE, 1));
        assertEquals(0.4, ProjectileStatusSupport.chance(Double.MAX_VALUE, Integer.MAX_VALUE, 0.4));
    }
}
