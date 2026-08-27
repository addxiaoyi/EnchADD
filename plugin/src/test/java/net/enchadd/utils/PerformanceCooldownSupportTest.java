package net.enchadd.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

class PerformanceCooldownSupportTest {

    @Test
    void cooldownIsInactiveForNonPositiveTicksOrMissingTimestamp() {
        TestPersistentDataContainer pdc = new TestPersistentDataContainer();
        NamespacedKey key = key("cooldown");

        assertFalse(PerformanceCooldownSupport.isOnCooldown(pdc, key, 0));
        assertFalse(PerformanceCooldownSupport.isOnCooldown(pdc, key, 5));
    }

    @Test
    void cooldownIsActiveForRecentTimestampAndInactiveForOldTimestamp() {
        TestPersistentDataContainer pdc = new TestPersistentDataContainer();
        NamespacedKey key = key("cooldown");

        pdc.set(key, PersistentDataType.LONG, System.nanoTime());
        assertTrue(PerformanceCooldownSupport.isOnCooldown(pdc, key, 20));

        pdc.set(key, PersistentDataType.LONG, System.nanoTime() - 2_000_000_000L);
        assertFalse(PerformanceCooldownSupport.isOnCooldown(pdc, key, 1));
    }

    @Test
    void windowActiveTracksUntilTimestamp() {
        TestPersistentDataContainer pdc = new TestPersistentDataContainer();
        NamespacedKey key = key("window");

        pdc.set(key, PersistentDataType.LONG, System.nanoTime() - 1_000_000L);
        assertFalse(PerformanceCooldownSupport.isWindowActive(pdc, key));

        pdc.set(key, PersistentDataType.LONG, System.nanoTime() + 1_000_000_000L);
        assertTrue(PerformanceCooldownSupport.isWindowActive(pdc, key));
    }

    @Test
    void futureTimestampsAreDiscardedInsteadOfCreatingPermanentCooldowns() {
        TestPersistentDataContainer pdc = new TestPersistentDataContainer();
        NamespacedKey cooldown = key("future-cooldown");
        NamespacedKey window = key("future-window");

        pdc.set(cooldown, PersistentDataType.LONG, Long.MAX_VALUE);
        pdc.set(window, PersistentDataType.LONG, Long.MAX_VALUE);

        assertFalse(PerformanceCooldownSupport.isOnCooldown(pdc, cooldown, 20));
        assertFalse(PerformanceCooldownSupport.isWindowActive(pdc, window));
        assertNull(pdc.get(cooldown, PersistentDataType.LONG));
        assertNull(pdc.get(window, PersistentDataType.LONG));
    }

    private static NamespacedKey key(String value) {
        return new NamespacedKey("enchadd", value);
    }
}
