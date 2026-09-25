package net.enchadd.listeners.support;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BarrierShieldSupportTest {
    @Test
    void defaultLevelsRetainTheirChanceAndRadius() {
        assertEquals(0.3, BarrierShieldSupport.triggerChance(1, 2, 0.3, 0.6));
        assertEquals(0.6, BarrierShieldSupport.triggerChance(2, 2, 0.3, 0.6));
        assertEquals(2, BarrierShieldSupport.knockbackRadius(1, 2, 2));
        assertEquals(4, BarrierShieldSupport.knockbackRadius(2, 2, 2));
    }

    @Test
    void unsafeLevelsCannotExceedTheConfiguredMaximum() {
        assertEquals(0.3, BarrierShieldSupport.triggerChance(Integer.MAX_VALUE, 1, 0.3, 0.6));
        assertEquals(0.6, BarrierShieldSupport.triggerChance(Integer.MAX_VALUE, 2, 0.3, 0.6));
        assertEquals(2, BarrierShieldSupport.knockbackRadius(Integer.MAX_VALUE, 1, 2));
        assertEquals(4, BarrierShieldSupport.knockbackRadius(Integer.MAX_VALUE, 2, 2));
    }

    @Test
    void disabledAndInvalidLevelsCannotTriggerOrPush() {
        for (int invalid : new int[]{0, -1, Integer.MIN_VALUE}) {
            assertEquals(0, BarrierShieldSupport.triggerChance(invalid, 2, 0.3, 0.6));
            assertEquals(0, BarrierShieldSupport.triggerChance(2, invalid, 0.3, 0.6));
            assertEquals(0, BarrierShieldSupport.knockbackRadius(invalid, 2, 2));
            assertEquals(0, BarrierShieldSupport.knockbackRadius(2, invalid, 2));
            assertNull(BarrierShieldSupport.knockbackVelocity(new Vector(1, 0, 0), invalid));
        }
    }

    @Test
    void invalidChanceAndRadiusSettingsDisableTheEffect() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, BarrierShieldSupport.triggerChance(2, 2, invalid, 0.6));
            assertEquals(0, BarrierShieldSupport.triggerChance(2, 2, 0.3, invalid));
            assertEquals(0, BarrierShieldSupport.knockbackRadius(2, 2, invalid));
        }
    }

    @Test
    void finiteOverflowStillHonorsChanceAndRangeLimits() {
        assertEquals(0.75, BarrierShieldSupport.triggerChance(Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE, 1));
        assertEquals(0.2, BarrierShieldSupport.triggerChance(2, 2, Double.MAX_VALUE, 0.2));
        assertEquals(6, BarrierShieldSupport.knockbackRadius(Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE));
    }

    @Test
    void smallConfiguredRadiusIsNotExpandedToOneAndAHalfBlocks() {
        assertEquals(0.1, BarrierShieldSupport.knockbackRadius(1, 2, 0.1));
        assertEquals(0.2, BarrierShieldSupport.knockbackRadius(2, 2, 0.1));
        assertFalse(EffectMotionSupport.withinRadius(new Vector(0.5, 0, 0),
                BarrierShieldSupport.knockbackRadius(1, 2, 0.1)));
    }

    @Test
    void elevatedTargetsReceiveTheSameHorizontalPush() {
        Vector flat = BarrierShieldSupport.knockbackVelocity(new Vector(3, 0, 4), 2);
        Vector elevated = BarrierShieldSupport.knockbackVelocity(new Vector(3, 3, 4), 2);
        assertNotNull(flat);
        assertEquals(flat, elevated);
        assertEquals(1, Math.hypot(flat.getX(), flat.getZ()), 1e-12);
        assertEquals(0.6, flat.getY(), 1e-12);
    }

    @Test
    void overlappingAndInvalidTargetsCannotReceiveAnArbitraryLaunch() {
        for (Vector offset : new Vector[]{new Vector(), new Vector(0, 2, 0),
                new Vector(Double.NaN, 0, 1), new Vector(1, Double.POSITIVE_INFINITY, 0),
                new Vector(0, 0, Double.NEGATIVE_INFINITY), new Vector(Double.MAX_VALUE, 0, Double.MAX_VALUE)}) {
            assertNull(BarrierShieldSupport.knockbackVelocity(offset, 2));
        }
    }

    @Test
    void velocityCapsAndOriginalDirectionSurviveExtremeLevels() {
        Vector offset = new Vector(-3, 2, -4);
        Vector original = offset.clone();
        Vector velocity = BarrierShieldSupport.knockbackVelocity(offset, Integer.MAX_VALUE);
        assertNotNull(velocity);
        assertEquals(original, offset);
        assertEquals(1.4, Math.hypot(velocity.getX(), velocity.getZ()), 1e-12);
        assertEquals(0.8, velocity.getY(), 1e-12);
        assertTrue(velocity.getX() < 0 && velocity.getZ() < 0);
    }
}
