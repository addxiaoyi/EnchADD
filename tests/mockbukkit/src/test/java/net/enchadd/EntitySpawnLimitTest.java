package net.enchadd;

import net.enchadd.enchants.VolleyEnchant;
import net.enchadd.listeners.support.VolleySpawnSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Entity Generation Limit Tests for Volley Enchantment
 *
 * Tests the entity spawn limiting mechanism that caps extra arrows:
 * - Normal arrows: max(0, min(32, level * additionalArrowsPerLevel))
 * - Spectral arrows: max(0, min(32, level))
 * - Absolute maximum cap: 32 (MAX_EXTRA_ARROW_MULTIPLIER)
 */
class EntitySpawnLimitTest {

    @Nested
    @DisplayName("VolleySpawnSupport Entity Generation Limit Tests")
    class VolleySpawnSupportLimitTests {

        @Test
        @DisplayName("Normal arrows should cap at MAX_EXTRA_ARROW_MULTIPLIER (32)")
        void normalArrowsShouldCapAtMaximum() {
            // Given: high level config with high additionalArrowsPerLevel
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(10); // Would produce 10 * 100 = 1000
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 32);

            // When: asking for extra arrows at level 100
            int result = support.extraArrowsForNormal(100);

            // Then: should be capped at 32
            assertEquals(32, result, "Normal arrows should be capped at MAX_EXTRA_ARROW_MULTIPLIER (32)");
        }

        @Test
        @DisplayName("Spectral arrows should cap at MAX_EXTRA_ARROW_MULTIPLIER (32)")
        void spectralArrowsShouldCapAtMaximum() {
            // Given: config with level exceeding cap
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(1);
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 32);

            // When: asking for extra spectral arrows at level 50
            int result = support.extraArrowsForSpectral(50);

            // Then: should be capped at 32
            assertEquals(32, result, "Spectral arrows should be capped at MAX_EXTRA_ARROW_MULTIPLIER (32)");
        }

        @ParameterizedTest
        @DisplayName("Normal arrows formula: level * additionalArrowsPerLevel should respect cap")
        @CsvSource({
            "1, 1, 1",     // Level 1, 1 per level = 1
            "2, 1, 2",     // Level 2, 1 per level = 2
            "5, 1, 5",     // Level 5, 1 per level = 5
            "1, 5, 5",     // Level 1, 5 per level = 5
            "10, 3, 30",   // Level 10, 3 per level = 30
            "10, 4, 32",   // Level 10, 4 per level = 40 -> capped to 32
            "32, 5, 32",   // Level 32, 5 per level = 160 -> capped to 32
            "50, 10, 32",  // Level 50, 10 per level = 500 -> capped to 32
        })
        void normalArrowsFormulaRespectsCap(int level, int arrowsPerLevel, int expected) {
            // Given
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(arrowsPerLevel);
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 32);

            // When
            int result = support.extraArrowsForNormal(level);

            // Then
            assertEquals(expected, result);
        }

        @ParameterizedTest
        @DisplayName("Spectral arrows formula: level should respect cap")
        @CsvSource({
            "1, 1",    // Level 1 = 1
            "10, 10",  // Level 10 = 10
            "32, 32",  // Level 32 = 32
            "33, 32",  // Level 33 -> capped to 32
            "50, 32",  // Level 50 -> capped to 32
            "100, 32", // Level 100 -> capped to 32
        })
        void spectralArrowsFormulaRespectsCap(int level, int expected) {
            // Given
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(1);
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 32);

            // When
            int result = support.extraArrowsForSpectral(level);

            // Then
            assertEquals(expected, result);
        }

        @ParameterizedTest
        @DisplayName("Level 0 or negative should return 0 arrows")
        @ValueSource(ints = {0, -1, -10, -100})
        void zeroOrNegativeLevelReturnsZero(int level) {
            // Given
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(5);
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 32);

            // When
            int normalResult = support.extraArrowsForNormal(level);
            int spectralResult = support.extraArrowsForSpectral(level);

            // Then
            assertEquals(0, normalResult, "Normal arrows should return 0 for level " + level);
            assertEquals(0, spectralResult, "Spectral arrows should return 0 for level " + level);
        }

        @Test
        @DisplayName("Different maxExtraArrowMultiplier should be respected")
        void differentMaxMultiplierShouldBeRespected() {
            // Given: custom max of 10
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(5);
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 10);

            // When
            int result = support.extraArrowsForNormal(100); // Would be 500, capped to 10

            // Then
            assertEquals(10, result, "Should respect custom maxExtraArrowMultiplier of 10");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests for Entity Generation Limits")
    class EdgeCaseTests {

        @Test
        @DisplayName("Very high additionalArrowsPerLevel should be capped")
        void veryHighArrowsPerLevelShouldBeCapped() {
            // Given
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(1000); // Unrealistic high value
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 32);

            // When: level 1 with 1000 arrows per level
            int result = support.extraArrowsForNormal(1);

            // Then: should still be capped at 32
            assertEquals(32, result, "Should cap at MAX_EXTRA_ARROW_MULTIPLIER even with very high arrowsPerLevel");
        }

        @Test
        @DisplayName("Boundary test: exactly at cap (level 32 with 1 per level)")
        void exactlyAtCapShouldWork() {
            // Given
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(1);
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 32);

            // When
            int normalResult = support.extraArrowsForNormal(32);
            int spectralResult = support.extraArrowsForSpectral(32);

            // Then
            assertEquals(32, normalResult, "Should return exactly 32 at boundary");
            assertEquals(32, spectralResult, "Should return exactly 32 at boundary");
        }

        @Test
        @DisplayName("Boundary test: one above cap (level 33 with 1 per level)")
        void oneAboveCapShouldBeCapped() {
            // Given
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(1);
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 32);

            // When
            int normalResult = support.extraArrowsForNormal(33);
            int spectralResult = support.extraArrowsForSpectral(33);

            // Then
            assertEquals(32, normalResult, "Should return 32 when one above cap");
            assertEquals(32, spectralResult, "Should return 32 when one above cap");
        }

        @Test
        @DisplayName("Zero additionalArrowsPerLevel should always return 0")
        void zeroArrowsPerLevelShouldReturnZero() {
            // Given
            VolleyEnchant config = org.mockito.Mockito.mock(VolleyEnchant.class);
            when(config.getAdditionalArrowsPerLevel()).thenReturn(0);
            when(config.getSpread()).thenReturn(0.0);

            VolleySpawnSupport support = new VolleySpawnSupport(config, 32);

            // When
            int result = support.extraArrowsForNormal(100);

            // Then
            assertEquals(0, result, "Should return 0 when additionalArrowsPerLevel is 0");
        }
    }
}
