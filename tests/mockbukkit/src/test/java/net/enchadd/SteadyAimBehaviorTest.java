package net.enchadd;

import net.enchadd.enchants.SteadyAimEnchant;
import net.enchadd.listeners.SteadyAimListener;
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

import java.lang.reflect.Field;
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

        SteadyAimListener listener = new SteadyAimListener();
        SteadyAimEnchant config = Mockito.mock(SteadyAimEnchant.class);
        when(config.getCooldownTicks()).thenReturn(100);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getMaxTriggerChance()).thenReturn(1.0);
        when(config.getBonusDamagePerLevel()).thenReturn(0.2);

        NamespacedKey levelKey = new NamespacedKey("enchadd", "steady_aim_level_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "steady_aim_cooldown_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "levelKey", levelKey);
        setField(listener, "cooldownKey", cooldownKey);

        listener.onShoot(new ProjectileLaunchEvent(arrow));
        assertEquals(2, arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));

        AtomicReference<Double> adjustedDamage = new AtomicReference<>();
        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(10.0);
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

        SteadyAimListener listener = new SteadyAimListener();
        SteadyAimEnchant config = Mockito.mock(SteadyAimEnchant.class);
        when(config.getCooldownTicks()).thenReturn(100);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getMaxTriggerChance()).thenReturn(1.0);
        when(config.getBonusDamagePerLevel()).thenReturn(0.2);

        NamespacedKey levelKey = new NamespacedKey("enchadd", "steady_aim_slow_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "steady_aim_slow_cooldown_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "levelKey", levelKey);
        setField(listener, "cooldownKey", cooldownKey);

        listener.onShoot(new ProjectileLaunchEvent(arrow));
        assertNull(arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));
    }

    private static ItemStack enchantedBow(int level) {
        ItemStack item = new ItemStack(Material.BOW);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, level);
        return item;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
