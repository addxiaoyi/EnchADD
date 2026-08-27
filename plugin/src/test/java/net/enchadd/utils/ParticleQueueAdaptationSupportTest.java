package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleQueueAdaptationSupportTest {

    @Test
    void adaptsBudgetAndWarningsPredictably() {
        assertEquals(0.5, ParticleQueueAdaptationSupport.windowDropRate(10, 5), 1.0e-9);
        assertEquals(0.0, ParticleQueueAdaptationSupport.windowDropRate(0, 5), 1.0e-9);
        assertEquals(5, ParticleQueueAdaptationSupport.adaptiveStep(40));
        assertEquals(12, ParticleQueueAdaptationSupport.adaptiveStep(120));
        assertTrue(ParticleQueueAdaptationSupport.shouldIncrease(0.1, 0.1, 0.05));
        assertTrue(ParticleQueueAdaptationSupport.shouldIncrease(0.0, 0.7, 0.05));
        assertFalse(ParticleQueueAdaptationSupport.shouldIncrease(0.01, 0.2, 0.05));
        assertTrue(ParticleQueueAdaptationSupport.shouldDecrease(0.0, 0.2));
        assertFalse(ParticleQueueAdaptationSupport.shouldDecrease(0.01, 0.2));
        assertFalse(ParticleQueueAdaptationSupport.shouldDecrease(0.0, 0.21));
        assertFalse(ParticleQueueAdaptationSupport.shouldWarn(0.01, 0.05));
        assertTrue(ParticleQueueAdaptationSupport.shouldWarn(0.06, 0.05));
        assertEquals(80, ParticleQueueAdaptationSupport.increaseBudget(70, 80, 10));
        assertEquals(75, ParticleQueueAdaptationSupport.increaseBudget(70, 80, 5));
        assertEquals(60, ParticleQueueAdaptationSupport.decreaseBudget(70, 60, 10));
        assertEquals(65, ParticleQueueAdaptationSupport.decreaseBudget(70, 60, 5));
    }
}
