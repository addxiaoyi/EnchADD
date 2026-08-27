package net.enchadd;

import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceUtilsCooldownTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void setCooldownWritesNanoTimestampAndExpires() {
        PlayerMock player = server.addPlayer("cooldown_player");
        NamespacedKey key = new NamespacedKey("enchadd", "cooldown_test");

        PerformanceUtils.setCooldown(player.getPersistentDataContainer(), key);

        Long stored = player.getPersistentDataContainer().get(key, PersistentDataType.LONG);
        assertNotNull(stored, "Cooldown should be stored as a LONG timestamp in PDC");
        assertTrue(stored > 0L, "Stored cooldown timestamp should be positive");

        assertTrue(PerformanceUtils.isOnCooldown(player.getPersistentDataContainer(), key, 1),
                "Cooldown should be active immediately after writing");

        player.getPersistentDataContainer().set(key, PersistentDataType.LONG,
                System.nanoTime() - (1L * 50L * 1_000_000L) - 10_000_000L);

        assertFalse(PerformanceUtils.isOnCooldown(player.getPersistentDataContainer(), key, 1),
                "Cooldown should expire after the configured window");
        assertEquals(0L, PerformanceUtils.getRemainingCooldown(player.getPersistentDataContainer(), key, 1),
                "Expired cooldown should report zero remaining time");
    }
}
