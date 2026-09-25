package net.enchadd.listeners.support;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EffectMotionSupportTest {
    @Test
    void sphericalRadiusExcludesCornersOfTheSearchBox() {
        assertTrue(EffectMotionSupport.withinRadius(new Vector(3, 0, 4), 5));
        assertFalse(EffectMotionSupport.withinRadius(new Vector(5, 0, 5), 5));
        assertFalse(EffectMotionSupport.withinRadius(new Vector(0, 5.01, 0), 5));
    }

    @Test
    void invalidRadiusOrLocationNeverMatches() {
        for (double radius : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertFalse(EffectMotionSupport.withinRadius(new Vector(), radius));
        }
        assertFalse(EffectMotionSupport.withinRadius(new Vector(Double.NaN, 0, 0), 5));
        assertFalse(EffectMotionSupport.withinRadius(new Vector(Double.MAX_VALUE, 0, 0), 5));
    }

    @Test
    void directedVelocityPreservesStrengthWithoutMutatingOffset() {
        Vector offset = new Vector(3, 0, 4);
        Vector velocity = EffectMotionSupport.directedVelocity(offset, 1.2);
        assertNotNull(velocity);
        assertEquals(1.2, velocity.length(), 1.0e-12);
        assertEquals(0.72, velocity.getX(), 1.0e-12);
        assertEquals(0.96, velocity.getZ(), 1.0e-12);
        assertEquals(new Vector(3, 0, 4), offset);
    }

    @Test
    void overlappingAndNonFinitePositionsCannotCreateVelocity() {
        assertNull(EffectMotionSupport.directedVelocity(new Vector(), 1));
        assertNull(EffectMotionSupport.directedVelocity(new Vector(1.0e-8, 0, 0), 1));
        assertNull(EffectMotionSupport.directedVelocity(new Vector(Double.NaN, 0, 0), 1));
        assertNull(EffectMotionSupport.directedVelocity(new Vector(0, Double.POSITIVE_INFINITY, 0), 1));
    }

    @Test
    void invalidSpeedCannotSpawnOrPull() {
        for (double speed : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertNull(EffectMotionSupport.directedVelocity(new Vector(1, 0, 0), speed));
        }
    }

    @Test
    void combinedVelocityRejectsOverflowBeforeWritingToEntity() {
        Vector combined = new Vector(Double.MAX_VALUE, 0, 0).add(new Vector(Double.MAX_VALUE, 0, 0));
        assertFalse(EffectMotionSupport.finite(combined));
        assertTrue(EffectMotionSupport.finite(new Vector(0.5, 0.2, -0.3)));
    }
}
