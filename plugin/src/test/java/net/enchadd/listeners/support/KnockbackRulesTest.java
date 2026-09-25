package net.enchadd.listeners.support;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KnockbackRulesTest {
    @Test
    void defaultReductionPreservesDirectionAndOriginalVector() {
        Vector original = new Vector(2, 1, -2);
        Vector reduced = KnockbackRules.reduce(original, 3, 3, 0.15, 0.45, 0.8);
        assertNotNull(reduced);
        assertEquals(1.1, reduced.getX(), 1e-12);
        assertEquals(0.55, reduced.getY(), 1e-12);
        assertEquals(-1.1, reduced.getZ(), 1e-12);
        assertEquals(new Vector(2, 1, -2), original);
        assertNotSame(original, reduced);
    }

    @Test
    void overlevelEquipmentRespectsConfiguredMaximum() {
        Vector reduced = KnockbackRules.reduce(new Vector(1, 0, 0), Integer.MAX_VALUE, 1, 0.15, 0.6, 0.6);
        assertNotNull(reduced);
        assertEquals(0.85, reduced.getX(), 1e-12);
        assertEquals(0.3, DefenseEffectRules.chance(Integer.MAX_VALUE, 1, 0.3, 0.6));
    }

    @Test
    void zeroOrUnchangedImpulseDoesNotCountAsSuccessfulReduction() {
        assertNull(KnockbackRules.reduce(new Vector(), 3, 3, 0.15, 0.6, 0.6));
        assertNull(KnockbackRules.reduce(new Vector(1, 0, 0), 3, 3, 0, 0.6, 0.6));
        assertNull(KnockbackRules.reduce(new Vector(1, 0, 0), 1, 3, Double.MIN_VALUE, 0.6, 0.6));
    }

    @Test
    void invalidVectorsAndConfigurationAreRejected() {
        assertNull(KnockbackRules.reduce(null, 3, 3, 0.15, 0.6, 0.6));
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertNull(KnockbackRules.reduce(new Vector(invalid, 0, 0), 3, 3, 0.15, 0.6, 0.6));
            assertNull(KnockbackRules.reduce(new Vector(0, invalid, 0), 3, 3, 0.15, 0.6, 0.6));
            assertNull(KnockbackRules.reduce(new Vector(0, 0, invalid), 3, 3, 0.15, 0.6, 0.6));
            assertNull(KnockbackRules.reduce(new Vector(1, 0, 0), 3, 3, invalid, 0.6, 0.6));
            assertNull(KnockbackRules.reduce(new Vector(1, 0, 0), 3, 3, 0.15, invalid, 0.6));
        }
    }

    @Test
    void disabledLevelsAndCapsDoNotProduceAnEffect() {
        Vector impulse = new Vector(1, 0, 0);
        assertNull(KnockbackRules.reduce(impulse, 0, 3, 0.15, 0.6, 0.6));
        assertNull(KnockbackRules.reduce(impulse, 3, 0, 0.15, 0.6, 0.6));
        assertNull(KnockbackRules.reduce(impulse, 3, 3, -1, 0.6, 0.6));
        assertNull(KnockbackRules.reduce(impulse, 3, 3, 0.15, 0, 0.6));
        assertNull(KnockbackRules.reduce(impulse, 3, 3, 0.15, 1, 1));
    }

    @Test
    void extremeFiniteRatesRetainKnockbackUnderSafetyCaps() {
        Vector brace = KnockbackRules.reduce(new Vector(1, 0, 0), 3, 3, Double.MAX_VALUE, 10, 0.8);
        Vector steadfast = KnockbackRules.reduce(new Vector(1, 0, 0), 3, 3, Double.MAX_VALUE, 10, 0.6);
        assertNotNull(brace);
        assertNotNull(steadfast);
        assertEquals(0.2, brace.getX(), 1e-12);
        assertEquals(0.4, steadfast.getX(), 1e-12);
    }
}
