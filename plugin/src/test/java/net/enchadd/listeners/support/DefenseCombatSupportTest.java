package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefenseCombatSupportTest {

    @Test
    void reducesDamageByLevelAndCapsConfiguredMaximum() {
        assertEquals(7.6, DefenseCombatSupport.reduce(10.0, 3, 3, 0.10, 0.24), 1.0e-9);
        assertEquals(5.5, DefenseCombatSupport.reduce(10.0, 5, 3, 0.30, 0.45), 1.0e-9);
    }

    @Test
    void rejectsInvalidInputs() {
        assertEquals(10.0, DefenseCombatSupport.reduce(10.0, 0, 3, 0.10, 0.24), 1.0e-9);
        assertTrue(Double.isNaN(DefenseCombatSupport.reduce(Double.NaN, 3, 3, 0.10, 0.24)));
    }
}