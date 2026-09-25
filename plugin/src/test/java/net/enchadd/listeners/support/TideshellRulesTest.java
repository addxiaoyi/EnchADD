package net.enchadd.listeners.support;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TideshellRulesTest {
    @Test
    void waterIncludesUnderwaterPlantsAndWaterloggedBlocks() {
        for (Material material : new Material[]{Material.WATER, Material.BUBBLE_COLUMN,
                Material.KELP, Material.KELP_PLANT, Material.SEAGRASS, Material.TALL_SEAGRASS}) {
            assertTrue(TideshellRules.containsWater(material, false));
        }
        assertTrue(TideshellRules.containsWater(Material.OAK_SLAB, true));
    }

    @Test
    void lavaAndDryBlocksDoNotActivateTideshell() {
        assertFalse(TideshellRules.containsWater(Material.LAVA, false));
        assertFalse(TideshellRules.containsWater(Material.AIR, false));
        assertFalse(TideshellRules.containsWater(Material.OAK_SLAB, false));
    }

    @Test
    void durationSupportsDisablingAndPreservesDefaultBuffs() {
        assertEquals(160, TideshellRules.durationTicks(8));
        assertEquals(100, TideshellRules.durationTicks(5));
        assertEquals(0, TideshellRules.durationTicks(0));
        assertEquals(0, TideshellRules.durationTicks(-1));
        assertEquals(2400, TideshellRules.durationTicks(Integer.MAX_VALUE));
    }

    @Test
    void strongerInfiniteAndLongerEffectsArePreserved() {
        assertFalse(TideshellRules.canRefresh(1, 20, 160));
        assertFalse(TideshellRules.canRefresh(0, -1, 160));
        assertFalse(TideshellRules.canRefresh(0, 160, 160));
        assertFalse(TideshellRules.canRefresh(0, 200, 160));
        assertTrue(TideshellRules.canRefresh(0, 20, 160));
        assertFalse(TideshellRules.canRefresh(0, 0, 0));
    }
}
