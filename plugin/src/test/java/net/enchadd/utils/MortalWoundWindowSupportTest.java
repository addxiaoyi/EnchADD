package net.enchadd.utils;

import net.enchadd.listeners.support.MortalWoundWindowSupport;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MortalWoundWindowSupportTest {
    private final NamespacedKey cooldownKey = new NamespacedKey("enchadd", "mortal_wound");
    private final NamespacedKey effectKey = new NamespacedKey("enchadd", "mortal_wound_antiheal");
    private final TestPersistentDataContainer pdc = new TestPersistentDataContainer();
    private final MortalWoundWindowSupport window = new MortalWoundWindowSupport(cooldownKey);

    @Test
    void healingCheckDoesNotEraseShooterCooldown() {
        PerformanceCooldownSupport.setCooldown(pdc, cooldownKey);
        Long timestamp = pdc.get(cooldownKey, PersistentDataType.LONG);
        assertFalse(window.isActive(pdc));
        assertEquals(timestamp, pdc.get(cooldownKey, PersistentDataType.LONG));
        assertTrue(PerformanceCooldownSupport.isOnCooldown(pdc, cooldownKey, 1200));
    }

    @Test
    void shooterAndVictimStatesCoexist() {
        assertTrue(window.apply(pdc, 180));
        Long until = pdc.get(effectKey, PersistentDataType.LONG);
        PerformanceCooldownSupport.setCooldown(pdc, cooldownKey);
        assertTrue(window.isActive(pdc));
        assertTrue(PerformanceCooldownSupport.isOnCooldown(pdc, cooldownKey, 1200));
        assertEquals(until, pdc.get(effectKey, PersistentDataType.LONG));
        pdc.remove(effectKey);
        assertFalse(window.isActive(pdc));
        assertTrue(PerformanceCooldownSupport.isOnCooldown(pdc, cooldownKey, 1200));
    }

    @Test
    void shorterHitCannotReduceExistingEffect() {
        long until = System.nanoTime() + 60_000_000_000L;
        pdc.set(effectKey, PersistentDataType.LONG, until);
        assertFalse(window.apply(pdc, 60));
        assertEquals(until, pdc.get(effectKey, PersistentDataType.LONG));
    }

    @Test
    void longerHitRefreshesFromNowInsteadOfAddingRemainingDuration() {
        pdc.set(effectKey, PersistentDataType.LONG, System.nanoTime() + 30_000_000_000L);
        long before = System.nanoTime();
        assertTrue(window.apply(pdc, 1200));
        long after = System.nanoTime();
        long until = pdc.get(effectKey, PersistentDataType.LONG);
        assertTrue(until >= before + 60_000_000_000L);
        assertTrue(until <= after + 60_000_000_000L);
    }

    @Test
    void invalidDurationDoesNotCreateOrEraseEffect() {
        assertFalse(window.apply(pdc, 0));
        assertTrue(pdc.isEmpty());
        assertTrue(window.apply(pdc, 180));
        Long until = pdc.get(effectKey, PersistentDataType.LONG);
        assertFalse(window.apply(pdc, -1));
        assertEquals(until, pdc.get(effectKey, PersistentDataType.LONG));
    }

    @Test
    void expiredAndCorruptWindowsCanBeReplaced() {
        for (long until : new long[]{System.nanoTime() - 1_000_000L, Long.MAX_VALUE}) {
            pdc.set(effectKey, PersistentDataType.LONG, until);
            assertTrue(window.apply(pdc, 180));
            assertTrue(window.isActive(pdc));
        }
    }

    @Test
    void durationsKeepDefaultScalingAndCapUnsafeLevels() {
        assertEquals(60, MortalWoundWindowSupport.durationTicks(1, 3, 3, 0.5));
        assertEquals(180, MortalWoundWindowSupport.durationTicks(3, 3, 3, 0.5));
        assertEquals(180, MortalWoundWindowSupport.durationTicks(Integer.MAX_VALUE, 3, 3, 0.5));
        assertEquals(2400, MortalWoundWindowSupport.durationTicks(Integer.MAX_VALUE,
                Integer.MAX_VALUE, Integer.MAX_VALUE, 0.5));
    }

    @Test
    void disabledDurationOrIneffectiveHealingScaleCannotTrigger() {
        assertEquals(0, MortalWoundWindowSupport.durationTicks(3, 3, 0, 0.5));
        assertEquals(0, MortalWoundWindowSupport.durationTicks(3, 3, -1, 0.5));
        assertEquals(0, MortalWoundWindowSupport.durationTicks(0, 3, 3, 0.5));
        assertEquals(0, MortalWoundWindowSupport.durationTicks(3, 0, 3, 0.5));
        for (double scale : new double[]{1, 2, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals(0, MortalWoundWindowSupport.durationTicks(3, 3, 3, scale));
        }
        assertEquals(180, MortalWoundWindowSupport.durationTicks(3, 3, 3, 0));
    }
}
