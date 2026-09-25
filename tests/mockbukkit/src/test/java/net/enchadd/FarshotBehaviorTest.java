package net.enchadd;

import net.enchadd.enchants.FarshotEnchant;
import net.enchadd.listeners.FarshotListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FarshotBehaviorTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        TestPluginSupport.unregisterAllHandlers();
        MockBukkit.unmock();
    }

    @Test
    void farshotTagsArrowAtLaunch() throws Exception {
        PlayerMock shooter = server.addPlayer("farshot_tagged");
        shooter.teleport(server.getWorld("world").getSpawnLocation());
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        FarshotEnchant config = farshotConfig();
        NamespacedKey levelKey = new NamespacedKey("enchadd", "farshot_level_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "farshot_cooldown_test");
        FarshotListener listener = new FarshotListener(Enchantment.SHARPNESS, levelKey, cooldownKey, config);

        listener.onShoot(new ProjectileLaunchEvent(arrow));

        assertEquals(2, arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));
    }

    @Test
    void farshotScalesDamageByDistanceAfterLaunchStateSnapshot() throws Exception {
        PlayerMock shooter = server.addPlayer("farshot_shooter");
        PlayerMock victim = server.addPlayer("farshot_victim");
        shooter.teleport(server.getWorld("world").getSpawnLocation());
        victim.teleport(server.getWorld("world").getSpawnLocation().add(24.0, 0.0, 0.0));
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        FarshotEnchant config = farshotConfig();
        NamespacedKey levelKey = new NamespacedKey("enchadd", "farshot_level_damage_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "farshot_cooldown_damage_test");
        FarshotListener listener = new FarshotListener(Enchantment.SHARPNESS, levelKey, cooldownKey, config);

        listener.onShoot(new ProjectileLaunchEvent(arrow));

        AtomicReference<Double> scaledDamage = new AtomicReference<>();
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(10.0d);
        when(event.getFinalDamage()).thenReturn(10.0d);
        doAnswer(invocation -> {
            scaledDamage.set(invocation.getArgument(0));
            return null;
        }).when(event).setDamage(Mockito.anyDouble());

        try (MockedStatic<net.enchadd.utils.PerformanceUtils> utils =
                     Mockito.mockStatic(net.enchadd.utils.PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> net.enchadd.utils.PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onHit(event);
        }

        assertEquals(11.542857142857144d, scaledDamage.get(), 1e-9);
    }

    @Test
    void farshotIgnoresWeaponSwappedAfterLaunchWhenArrowWasUnenchanted() throws Exception {
        PlayerMock shooter = server.addPlayer("farshot_swap");
        PlayerMock victim = server.addPlayer("farshot_swap_victim");
        shooter.teleport(server.getWorld("world").getSpawnLocation());
        victim.teleport(server.getWorld("world").getSpawnLocation().add(24.0, 0.0, 0.0));
        shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        FarshotEnchant config = farshotConfig();
        NamespacedKey levelKey = new NamespacedKey("enchadd", "farshot_level_state_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "farshot_cooldown_state_test");
        FarshotListener listener = new FarshotListener(Enchantment.SHARPNESS, levelKey, cooldownKey, config);

        listener.onShoot(new ProjectileLaunchEvent(arrow));
        assertNull(arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));

        shooter.getInventory().setItemInMainHand(enchantedCrossbow(1));

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(10.0d);
        when(event.getFinalDamage()).thenReturn(10.0d);

        try (MockedStatic<net.enchadd.utils.PerformanceUtils> utils =
                     Mockito.mockStatic(net.enchadd.utils.PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> net.enchadd.utils.PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onHit(event);
        }

        assertNull(shooter.getPersistentDataContainer().get(cooldownKey, PersistentDataType.LONG));
    }

    @Test
    void farshotUsesLaunchPositionWhenShooterMovesAfterFiring() {
        PlayerMock shooter = server.addPlayer("farshot_moved");
        PlayerMock victim = server.addPlayer("farshot_moved_victim");
        shooter.teleport(server.getWorld("world").getSpawnLocation());
        victim.teleport(server.getWorld("world").getSpawnLocation().add(6.0, 0.0, 0.0));
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());
        FarshotEnchant config = farshotConfig();
        NamespacedKey levelKey = new NamespacedKey("enchadd", "farshot_move_level_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "farshot_move_cooldown_test");
        FarshotListener listener = new FarshotListener(Enchantment.SHARPNESS, levelKey, cooldownKey, config);
        listener.onShoot(new ProjectileLaunchEvent(arrow));

        shooter.teleport(shooter.getLocation().add(-40.0, 0.0, 0.0));
        EntityDamageByEntityEvent event = damageEvent(arrow, victim, 10.0d);
        listener.onHit(event);

        Mockito.verify(event, Mockito.never()).setDamage(Mockito.anyDouble());
        assertNull(shooter.getPersistentDataContainer().get(cooldownKey, PersistentDataType.LONG));
    }

    @Test
    void farshotKeepsLongRangeBonusWhenShooterMovesCloser() {
        PlayerMock shooter = server.addPlayer("farshot_closer");
        PlayerMock victim = server.addPlayer("farshot_closer_victim");
        shooter.teleport(server.getWorld("world").getSpawnLocation());
        victim.teleport(server.getWorld("world").getSpawnLocation().add(24.0, 0.0, 0.0));
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());
        FarshotEnchant config = farshotConfig();
        NamespacedKey levelKey = new NamespacedKey("enchadd", "farshot_closer_level_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "farshot_closer_cooldown_test");
        FarshotListener listener = new FarshotListener(Enchantment.SHARPNESS, levelKey, cooldownKey, config);
        listener.onShoot(new ProjectileLaunchEvent(arrow));
        shooter.teleport(victim.getLocation().add(-1.0, 0.0, 0.0));

        EntityDamageByEntityEvent event = damageEvent(arrow, victim, 10.0d);
        try (MockedStatic<net.enchadd.utils.PerformanceUtils> utils =
                     Mockito.mockStatic(net.enchadd.utils.PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> net.enchadd.utils.PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onHit(event);
        }

        Mockito.verify(event).setDamage(11.542857142857144d);
    }

    private static ItemStack enchantedCrossbow(int level) {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, level);
        return item;
    }

    private static FarshotEnchant farshotConfig() {
        FarshotEnchant config = Mockito.mock(FarshotEnchant.class);
        when(config.getCooldownTicks()).thenReturn(40);
        when(config.getTriggerChance()).thenReturn(1.0d);
        when(config.getMaxTriggerChance()).thenReturn(1.0d);
        when(config.getBonusDamagePerLevel()).thenReturn(0.18d);
        when(config.getMinDistance()).thenReturn(12.0d);
        when(config.getMaxDistance()).thenReturn(40.0d);
        when(config.getMaxLevel()).thenReturn(3);
        return config;
    }

    private static EntityDamageByEntityEvent damageEvent(ArrowMock arrow, PlayerMock victim, double damage) {
        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(damage);
        when(event.getFinalDamage()).thenReturn(damage);
        return event;
    }
}
