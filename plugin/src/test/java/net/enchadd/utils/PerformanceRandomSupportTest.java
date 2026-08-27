package net.enchadd.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceRandomSupportTest {

    @AfterEach
    void tearDown() {
        SafetyModeManager.shutdown();
    }

    @Test
    void getRandomReturnsSharedRandom() {
        assertNotNull(PerformanceRandomSupport.getRandom());
    }

    @Test
    void rollChanceShortCircuitsAtZeroAndOne() {
        assertFalse(PerformanceRandomSupport.rollChance(0.0));
        assertFalse(PerformanceRandomSupport.rollChance(-1.0));
        assertTrue(PerformanceRandomSupport.rollChance(1.0));
        assertTrue(PerformanceRandomSupport.rollChance(2.0));
    }

    @Test
    void rollChanceWithLevelAppliesLevelAndMaxChanceBounds() {
        assertFalse(PerformanceRandomSupport.rollChanceWithLevel(0.0, 10, 1.0));
        assertTrue(PerformanceRandomSupport.rollChanceWithLevel(0.5, 10, 1.0));
    }
}
