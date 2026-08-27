package net.enchadd;

import net.enchadd.enchants.QuellEnchant;
import net.enchadd.enchants.VolleyEnchant;
import net.enchadd.listeners.QuellListener;
import net.enchadd.listeners.VolleyListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.entity.SpectralArrowMock;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

class VolleyAndCooldownBehaviorTest {

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
    void volleySpawnsExtraArrowsAndMarksSpawnedProjectiles() throws Exception {
        PlayerMock shooter = server.addPlayer("volley_shooter");
        shooter.teleport(server.getWorld("world").getSpawnLocation());

        ItemStack bow = new ItemStack(Material.BOW);
        bow.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        shooter.getInventory().setItemInMainHand(bow);

        // Use SpectralArrow to avoid ArrowMock#getCustomEffects (unimplemented in MockBukkit)
        SpectralArrowMock arrow = new SpectralArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());
        arrow.setVelocity(new Vector(0, 0, 2));

        VolleyListener listener = allocate(VolleyListener.class);
        // inject stand-in enchant + config
        setField(listener, "volley", Enchantment.SHARPNESS);
        VolleyEnchant config = Mockito.mock(VolleyEnchant.class);
        when(config.getAdditionalArrowsPerLevel()).thenReturn(1);
        when(config.getSpread()).thenReturn(0.0);
        setField(listener, "config", config);
        setField(listener, "random", java.util.concurrent.ThreadLocalRandom.current());

        int beforeEntities = server.getEntities().size();
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "volley-spawn"));
        server.getPluginManager().callEvent(new ProjectileLaunchEvent(arrow));

        int afterEntities = server.getEntities().size();
        assertTrue(afterEntities > beforeEntities, "Volley should spawn extra arrow entities");

        // ensure spawned arrow gets marker in its PDC (using enchantment key)
        boolean anyMarked = server.getEntities().stream()
            .filter(e -> e instanceof org.bukkit.entity.AbstractArrow)
            .map(e -> (org.bukkit.entity.AbstractArrow) e)
            .anyMatch(a -> a.getPersistentDataContainer().has(Enchantment.SHARPNESS.getKey(), PersistentDataType.BOOLEAN));
        assertTrue(anyMarked, "Volley should mark spawned arrows to avoid recursion");
    }

    @Test
    void volleyDoesNotRecurseOnMarkedProjectile() throws Exception {
        PlayerMock shooter = server.addPlayer("volley_marked");
        shooter.teleport(server.getWorld("world").getSpawnLocation());

        ItemStack bow = new ItemStack(Material.BOW);
        bow.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        shooter.getInventory().setItemInMainHand(bow);

        SpectralArrowMock arrow = new SpectralArrowMock(server, UUID.randomUUID());
        arrow.setShooter(shooter);
        arrow.teleport(shooter.getLocation());
        arrow.setVelocity(new Vector(0, 0, 2));

        VolleyListener listener = allocate(VolleyListener.class);
        setField(listener, "volley", Enchantment.SHARPNESS);
        VolleyEnchant config = Mockito.mock(VolleyEnchant.class);
        when(config.getAdditionalArrowsPerLevel()).thenReturn(5);
        when(config.getSpread()).thenReturn(0.0);
        setField(listener, "config", config);
        setField(listener, "random", java.util.concurrent.ThreadLocalRandom.current());

        // pre-mark the arrow as already spawned by volley
        arrow.getPersistentDataContainer().set(Enchantment.SHARPNESS.getKey(), PersistentDataType.BOOLEAN, true);

        int beforeEntities = server.getEntities().size();
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "volley-marked"));
        server.getPluginManager().callEvent(new ProjectileLaunchEvent(arrow));
        int afterEntities = server.getEntities().size();

        assertEquals(beforeEntities, afterEntities, "Marked projectile should not spawn extra arrows (no recursion)");
    }

    @Test
    void quellDoesNotTriggerWhenOnCooldown() throws Exception {
        PlayerMock player = server.addPlayer("quell_cd");
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chest.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        player.getInventory().setChestplate(chest);

        QuellListener listener = new QuellListener();
        QuellEnchant config = Mockito.mock(QuellEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getReductionPerLevel()).thenReturn(0.2);

        NamespacedKey key = new NamespacedKey("enchadd", "quell_cd_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        // put cooldown into PDC
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.LONG, System.nanoTime());

        AtomicReference<Double> scaled = new AtomicReference<>();
        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.MAGIC);
        when(event.getDamage()).thenReturn(10.0);
        doAnswer(inv -> { scaled.set(inv.getArgument(0)); return null; }).when(event).setDamage(anyDouble());

        listener.onDamage(event);
        assertNull(scaled.get(), "Quell should not scale damage while on cooldown");
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> clazz) throws Exception {
        return (T) clazz.getDeclaredConstructor().newInstance();
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

