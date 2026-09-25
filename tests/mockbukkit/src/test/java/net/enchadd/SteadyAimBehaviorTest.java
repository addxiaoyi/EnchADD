package net.enchadd;

import net.enchadd.enchants.SteadyAimEnchant;
import net.enchadd.listeners.SteadyAimListener;
import net.enchadd.listeners.support.SteadyAimProjectileSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.ArrowMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class SteadyAimBehaviorTest {

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
    void steadyAimSnapshotsLaunchLevelAndScalesDamage() throws Exception {
        PlayerMock shooter = server.addPlayer("steady_aim_shooter");
        PlayerMock victim = server.addPlayer("steady_aim_victim");
        shooter.teleport(server.getWorld("world").getSpawnLocation());
        victim.teleport(server.getWorld("world").getSpawnLocation().add(12.0, 0.0, 0.0));
        shooter.getInventory().setItemInMainHand(enchantedBow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.setVelocity(new org.bukkit.util.Vector(3.2, 0.0, 0.0));
        arrow.teleport(shooter.getLocation());

        SteadyAimEnchant config = Mockito.mock(SteadyAimEnchant.class);
        when(config.getCooldownTicks()).thenReturn(100);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getMaxTriggerChance()).thenReturn(1.0);
        when(config.getBonusDamagePerLevel()).thenReturn(0.2);
        when(config.getMaxLevel()).thenReturn(3);

        NamespacedKey levelKey = new NamespacedKey("enchadd", "steady_aim_level_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "steady_aim_cooldown_test");
        SteadyAimListener listener = new SteadyAimListener(Enchantment.SHARPNESS, levelKey, cooldownKey,
                config, new SteadyAimProjectileSupport());

        listener.onShoot(new ProjectileLaunchEvent(arrow));
        assertEquals(2, arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));

        AtomicReference<Double> adjustedDamage = new AtomicReference<>();
        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(10.0);
        when(event.getFinalDamage()).thenReturn(10.0);
        doAnswer(invocation -> {
            adjustedDamage.set(invocation.getArgument(0));
            return null;
        }).when(event).setDamage(Mockito.anyDouble());

        listener.onHit(event);
        assertEquals(14.0, adjustedDamage.get(), 0.0001);
    }

    @Test
    void steadyAimDoesNotTagSlowProjectiles() throws Exception {
        PlayerMock shooter = server.addPlayer("steady_aim_slow");
        shooter.getInventory().setItemInMainHand(enchantedBow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.setVelocity(new org.bukkit.util.Vector(1.0, 0.0, 0.0));
        arrow.teleport(shooter.getLocation());

        SteadyAimEnchant config = Mockito.mock(SteadyAimEnchant.class);
        when(config.getCooldownTicks()).thenReturn(100);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getMaxTriggerChance()).thenReturn(1.0);
        when(config.getBonusDamagePerLevel()).thenReturn(0.2);
        when(config.getMaxLevel()).thenReturn(3);

        NamespacedKey levelKey = new NamespacedKey("enchadd", "steady_aim_slow_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "steady_aim_slow_cooldown_test");
        SteadyAimListener listener = new SteadyAimListener(Enchantment.SHARPNESS, levelKey, cooldownKey,
                config, new SteadyAimProjectileSupport());

        listener.onShoot(new ProjectileLaunchEvent(arrow));
        assertNull(arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));
    }

    @Test
    void steadyAimProcessesEachArrowLaunchOnlyOnce() {
        PlayerMock shooter = server.addPlayer("steady_aim_duplicate");
        shooter.getInventory().setItemInMainHand(enchantedBow(2));
        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.setVelocity(new org.bukkit.util.Vector(3.2, 0.0, 0.0));

        SteadyAimEnchant config = Mockito.mock(SteadyAimEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getMaxTriggerChance()).thenReturn(1.0);
        when(config.getBonusDamagePerLevel()).thenReturn(0.2);
        when(config.getMaxLevel()).thenReturn(3);
        NamespacedKey levelKey = new NamespacedKey("enchadd", "steady_aim_duplicate_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "steady_aim_duplicate_cooldown_test");
        SteadyAimListener listener = new SteadyAimListener(Enchantment.SHARPNESS, levelKey, cooldownKey,
                config, new SteadyAimProjectileSupport());

        ProjectileLaunchEvent event = new ProjectileLaunchEvent(arrow);
        try (MockedStatic<PerformanceUtils> utils =
                     Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onShoot(event);
            shooter.getInventory().setItemInMainHand(enchantedBow(3));
            listener.onShoot(event);
            utils.verify(() -> PerformanceUtils.rollChance(Mockito.anyDouble()), Mockito.times(1));
        }

        assertEquals(2, arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));
    }

    private static ItemStack enchantedBow(int level) {
        ItemStack item = new ItemStack(Material.BOW);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, level);
        return item;
    }

}
