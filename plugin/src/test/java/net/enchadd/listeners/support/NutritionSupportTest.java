package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NutritionSupportTest {
    @Test
    void defaultFishingRecoveryKeepsItsProgression() {
        for (int level = 1; level <= 2; level++) {
            assertEquals(10 + level, NutritionSupport.food(10, level, 2, 1));
            assertEquals(2f + 0.5f * level, NutritionSupport.saturation(2, 10, level, 2, 0.5));
        }
    }

    @Test
    void commandEnchantsCannotExceedConfiguredLevel() {
        assertEquals(12, NutritionSupport.food(10, Integer.MAX_VALUE, 2, 1));
        assertEquals(3f, NutritionSupport.saturation(2, 10, Integer.MAX_VALUE, 2, 0.5));
        assertEquals(6, NutritionSupport.absorptionSeconds(Integer.MAX_VALUE, 3, 2));
    }

    @Test
    void restorationStopsAtFoodAndSaturationCapacity() {
        assertEquals(20, NutritionSupport.food(19, 2, 2, 1));
        assertEquals(20, NutritionSupport.food(20, 2, 2, 1));
        assertEquals(10f, NutritionSupport.saturation(9.5f, 10, 2, 2, 0.5));
        assertEquals(10f, NutritionSupport.saturation(10, 10, 2, 2, 0.5));
    }

    @Test
    void disabledAndInvalidRecoveryDoesNotRemoveNutrition() {
        for (int perLevel : new int[]{0, -1}) {
            assertEquals(10, NutritionSupport.food(10, 2, 2, perLevel));
        }
        for (double perLevel : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(3f, NutritionSupport.saturation(3, 10, 2, 2, perLevel));
        }
        assertEquals(12f, NutritionSupport.saturation(12, 10, 2, 2, 0.5));
    }

    @Test
    void zeroLevelsDisableBothRestorationChannels() {
        assertEquals(10, NutritionSupport.food(10, 0, 2, 1));
        assertEquals(10, NutritionSupport.food(10, 2, 0, 1));
        assertEquals(3f, NutritionSupport.saturation(3, 10, 0, 2, 0.5));
        assertEquals(3f, NutritionSupport.saturation(3, 10, 2, 0, 0.5));
    }

    @Test
    void extremeFiniteSettingsCannotOverflowRecoveryOrDuration() {
        assertEquals(20, NutritionSupport.food(10, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(20f, NutritionSupport.saturation(3, 20, Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE));
        assertEquals(120, NutritionSupport.absorptionSeconds(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void nourishDurationPreservesDefaultAndRespectsDisabledSettings() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(level * 2, NutritionSupport.absorptionSeconds(level, 3, 2));
        }
        assertEquals(0, NutritionSupport.absorptionSeconds(3, 3, 0));
        assertEquals(0, NutritionSupport.absorptionSeconds(3, 3, -1));
        assertEquals(0, NutritionSupport.absorptionSeconds(0, 3, 2));
        assertEquals(0, NutritionSupport.absorptionSeconds(3, 0, 2));
    }
}
