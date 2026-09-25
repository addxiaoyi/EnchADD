package net.enchadd.listeners.support;

import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirbagImpactSupportTest {
    @Test
    void defaultFourLevelsRetainTwentyPercentSteps() {
        for (int level = 1; level <= 4; level++) {
            double reduction = AirbagImpactSupport.reduction(level, 4, 0.2);
            assertEquals(level * 0.2, reduction, 1e-12);
            assertEquals(20 * (1 - level * 0.2),
                    AirbagImpactSupport.reducedDamage(20, 20, reduction), 1e-12);
        }
    }

    @Test
    void armorStackingCannotExceedConfiguredEffectiveLevel() {
        int total = AirbagImpactSupport.addArmorLevel(2, 2);
        assertEquals(4, total);
        assertEquals(0.8, AirbagImpactSupport.reduction(total, 4, 0.2), 1e-12);
        total = AirbagImpactSupport.addArmorLevel(total, 4);
        assertEquals(8, total);
        assertEquals(0.8, AirbagImpactSupport.reduction(total, 4, 0.2), 1e-12);
        assertEquals(0.4, AirbagImpactSupport.reduction(total, 2, 0.2), 1e-12);
    }

    @Test
    void corruptAndHugeArmorLevelsDoNotCancelOtherEquipment() {
        assertEquals(3, AirbagImpactSupport.addArmorLevel(3, -100));
        assertEquals(2, AirbagImpactSupport.addArmorLevel(-1, 2));
        int total = 0;
        for (int slot = 0; slot < 4; slot++) {
            total = AirbagImpactSupport.addArmorLevel(total, Integer.MAX_VALUE);
        }
        assertEquals(Integer.MAX_VALUE, total);
        assertEquals(0.8, AirbagImpactSupport.reduction(total, 4, 0.2), 1e-12);
    }

    @Test
    void disabledAndInvalidReductionSettingsProduceNoBenefit() {
        for (double rate : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertEquals(0, AirbagImpactSupport.reduction(4, 4, rate));
        }
        assertEquals(0, AirbagImpactSupport.reduction(0, 4, 0.2));
        assertEquals(0, AirbagImpactSupport.reduction(-1, 4, 0.2));
        assertEquals(0, AirbagImpactSupport.reduction(4, 0, 0.2));
        assertEquals(0, AirbagImpactSupport.reduction(4, -1, 0.2));
    }

    @Test
    void safetyCapPreservesTenPercentOfImpact() {
        double reduction = AirbagImpactSupport.reduction(Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE);
        assertEquals(0.9, reduction);
        assertEquals(2, AirbagImpactSupport.reducedDamage(20, 20, reduction), 1e-12);
        assertEquals(2, AirbagImpactSupport.reducedDamage(20, 20, 100), 1e-12);
    }

    @Test
    void noDamageAndInvalidValuesCannotBecomeSuccessfulReduction() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(invalid, AirbagImpactSupport.reducedDamage(invalid, 20, 0.8));
            assertEquals(20, AirbagImpactSupport.reducedDamage(20, invalid, 0.8));
            assertEquals(20, AirbagImpactSupport.reducedDamage(20, 20, invalid));
        }
        assertEquals(20, AirbagImpactSupport.reducedDamage(20, 20, Double.MIN_VALUE));
    }

    @Test
    void reductionScalesBaseDamageOnlyOnce() {
        assertEquals(10, AirbagImpactSupport.reducedDamage(20, 8, 0.5), 1e-12);
        double adjusted = AirbagImpactSupport.reducedDamage(Double.MAX_VALUE, Double.MAX_VALUE, 0.8);
        assertTrue(Double.isFinite(adjusted));
        assertTrue(adjusted > 0);
        assertTrue(adjusted < Double.MAX_VALUE);
    }

    @Test
    void airbagOnlyCushionsFallsAndWallImpacts() {
        AirbagImpactSupport support = new AirbagImpactSupport();
        assertTrue(support.isCushionedCause(EntityDamageEvent.DamageCause.FALL));
        assertTrue(support.isCushionedCause(EntityDamageEvent.DamageCause.FLY_INTO_WALL));
        assertFalse(support.isCushionedCause(EntityDamageEvent.DamageCause.ENTITY_ATTACK));
        assertFalse(support.isCushionedCause(EntityDamageEvent.DamageCause.VOID));
        assertFalse(support.isCushionedCause(EntityDamageEvent.DamageCause.FIRE));
    }
}
