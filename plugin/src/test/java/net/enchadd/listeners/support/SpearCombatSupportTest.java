package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpearCombatSupportTest {

    @Test
    void capsLancerDamageBonus() {
        assertEquals(12.4, SpearCombatSupport.damageBonus(10.0, 3, 0.08, 0.24, 1.0), 1.0e-9);
    }

    @Test
    void reachBonusOnlyStartsAfterMinimumDistance() {
        assertEquals(0.0, SpearCombatSupport.reachFactor(3.5, 3.5, 7.0), 1.0e-9);
        assertEquals(0.5, SpearCombatSupport.reachFactor(5.25, 3.5, 7.0), 1.0e-9);
        assertEquals(1.0, SpearCombatSupport.reachFactor(9.0, 3.5, 7.0), 1.0e-9);
    }

    @Test
    void capsCounterthrustReduction() {
        assertEquals(8.2, SpearCombatSupport.reduceDamage(10.0, 3, 0.06, 0.18), 1.0e-9);
    }

    @Test
    void capsSkewerDuration() {
        assertEquals(30, SpearCombatSupport.durationTicks(3, 3, 10, 40));
        assertEquals(40, SpearCombatSupport.durationTicks(5, 3, 20, 40));
    }
}