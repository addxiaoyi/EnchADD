package net.enchadd;

import net.enchadd.enchants.FlareEnchant;
import net.enchadd.enchants.MortalWoundEnchant;
import net.enchadd.enchants.ObscureEnchant;
import net.enchadd.enchants.RicochetEnchant;
import net.enchadd.listeners.FlareListener;
import net.enchadd.listeners.MortalWoundListener;
import net.enchadd.listeners.ObscureListener;
import net.enchadd.listeners.RicochetListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.ArrowMock;
import org.mockbukkit.mockbukkit.entity.FireworkMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ProjectileLaunchStateBehaviorTest {

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
    void mortalWoundIgnoresWeaponSwappedAfterArrowLaunch() throws Exception {
        PlayerMock shooter = spawnPlayer("mortal_swap_shooter");
        PlayerMock victim = spawnPlayer("mortal_swap_victim");
        shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        MortalWoundListener listener = new MortalWoundListener();
        MortalWoundEnchant config = Mockito.mock(MortalWoundEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getAntiHealSecondsPerLevel()).thenReturn(3);
        when(config.getAntiHealScale()).thenReturn(0.5);

        NamespacedKey key = new NamespacedKey("enchadd", "mortal_launch_state_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        listener.onShoot(new ProjectileLaunchEvent(arrow));
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(1));

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);

        try (MockedStatic<PerformanceUtils> mocked = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onHit(event);
        }

        assertNull(victim.getPersistentDataContainer().get(key, PersistentDataType.LONG),
                "Arrow launched without enchant should not gain Mortal Wound after weapon swap");
    }

    @Test
    void mortalWoundKeepsLaunchLevelAfterWeaponSwapAway() throws Exception {
        PlayerMock shooter = spawnPlayer("mortal_tagged_shooter");
        PlayerMock victim = spawnPlayer("mortal_tagged_victim");
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        MortalWoundListener listener = new MortalWoundListener();
        MortalWoundEnchant config = Mockito.mock(MortalWoundEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getAntiHealSecondsPerLevel()).thenReturn(3);
        when(config.getAntiHealScale()).thenReturn(0.5);

        NamespacedKey key = new NamespacedKey("enchadd", "mortal_launch_persist_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        listener.onShoot(new ProjectileLaunchEvent(arrow));
        assertEquals(2, arrow.getPersistentDataContainer().get(key, PersistentDataType.INTEGER));

        shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);

        try (MockedStatic<PerformanceUtils> mocked = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onHit(event);
        }

        assertNotNull(victim.getPersistentDataContainer().get(key, PersistentDataType.LONG),
                "Arrow launched with Mortal Wound should keep its effect after shooter swaps weapon");
    }

    @Test
    void obscureIgnoresWeaponSwappedAfterProjectileLaunch() throws Exception {
        PlayerMock shooter = spawnPlayer("obscure_swap_shooter");
        PlayerMock victim = spawnPlayer("obscure_swap_victim");
        shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        ObscureListener listener = new ObscureListener();
        ObscureEnchant config = Mockito.mock(ObscureEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getBlindSecondsPerLevel()).thenReturn(2);

        NamespacedKey key = new NamespacedKey("enchadd", "obscure_launch_state_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        listener.onLaunch(new ProjectileLaunchEvent(arrow));
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(1));

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);

        try (MockedStatic<PerformanceUtils> mocked = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onHit(event);
        }

        assertFalse(victim.hasPotionEffect(PotionEffectType.BLINDNESS),
                "Projectile launched without Obscure should not blind after weapon swap");
    }

    @Test
    void obscureKeepsLaunchLevelAfterWeaponSwapAway() throws Exception {
        PlayerMock shooter = spawnPlayer("obscure_tagged_shooter");
        PlayerMock victim = spawnPlayer("obscure_tagged_victim");
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        ObscureListener listener = new ObscureListener();
        ObscureEnchant config = Mockito.mock(ObscureEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getBlindSecondsPerLevel()).thenReturn(2);

        NamespacedKey key = new NamespacedKey("enchadd", "obscure_launch_persist_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        listener.onLaunch(new ProjectileLaunchEvent(arrow));
        assertEquals(2, arrow.getPersistentDataContainer().get(key, PersistentDataType.INTEGER));

        shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);

        try (MockedStatic<PerformanceUtils> mocked = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            listener.onHit(event);
        }

        assertTrue(victim.hasPotionEffect(PotionEffectType.BLINDNESS),
                "Projectile launched with Obscure should still blind after shooter swaps weapon");
    }

    @Test
    void flareIgnoresWeaponSwappedAfterFireworkLaunch() throws Exception {
        PlayerMock shooter = spawnPlayer("flare_swap_shooter");
        PlayerMock victim = spawnPlayer("flare_swap_victim");
        shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

        FireworkMock firework = new FireworkMock(server, UUID.randomUUID());
        firework.setShooter(shooter);
        firework.teleport(shooter.getLocation());

        FlareListener listener = new FlareListener();
        FlareEnchant config = Mockito.mock(FlareEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getMaxTriggerChance()).thenReturn(1.0);
        when(config.getGlowSecondsPerLevel()).thenReturn(3);

        NamespacedKey levelKey = new NamespacedKey("enchadd", "flare_level_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "flare_cooldown_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "levelKey", levelKey);
        setField(listener, "cooldownKey", cooldownKey);

        listener.onLaunch(new ProjectileLaunchEvent(firework));
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(1));

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(firework);
        when(event.getEntity()).thenReturn(victim);

        listener.onDamage(event);

        assertFalse(victim.hasPotionEffect(PotionEffectType.GLOWING),
                "Firework launched without Flare should not glow targets after weapon swap");
    }

    @Test
    void flareKeepsLaunchLevelAfterWeaponSwapAway() throws Exception {
        PlayerMock shooter = spawnPlayer("flare_tagged_shooter");
        PlayerMock victim = spawnPlayer("flare_tagged_victim");
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        FireworkMock firework = new FireworkMock(server, UUID.randomUUID());
        firework.setShooter(shooter);
        firework.teleport(shooter.getLocation());

        FlareListener listener = new FlareListener();
        FlareEnchant config = Mockito.mock(FlareEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getMaxTriggerChance()).thenReturn(1.0);
        when(config.getGlowSecondsPerLevel()).thenReturn(3);

        NamespacedKey levelKey = new NamespacedKey("enchadd", "flare_level_persist_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "flare_cooldown_persist_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "levelKey", levelKey);
        setField(listener, "cooldownKey", cooldownKey);

        listener.onLaunch(new ProjectileLaunchEvent(firework));
        assertEquals(2, firework.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));

        shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(firework);
        when(event.getEntity()).thenReturn(victim);

        listener.onDamage(event);

        assertTrue(victim.hasPotionEffect(PotionEffectType.GLOWING),
                "Firework launched with Flare should still glow targets after shooter swaps weapon");
    }

    @Test
    void ricochetTagsArrowAtLaunch() throws Exception {
        PlayerMock shooter = spawnPlayer("ricochet_tagged_shooter");
        shooter.getInventory().setItemInMainHand(enchantedCrossbow(2));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        RicochetListener listener = new RicochetListener();
        RicochetEnchant config = Mockito.mock(RicochetEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getRadius()).thenReturn(6.0);
        when(config.getSpeedScale()).thenReturn(0.7);

        NamespacedKey levelKey = new NamespacedKey("enchadd", "ricochet_level_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "ricochet_cooldown_test");
        NamespacedKey bouncedKey = new NamespacedKey("enchadd", "ricochet_bounced_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "levelKey", levelKey);
        setField(listener, "cooldownKey", cooldownKey);
        setField(listener, "bouncedKey", bouncedKey);

        listener.onShoot(new ProjectileLaunchEvent(arrow));

        assertEquals(2, arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));
    }

    @Test
    void ricochetIgnoresWeaponSwappedAfterArrowLaunch() throws Exception {
        PlayerMock shooter = spawnPlayer("ricochet_swap_shooter");
        PlayerMock victim = spawnPlayer("ricochet_swap_victim");
        shooter.getInventory().setItemInMainHand(new ItemStack(Material.CROSSBOW));

        ArrowMock arrow = new ArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());

        RicochetListener listener = new RicochetListener();
        RicochetEnchant config = Mockito.mock(RicochetEnchant.class);
        when(config.getCooldownTicks()).thenReturn(40);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getRadius()).thenReturn(6.0);
        when(config.getSpeedScale()).thenReturn(0.7);

        NamespacedKey levelKey = new NamespacedKey("enchadd", "ricochet_level_state_test");
        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "ricochet_cooldown_state_test");
        NamespacedKey bouncedKey = new NamespacedKey("enchadd", "ricochet_bounced_state_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "levelKey", levelKey);
        setField(listener, "cooldownKey", cooldownKey);
        setField(listener, "bouncedKey", bouncedKey);

        listener.onShoot(new ProjectileLaunchEvent(arrow));
        assertNull(arrow.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER));

        shooter.getInventory().setItemInMainHand(enchantedCrossbow(1));

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(arrow);
        when(event.getEntity()).thenReturn(victim);

        listener.onHit(event);

        assertNull(shooter.getPersistentDataContainer().get(cooldownKey, PersistentDataType.LONG),
                "Arrow launched without Ricochet should not trigger cooldown after weapon swap");
    }

    private PlayerMock spawnPlayer(String name) {
        PlayerMock player = server.addPlayer(name);
        player.teleport(server.getWorld("world").getSpawnLocation());
        return player;
    }

    private static ItemStack enchantedCrossbow(int level) {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        item.addUnsafeEnchantment(Enchantment.SHARPNESS, level);
        return item;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
