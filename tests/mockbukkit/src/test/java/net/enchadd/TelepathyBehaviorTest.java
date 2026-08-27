package net.enchadd;

import net.enchadd.enchants.TelepathyEnchant;
import net.enchadd.listeners.support.TelepathyDropSupport;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class TelepathyBehaviorTest {

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
    void telepathyCollectsDropsAndAssignsOwner() {
        PlayerMock player = server.addPlayer("telepathy_owner");
        Item first = mock(Item.class);
        Item second = mock(Item.class);
        when(first.isValid()).thenReturn(true);
        when(second.isValid()).thenReturn(true);

        TelepathyEnchant config = Mockito.mock(TelepathyEnchant.class);
        when(config.isOnlyUserCanPickupItems()).thenReturn(true);

        TelepathyDropSupport support = new TelepathyDropSupport(config);
        List<Item> collected = support.collectDroppedItems(player, Arrays.asList(first, null, second));

        assertEquals(List.of(first, second), collected);
        verify(first).setPickupDelay(0);
        verify(second).setPickupDelay(0);
        verify(first).setOwner(player.getUniqueId());
        verify(second).setOwner(player.getUniqueId());
    }

    @Test
    void telepathyTeleportsCollectedDropsToPlayer() {
        PlayerMock player = server.addPlayer("telepathy_runner");
        Location destination = new Location(server.getWorld("world"), 10.5, 66.0, -4.5);
        player.teleport(destination);

        Item item = server.getWorld("world").dropItem(new Location(server.getWorld("world"), 1.0, 64.0, 1.0), new ItemStack(Material.EMERALD));

        TelepathyEnchant config = Mockito.mock(TelepathyEnchant.class);
        when(config.isOnlyUserCanPickupItems()).thenReturn(false);

        TelepathyDropSupport support = new TelepathyDropSupport(config);
        support.teleportCollectedItems(player, List.of(item));

        assertSame(player.getWorld(), item.getWorld());
        assertEquals(destination.getX(), item.getLocation().getX(), 0.0001);
        assertEquals(destination.getY(), item.getLocation().getY(), 0.0001);
        assertEquals(destination.getZ(), item.getLocation().getZ(), 0.0001);
    }
}
