package net.enchadd.listeners.support;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CursePenaltySupportTest {
    @Test
    void defaultBackfireAndUnsafeLevelsShareTheConfiguredLimit() {
        assertEquals(0.05, CursePenaltySupport.backfireChance(1, 1, 0.05, 0.5));
        assertEquals(0.05, CursePenaltySupport.backfireChance(Integer.MAX_VALUE, 1, 0.05, 0.5));
        assertEquals(0.15, CursePenaltySupport.backfireChance(3, 3, 0.05, 0.5), 1e-12);
        assertEquals(0.1, CursePenaltySupport.backfireChance(3, 3, 0.05, 0.1));
    }

    @Test
    void invalidChanceLimitsDoNotEnableUncappedPenalty() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, CursePenaltySupport.backfireChance(1, 1, invalid, 0.5));
            assertEquals(0, CursePenaltySupport.backfireChance(1, 1, 0.05, invalid));
        }
        assertEquals(0, CursePenaltySupport.backfireChance(1, 0, 0.05, 0.5));
        assertEquals(1, CursePenaltySupport.backfireChance(Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE, 2));
    }

    @Test
    void selfDamagePreservesDefaultsAndExistingDoubleDamageCap() {
        assertEquals(5, CursePenaltySupport.selfDamage(10, 0.5));
        assertEquals(20, CursePenaltySupport.selfDamage(10, 3));
        assertEquals(0, CursePenaltySupport.selfDamage(Double.MAX_VALUE, 2));
    }

    @Test
    void invalidHitsAndMultipliersNeverProduceSelfDamage() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, CursePenaltySupport.selfDamage(invalid, 0.5));
            assertEquals(0, CursePenaltySupport.selfDamage(10, invalid));
        }
    }

    @Test
    void durabilityKeepsExistingRoundingAndDoubleDamageCeiling() {
        assertEquals(2, CursePenaltySupport.durabilityDamage(1, 1, 3, 0.5, 3));
        assertEquals(3, CursePenaltySupport.durabilityDamage(2, 1, 3, 0.5, 3));
        assertEquals(4, CursePenaltySupport.durabilityDamage(2, 3, 3, 0.5, 3));
        assertEquals(15, CursePenaltySupport.durabilityDamage(10, Integer.MAX_VALUE, 1, 0.5, 3));
        assertEquals(12, CursePenaltySupport.durabilityDamage(10, 3, 3, 0.5, 1.2));
    }

    @Test
    void invalidDurabilityConfigurationPreservesOriginalLoss() {
        for (double invalid : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(10, CursePenaltySupport.durabilityDamage(10, 3, 3, invalid, 3));
            assertEquals(10, CursePenaltySupport.durabilityDamage(10, 3, 3, 0.5, invalid));
        }
        assertEquals(10, CursePenaltySupport.durabilityDamage(10, 3, 3, 0.5, 1));
        assertEquals(10, CursePenaltySupport.durabilityDamage(10, 3, 0, 0.5, 3));
    }

    @Test
    void zeroLossAndIntegerExtremesCannotBecomeNegativeDurability() {
        assertEquals(0, CursePenaltySupport.durabilityDamage(0, 3, 3, 0.5, 3));
        assertEquals(Integer.MAX_VALUE, CursePenaltySupport.durabilityDamage(Integer.MAX_VALUE, 3, 3, 0.5, 3));
        assertEquals(20, CursePenaltySupport.durabilityDamage(10, Integer.MAX_VALUE, Integer.MAX_VALUE, Double.MAX_VALUE, 3));
    }
}
