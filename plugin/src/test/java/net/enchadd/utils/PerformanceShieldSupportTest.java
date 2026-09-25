package net.enchadd.utils;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceShieldSupportTest {
    @Test
    void mainhandBlockingCannotBorrowOffhandShieldEnchants() {
        assertTrue(PerformanceShieldSupport.isActiveOffhandShield(EquipmentSlot.OFF_HAND, Material.SHIELD));
        assertFalse(PerformanceShieldSupport.isActiveOffhandShield(EquipmentSlot.HAND, Material.SHIELD));
        assertFalse(PerformanceShieldSupport.isActiveOffhandShield(EquipmentSlot.CHEST, Material.SHIELD));
    }

    @Test
    void unusedAndNonShieldItemsCannotSupplyShieldEffects() {
        assertFalse(PerformanceShieldSupport.isActiveOffhandShield(null, Material.SHIELD));
        assertFalse(PerformanceShieldSupport.isActiveOffhandShield(EquipmentSlot.OFF_HAND, null));
        assertFalse(PerformanceShieldSupport.isActiveOffhandShield(EquipmentSlot.OFF_HAND, Material.AIR));
        assertFalse(PerformanceShieldSupport.isActiveOffhandShield(EquipmentSlot.OFF_HAND, Material.BREAD));
        assertFalse(PerformanceShieldSupport.isActiveOffhandShield(EquipmentSlot.OFF_HAND, Material.DIAMOND_SWORD));
    }

    @Test
    void actualBlockingStillCountsWhenFinalDamageIsZero() {
        assertTrue(PerformanceShieldSupport.hasIncomingDamage(false, 8, 0));
        assertTrue(PerformanceShieldSupport.hasIncomingDamage(false, 8, 3));
        assertTrue(PerformanceShieldSupport.hasBlockedDamage(-8));
        assertTrue(PerformanceShieldSupport.hasBlockedDamage(-5));
    }

    @Test
    void cancelledAndInvalidHitsCannotTriggerShieldEnchants() {
        assertFalse(PerformanceShieldSupport.hasIncomingDamage(true, 8, 0));
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertFalse(PerformanceShieldSupport.hasIncomingDamage(false, invalid, 0));
        }
        for (double invalid : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertFalse(PerformanceShieldSupport.hasIncomingDamage(false, 8, invalid));
        }
    }

    @Test
    void raisedShieldWithoutNegativeBlockingModifierDoesNotCountAsABlock() {
        for (double modifier : new double[]{0, 1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertFalse(PerformanceShieldSupport.hasBlockedDamage(modifier));
        }
    }

    @Test
    void facingFallbackRejectsHitsFromTheSideAndBehind() {
        Vector facing = new Vector(0, 0, 1);
        assertTrue(PerformanceShieldSupport.facesIncoming(facing, new Vector(0, 0, 2)));
        assertFalse(PerformanceShieldSupport.facesIncoming(facing, new Vector(0, 0, -2)));
        assertFalse(PerformanceShieldSupport.facesIncoming(facing, new Vector(2, 0, 0)));
    }

    @Test
    void fallbackKeepsTheExistingFrontFacingThreshold() {
        Vector facing = new Vector(0, 0, 1);
        assertFalse(PerformanceShieldSupport.facesIncoming(facing, new Vector(1, 0, 0.1)));
        assertTrue(PerformanceShieldSupport.facesIncoming(facing, new Vector(1, 0, 0.2)));
    }

    @Test
    void invalidAndDegenerateVectorsCannotProduceABlock() {
        Vector forward = new Vector(0, 0, 1);
        for (Vector invalid : new Vector[]{null, new Vector(), new Vector(0, 1, 0),
                new Vector(0, 0, 0.0001), new Vector(Double.NaN, 0, 1),
                new Vector(0, Double.POSITIVE_INFINITY, 1), new Vector(0, 0, Double.NEGATIVE_INFINITY),
                new Vector(Double.MAX_VALUE, 0, Double.MAX_VALUE)}) {
            assertFalse(PerformanceShieldSupport.facesIncoming(invalid, forward));
            assertFalse(PerformanceShieldSupport.facesIncoming(forward, invalid));
        }
    }

    @Test
    void facingCheckDoesNotMutateEventVectors() {
        Vector facing = new Vector(0, 3, 2);
        Vector incoming = new Vector(0, -4, 5);
        Vector originalFacing = facing.clone();
        Vector originalIncoming = incoming.clone();
        assertTrue(PerformanceShieldSupport.facesIncoming(facing, incoming));
        assertEquals(originalFacing, facing);
        assertEquals(originalIncoming, incoming);
    }

    @Test
    void verticalOffsetDoesNotChangeHorizontalFacing() {
        assertTrue(PerformanceShieldSupport.facesIncoming(new Vector(0, 10, 1), new Vector(0, -50, 1)));
        assertFalse(PerformanceShieldSupport.facesIncoming(new Vector(0, 10, 1), new Vector(0, -50, -1)));
    }

    @Test
    void shieldBlockChecksRejectNullInputs() {
        assertFalse(PerformanceShieldSupport.isSuccessfulShieldBlock(null, null));
    }

    @Test
    void likelyFacingBlockRejectsNullInputs() {
        assertFalse(PerformanceShieldSupport.isLikelyShieldFacingBlock(null, new Vector(1, 0, 0)));
        assertFalse(PerformanceShieldSupport.isLikelyShieldFacingBlock(null, null));
    }
}
