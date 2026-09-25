package net.enchadd.listeners.support;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ActiveBuffSupportTest {
    @Test
    void disabledDurationsStayDisabledAndLargeValuesStayBounded() {
        assertEquals(0, ActiveBuffSupport.durationTicks(0));
        assertEquals(0, ActiveBuffSupport.durationTicks(-1));
        assertEquals(160, ActiveBuffSupport.durationTicks(8));
        assertEquals(2400, ActiveBuffSupport.durationTicks(Integer.MAX_VALUE));
    }

    @Test
    void strongerAndInfiniteEffectsCannotBeOverwritten() {
        assertFalse(ActiveBuffSupport.canUpgrade(2, 20, 1, 160));
        assertFalse(ActiveBuffSupport.canUpgrade(0, -1, 0, 160));
        assertFalse(ActiveBuffSupport.canUpgrade(0, -1, 2, 160));
    }

    @Test
    void equalStrengthRequiresLongerDuration() {
        assertFalse(ActiveBuffSupport.canUpgrade(0, 160, 0, 160));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 200, 0, 160));
        assertTrue(ActiveBuffSupport.canUpgrade(0, 20, 0, 160));
        assertFalse(ActiveBuffSupport.canUpgrade(0, 0, 0, 0));
    }

    @Test
    void strongerFiniteBuffCanReplaceWeakerFiniteBuff() {
        assertTrue(ActiveBuffSupport.canUpgrade(0, 200, 1, 160));
        assertTrue(ActiveBuffSupport.canUpgrade(1, 200, 2, 160));
    }

    @Test
    void invalidRadiiDisableNearbySearch() {
        for (double radius : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, ActiveBuffSupport.radius(radius, 12));
        }
        assertEquals(12, ActiveBuffSupport.radius(100, 12));
        assertEquals(16, ActiveBuffSupport.radius(100, 16));
        assertEquals(6, ActiveBuffSupport.radius(6, 12));
    }

    @Test
    void areaBuffsExcludeBoundingBoxCornersButIncludeSphereBoundary() {
        for (double radius : new double[]{12, 16}) {
            assertFalse(EffectMotionSupport.withinRadius(new Vector(radius, 0, radius), radius));
            assertTrue(EffectMotionSupport.withinRadius(new Vector(0, radius, 0), radius));
            assertFalse(EffectMotionSupport.withinRadius(new Vector(0, radius + 0.01, 0), radius));
        }
    }
}
