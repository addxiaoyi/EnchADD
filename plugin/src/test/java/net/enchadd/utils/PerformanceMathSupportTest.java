package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerformanceMathSupportTest {

    @Test
    void safeDivideReturnsDefaultForInvalidDenominatorOrResult() {
        assertEquals(7.0, PerformanceMathSupport.safeDivide(10.0, 0.0, 7.0));
        assertEquals(7.0, PerformanceMathSupport.safeDivide(10.0, Double.NaN, 7.0));
        assertEquals(7.0, PerformanceMathSupport.safeDivide(10.0, Double.POSITIVE_INFINITY, 7.0));
        assertEquals(7.0, PerformanceMathSupport.safeDivide(Double.POSITIVE_INFINITY, 2.0, 7.0));
    }

    @Test
    void safeDivideReturnsFiniteDivisionResult() {
        assertEquals(2.5, PerformanceMathSupport.safeDivide(5.0, 2.0, 7.0));
    }

    @Test
    void clampBoundsDoubleAndIntegerValues() {
        assertEquals(1.0, PerformanceMathSupport.clamp(-5.0, 1.0, 3.0));
        assertEquals(2.0, PerformanceMathSupport.clamp(2.0, 1.0, 3.0));
        assertEquals(3.0, PerformanceMathSupport.clamp(5.0, 1.0, 3.0));
        assertEquals(1, PerformanceMathSupport.clamp(-5, 1, 3));
        assertEquals(2, PerformanceMathSupport.clamp(2, 1, 3));
        assertEquals(3, PerformanceMathSupport.clamp(5, 1, 3));
    }

    @Test
    void durationCalculationHasOneTickMinimumUnitBeforeConvertingToTicks() {
        assertEquals(20, PerformanceMathSupport.calculateDurationTicks(0, 5));
        assertEquals(120, PerformanceMathSupport.calculateDurationTicks(3, 2));
        assertEquals(20, PerformanceMathSupport.calculateDurationTicksPerLevel(0, 5));
        assertEquals(120, PerformanceMathSupport.calculateDurationTicksPerLevel(3, 2));
    }

    @Test
    void durationCalculationCapsExtremeConfiguration() {
        assertEquals(120 * 20, PerformanceMathSupport.calculateDurationTicks(10_000, 10_000));
        assertEquals(120 * 20, PerformanceMathSupport.calculateDurationTicksPerLevel(10_000, 10_000));
    }
}
