package net.enchadd.listeners.support;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WardShieldSupportTest {
    @Test
    void residualDamageRoundsUpToDurabilityCost() {
        assertEquals(4, WardShieldSupport.durabilityCost(3.2, 336, 0));
        assertEquals(3, WardShieldSupport.durabilityCost(3, 336, 0));
        assertEquals(1, WardShieldSupport.durabilityCost(0.01, 336, 0));
    }

    @Test
    void fullyBlockedAndInvalidDamageCannotConsumeDurability() {
        for (double damage : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, WardShieldSupport.durabilityCost(damage, 336, 0));
        }
    }

    @Test
    void hugeHitsAreBoundedByRemainingShieldDurability() {
        assertEquals(336, WardShieldSupport.durabilityCost(Double.MAX_VALUE, 336, 0));
        assertEquals(1, WardShieldSupport.durabilityCost(Double.MAX_VALUE, 336, 335));
        assertEquals(0, WardShieldSupport.durabilityCost(5, 336, 336));
    }

    @Test
    void customMaximumDurabilityCannotOverflowTheCost() {
        assertEquals(924, WardShieldSupport.durabilityCost(1000, 1024, 100));
        assertEquals(1, WardShieldSupport.durabilityCost(Double.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE - 1));
        assertEquals(Integer.MAX_VALUE, WardShieldSupport.durabilityCost(Double.MAX_VALUE, Integer.MAX_VALUE, 0));
    }

    @Test
    void invalidOrBrokenShieldStateCannotGrantProtection() {
        for (int maximum : new int[]{0, -1, Integer.MIN_VALUE}) {
            assertEquals(0, WardShieldSupport.durabilityCost(4, maximum, 0));
        }
        assertEquals(0, WardShieldSupport.durabilityCost(4, 336, -1));
        assertEquals(0, WardShieldSupport.durabilityCost(4, 336, 337));
    }

    @Test
    void onlyTheActivelyUsedOffhandShieldSuppliesWard() {
        assertTrue(WardShieldSupport.isActiveWardShield(EquipmentSlot.OFF_HAND, Material.SHIELD));
        assertFalse(WardShieldSupport.isActiveWardShield(EquipmentSlot.HAND, Material.SHIELD));
        assertFalse(WardShieldSupport.isActiveWardShield(EquipmentSlot.OFF_HAND, Material.BREAD));
        assertFalse(WardShieldSupport.isActiveWardShield(EquipmentSlot.OFF_HAND, Material.AIR));
        assertFalse(WardShieldSupport.isActiveWardShield(null, Material.SHIELD));
        assertFalse(WardShieldSupport.isActiveWardShield(EquipmentSlot.OFF_HAND, null));
    }
}
