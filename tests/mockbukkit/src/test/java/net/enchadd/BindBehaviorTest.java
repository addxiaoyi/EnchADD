package net.enchadd;

import net.enchadd.enchants.BindEnchant;
import net.enchadd.listeners.BindListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class BindBehaviorTest {

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
    void bindTagsArrowWithLaunchLevel() throws Exception {
        PlayerMock shooter = spawnPlayer("bind_tag_shooter");
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        BindListener listener = new BindListener();
        NamespacedKey key = new NamespacedKey("enchadd", "bind_launch_level_test");
        configureListener(listener, bindConfig(0, 2), key);

        ArrowMock arrow = launchArrow(shooter, listener);
        Integer taggedLevel = arrow.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);

        assertEquals(2, taggedLevel, "Bind should snapshot arrow level at projectile launch");
    }

    @Test
    void bindUsesLaunchSnapshotAfterShooterSwapsWeapon() throws Exception {
        PlayerMock shooter = spawnPlayer("bind_snapshot_shooter");
        PlayerMock victim = spawnPlayer("bind_snapshot_victim");
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        BindListener listener = new BindListener();
        NamespacedKey key = new NamespacedKey("enchadd", "bind_snapshot_test");
        configureListener(listener, bindConfig(0, 2), key);

        ArrowMock arrow = launchArrow(shooter, listener);
        shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

        EntityDamageByEntityEvent hitEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(hitEvent.getDamager()).thenReturn(arrow);
        when(hitEvent.getEntity()).thenReturn(victim);

        try (MockedStatic<net.enchadd.utils.PerformanceUtils> mocked =
                     Mockito.mockStatic(net.enchadd.utils.PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> net.enchadd.utils.PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onHit(hitEvent);
        }

        assertTrue(victim.hasPotionEffect(PotionEffectType.SLOWNESS),
                "Bind should apply slowness from the launch snapshot even after weapon swap");
        PotionEffect slowness = victim.getPotionEffect(PotionEffectType.SLOWNESS);
        assertNotNull(slowness);
        assertEquals(80, slowness.getDuration(), "Bind duration should scale from launch level");
    }

    @Test
    void bindCooldownBlocksImmediateSecondProc() throws Exception {
        PlayerMock shooter = spawnPlayer("bind_cooldown_shooter");
        PlayerMock firstVictim = spawnPlayer("bind_cooldown_first");
        PlayerMock secondVictim = spawnPlayer("bind_cooldown_second");
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(1));

        BindListener listener = new BindListener();
        NamespacedKey key = new NamespacedKey("enchadd", "bind_cooldown_test");
        configureListener(listener, bindConfig(200, 1), key);

        ArrowMock firstArrow = launchArrow(shooter, listener);
        ArrowMock secondArrow = launchArrow(shooter, listener);

        EntityDamageByEntityEvent firstHit = Mockito.mock(EntityDamageByEntityEvent.class);
        when(firstHit.getDamager()).thenReturn(firstArrow);
        when(firstHit.getEntity()).thenReturn(firstVictim);

        EntityDamageByEntityEvent secondHit = Mockito.mock(EntityDamageByEntityEvent.class);
        when(secondHit.getDamager()).thenReturn(secondArrow);
        when(secondHit.getEntity()).thenReturn(secondVictim);

        try (MockedStatic<net.enchadd.utils.PerformanceUtils> mocked =
                     Mockito.mockStatic(net.enchadd.utils.PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> net.enchadd.utils.PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onHit(firstHit);
            listener.onHit(secondHit);
        }

        assertTrue(firstVictim.hasPotionEffect(PotionEffectType.SLOWNESS),
                "First Bind proc should apply slowness");
        assertFalse(secondVictim.hasPotionEffect(PotionEffectType.SLOWNESS),
                "Cooldown should block immediate second Bind proc");
        assertNotNull(shooter.getPersistentDataContainer().get(key, PersistentDataType.LONG),
                "Successful Bind proc should write shooter cooldown timestamp");
    }

    private PlayerMock spawnPlayer(String name) {
        PlayerMock player = server.addPlayer(name);
        player.teleport(server.getWorld("world").getSpawnLocation());
        return player;
    }

    private static ItemStack enchantedCrossbow(int level) {
        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        crossbow.addUnsafeEnchantment(Enchantment.SHARPNESS, level);
        return crossbow;
    }

    private ArrowMock launchArrow(PlayerMock shooter, BindListener listener) {
        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());
        listener.onShoot(new ProjectileLaunchEvent(arrow));
        return arrow;
    }

    private static BindEnchant bindConfig(int cooldownTicks, int slowSecondsPerLevel) {
        BindEnchant config = Mockito.mock(BindEnchant.class);
        when(config.getCooldownTicks()).thenReturn(cooldownTicks);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getSlowSecondsPerLevel()).thenReturn(slowSecondsPerLevel);
        return config;
    }

    private static void configureListener(BindListener listener, BindEnchant config, NamespacedKey key) throws Exception {
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
