package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RangedDamageRulesTest {
    @Test
    void farshotDefaultDistanceCurveIsPreserved() {
        assertEquals(0, RangedDamageRules.distanceFactor(12, 12, 40));
        assertEquals(0.5, RangedDamageRules.distanceFactor(26, 12, 40));
        assertEquals(1, RangedDamageRules.distanceFactor(40, 12, 40));
        assertEquals(1, RangedDamageRules.distanceFactor(100, 12, 40));
        assertEquals(0.27, RangedDamageRules.bonus(3, 3, 0.18, 0.5), 1e-12);
        assertEquals(15.4, RangedDamageRules.damage(10, RangedDamageRules.bonus(3, 3, 0.18, 1)), 1e-12);
    }

    @Test
    void invalidDistanceSettingsCannotAwardFullBonus() {
        assertEquals(0, RangedDamageRules.distanceFactor(40, 12, 12));
        assertEquals(0, RangedDamageRules.distanceFactor(40, 20, 12));
        assertEquals(0, RangedDamageRules.distanceFactor(40, -1, 12));
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, RangedDamageRules.distanceFactor(invalid, 12, 40));
            assertEquals(0, RangedDamageRules.distanceFactor(26, invalid, 40));
            assertEquals(0, RangedDamageRules.distanceFactor(26, 12, invalid));
        }
    }

    @Test
    void configuredLevelBoundsBothEnchantmentBonuses() {
        assertEquals(0.54, RangedDamageRules.bonus(Integer.MAX_VALUE, 3, 0.18, 1), 1e-12);
        assertEquals(0.6, RangedDamageRules.bonus(Integer.MAX_VALUE, 3, 0.2, 1), 1e-12);
        assertEquals(0, RangedDamageRules.bonus(0, 3, 0.2, 1));
        assertEquals(0, RangedDamageRules.bonus(3, 0, 0.2, 1));
    }

    @Test
    void disabledAndInvalidRatesDoNotProduceBonus() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, RangedDamageRules.bonus(3, 3, invalid, 1));
            assertEquals(0, RangedDamageRules.bonus(3, 3, 0.2, invalid));
        }
    }

    @Test
    void finiteExtremeRatesStayBoundedAndOverflowDoesNotChangeDamage() {
        assertEquals(0.75, RangedDamageRules.bonus(3, 3, Double.MAX_VALUE, 1));
        assertEquals(17.5, RangedDamageRules.damage(10, 100));
        assertEquals(Double.MAX_VALUE, RangedDamageRules.damage(Double.MAX_VALUE, 0.75));
    }

    @Test
    void invalidAndZeroBaseDamageArePreserved() {
        for (double base : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(base, RangedDamageRules.damage(base, 0.6));
        }
        assertEquals(10, RangedDamageRules.damage(10, Double.NaN));
        assertEquals(10, RangedDamageRules.damage(10, -1));
    }

    @Test
    void steadyAimRequiresFiniteSpeedAtItsExistingThreshold() {
        assertTrue(RangedDamageRules.isSteadySpeed(2.8));
        assertTrue(RangedDamageRules.isSteadySpeed(3));
        for (double speed : new double[]{0, 2.79, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertFalse(RangedDamageRules.isSteadySpeed(speed));
        }
    }
}
