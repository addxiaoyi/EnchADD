package net.enchadd;

import net.enchadd.events.HomecomingEvent;
import net.enchadd.listeners.HomecomingListener;
import net.enchadd.listeners.support.HomecomingResurrectSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomecomingBehaviorTest {

    @Test
    void homecomingUsesEventAdjustedDestinationAndRespectsCancellation() {
        Enchantment enchantment = Enchantment.UNBREAKING;
        HomecomingListener listener = new HomecomingListener(enchantment, new HomecomingResurrectSupport());

        Server server = Mockito.mock(Server.class);
        PluginManager pluginManager = Mockito.mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pluginManager);

        World world = Mockito.mock(World.class);
        Location spawnLocation = new Location(world, 0.0, 80.0, 0.0);
        when(world.getSpawnLocation()).thenReturn(spawnLocation);

        Player player = Mockito.mock(Player.class);
        when(player.getServer()).thenReturn(server);
        when(player.getRespawnLocation()).thenReturn(null);
        when(player.getWorld()).thenReturn(world);

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        ItemStack totem = Mockito.mock(ItemStack.class);
        when(equipment.getItem(EquipmentSlot.OFF_HAND)).thenReturn(totem);

        EntityResurrectEvent event = Mockito.mock(EntityResurrectEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getHand()).thenReturn(EquipmentSlot.OFF_HAND);

        Location redirected = new Location(world, 12.0, 75.0, -6.0);
        AtomicInteger eventCalls = new AtomicInteger(0);
        doAnswer(invocation -> {
            HomecomingEvent homecomingEvent = invocation.getArgument(0);
            if (eventCalls.getAndIncrement() == 0) {
                homecomingEvent.setLocation(redirected);
            } else {
                homecomingEvent.setCancelled(true);
            }
            return null;
        }).when(pluginManager).callEvent(Mockito.any(HomecomingEvent.class));

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(totem, enchantment)).thenReturn(1);

            listener.onHomecoming(event);
            verify(player).teleport(redirected, PlayerTeleportEvent.TeleportCause.PLUGIN);

            clearInvocations(player);
            listener.onHomecoming(event);
            verify(player, never()).teleport(Mockito.any(Location.class), Mockito.eq(PlayerTeleportEvent.TeleportCause.PLUGIN));
        }
    }
}
