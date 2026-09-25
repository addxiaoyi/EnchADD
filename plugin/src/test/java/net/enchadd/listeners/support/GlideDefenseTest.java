package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GlideDefenseTest {
    @Test
    void defaultProgressionRemainsUnchanged() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(10 - level, SkimImpactSupport.reducedDamage(10, level, 3, 1, 3));
            assertEquals(level * 40, AfterglideGlideSupport.durationTicks(level, 3, 2));
        }
    }

    @Test
    void unsafeLevelsAreBoundedByConfiguration() {
        assertEquals(9, SkimImpactSupport.reducedDamage(10, Integer.MAX_VALUE, 1, 1, 3));
        assertEquals(120, AfterglideGlideSupport.durationTicks(Integer.MAX_VALUE, 3, 2));
        assertEquals(2400, AfterglideGlideSupport.durationTicks(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void invalidReductionSettingsDoNotChangeDamage() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(10, SkimImpactSupport.reducedDamage(10, 3, 3, invalid, 3));
            assertEquals(10, SkimImpactSupport.reducedDamage(10, 3, 3, 1, invalid));
        }
        assertEquals(10, SkimImpactSupport.reducedDamage(10, 0, 3, 1, 3));
        assertEquals(10, SkimImpactSupport.reducedDamage(10, 3, 0, 1, 3));
    }

    @Test
    void skimPreservesInvalidBaseAndFullyAbsorbsSmallImpacts() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(invalid, SkimImpactSupport.reducedDamage(invalid, 3, 3, 1, 3));
        }
        assertEquals(0, SkimImpactSupport.reducedDamage(2, 3, 3, 1, 3));
        assertEquals(6, SkimImpactSupport.reducedDamage(10, 3, 3, Double.MAX_VALUE, 100));
    }

    @Test
    void disabledSlowFallingCannotCreateMinimumDuration() {
        assertEquals(0, AfterglideGlideSupport.durationTicks(3, 3, 0));
        assertEquals(0, AfterglideGlideSupport.durationTicks(3, 3, -1));
        assertEquals(0, AfterglideGlideSupport.durationTicks(0, 3, 2));
        assertEquals(0, AfterglideGlideSupport.durationTicks(3, 0, 2));
    }

    @Test
    void existingSlowFallingMustActuallyImproveBeforeRefresh() {
        assertFalse(ActiveBuffSupport.canUpgrade(1, 20, 0, 120));
        assertFalse(ActiveBuffSupport.canUpgrade(0, -1, 0, 120));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 120, 0, 120));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 200, 0, 120));
        assertTrue(ActiveBuffSupport.canUpgrade(0, 20, 0, 120));
    }
}
