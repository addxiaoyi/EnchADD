package net.enchadd.listeners.support;

import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TideRunnerRulesTest {
    @Test
    void defaultDurationAndAmplifierProgressionArePreserved() {
        for (int level = 1; level <= 3; level++) {
            assertEquals(level * 60, TideRunnerRules.duration(level, 3, 60));
            assertEquals(level == 1 ? 0 : 1, TideRunnerRules.amplifier(level, 3, 0.3));
        }
    }

    @Test
    void disabledSettingsStayDisabledAndExtremeLevelsAreBounded() {
        assertEquals(0, TideRunnerRules.duration(3, 3, 0));
        assertEquals(0, TideRunnerRules.duration(3, 3, -1));
        assertEquals(0, TideRunnerRules.duration(0, 3, 60));
        assertEquals(0, TideRunnerRules.duration(3, 0, 60));
        assertEquals(40, TideRunnerRules.duration(1, 3, 1));
        assertEquals(180, TideRunnerRules.duration(Integer.MAX_VALUE, 3, 60));
        assertEquals(2400, TideRunnerRules.duration(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(0, TideRunnerRules.amplifier(Integer.MAX_VALUE, 1, 0.3));
    }

    @Test
    void amplifierCannotOverflowOrAcceptNonFiniteConfiguration() {
        assertEquals(1, TideRunnerRules.amplifier(3, 3, Double.MAX_VALUE));
        for (double rate : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, TideRunnerRules.amplifier(3, 3, rate));
        }
    }

    @Test
    void movementRequiresFiniteNontrivialDisplacement() {
        assertFalse(TideRunnerRules.hasMovement(new Vector()));
        assertFalse(TideRunnerRules.hasMovement(new Vector(0.09, 0, 0)));
        assertTrue(TideRunnerRules.hasMovement(new Vector(0.1, 0, 0)));
        assertTrue(TideRunnerRules.hasMovement(new Vector(0, 0, -0.2)));
        for (double invalid : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE}) {
            assertFalse(TideRunnerRules.hasMovement(new Vector(invalid, 0, 0)));
        }
    }

    @Test
    void waterDetectionIncludesAquaticBlocksButNotLava() {
        assertTrue(TideshellRules.containsWater(Material.WATER, false));
        assertTrue(TideshellRules.containsWater(Material.BUBBLE_COLUMN, false));
        assertTrue(TideshellRules.containsWater(Material.KELP, false));
        assertTrue(TideshellRules.containsWater(Material.SEAGRASS, false));
        assertTrue(TideshellRules.containsWater(Material.OAK_SLAB, true));
        assertFalse(TideshellRules.containsWater(Material.OAK_SLAB, false));
        assertFalse(TideshellRules.containsWater(Material.LAVA, false));
        assertFalse(TideshellRules.containsWater(Material.AIR, false));
    }
}
