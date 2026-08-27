package net.enchadd;

import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PerformanceUtilsShieldBlockTest {

    private ServerMock server;
    private int damagerCounter = 0;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void frontHitWithRaisedShieldCountsAsSuccessfulBlock() {
        Player player = mockBlockingShieldPlayer(new Vector(0.0, 0.0, 1.0));
        Entity damager = spawnDamagerAt(0.0, 64.0, 2.0);
        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(damager);
        when(event.getDamageSource()).thenReturn(null);

        assertTrue(PerformanceUtils.isSuccessfulShieldBlock(player, event),
                "front hit while shield is raised should be treated as a successful block");
    }

    @Test
    void sideHitDoesNotCountAsSuccessfulBlock() {
        Player player = mockBlockingShieldPlayer(new Vector(0.0, 0.0, 1.0));
        Entity damager = spawnDamagerAt(2.0, 64.0, 0.0);
        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(damager);
        when(event.getDamageSource()).thenReturn(null);

        assertFalse(PerformanceUtils.isSuccessfulShieldBlock(player, event),
                "side hit should not pass shield-facing validation");
    }

    @Test
    void requiresActualRaisedShieldItem() {
        Player player = Mockito.mock(Player.class);
        when(player.isBlocking()).thenReturn(true);
        when(player.isHandRaised()).thenReturn(true);
        when(player.getHandRaisedTime()).thenReturn(6);
        when(player.getActiveItem()).thenReturn(new ItemStack(Material.AIR));

        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getItemInOffHand()).thenReturn(new ItemStack(Material.AIR));
        when(player.getEquipment()).thenReturn(equipment);

        Location loc = new Location(Mockito.mock(World.class), 0.0, 64.0, 0.0);
        loc.setDirection(new Vector(0.0, 0.0, 1.0));
        when(player.getLocation()).thenReturn(loc);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(spawnDamagerAt(0.0, 64.0, 2.0));
        when(event.getDamageSource()).thenReturn(null);

        assertFalse(PerformanceUtils.isSuccessfulShieldBlock(player, event),
                "blocking state without a raised shield item should be rejected");
    }

    @Test
    void requiresShieldWarmupTicks() {
        Player player = mockBlockingShieldPlayer(new Vector(0.0, 0.0, 1.0));
        when(player.getHandRaisedTime()).thenReturn(3);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(spawnDamagerAt(0.0, 64.0, 2.0));
        when(event.getDamageSource()).thenReturn(null);

        assertFalse(PerformanceUtils.isSuccessfulShieldBlock(player, event),
                "shield should only count after the vanilla warmup window");
    }

    @Test
    void likelyShieldFacingBlockUsesSameRaisedShieldGate() {
        Player player = mockBlockingShieldPlayer(new Vector(0.0, 0.0, 1.0));
        assertTrue(PerformanceUtils.isLikelyShieldFacingBlock(player, new Vector(0.0, 0.3, -1.0)),
                "front-facing knockback should pass the facing-aware shield gate");

        when(player.isHandRaised()).thenReturn(false);
        assertFalse(PerformanceUtils.isLikelyShieldFacingBlock(player, new Vector(0.0, 0.3, -1.0)),
                "knockback path should reject non-raised shields just like direct damage path");
    }

    @Test
    void likelyShieldFacingBlockRejectsSideKnockback() {
        Player player = mockBlockingShieldPlayer(new Vector(0.0, 0.0, 1.0));
        assertFalse(PerformanceUtils.isLikelyShieldFacingBlock(player, new Vector(1.0, 0.3, 0.0)),
                "side knockback should not be considered a valid shield-facing block");
    }

    private static Player mockBlockingShieldPlayer(Vector direction) {
        Player player = Mockito.mock(Player.class);
        when(player.isBlocking()).thenReturn(true);
        when(player.isHandRaised()).thenReturn(true);
        when(player.getHandRaisedTime()).thenReturn(6);
        when(player.getActiveItem()).thenReturn(new ItemStack(Material.SHIELD));

        Location location = new Location(Mockito.mock(World.class), 0.0, 64.0, 0.0);
        location.setDirection(direction);
        when(player.getLocation()).thenReturn(location);
        return player;
    }

    private Entity spawnDamagerAt(double x, double y, double z) {
        PlayerMock damager = server.addPlayer("shield_damager_" + (++damagerCounter));
        damager.teleport(new Location(damager.getWorld(), x, y, z));
        return damager;
    }
}
