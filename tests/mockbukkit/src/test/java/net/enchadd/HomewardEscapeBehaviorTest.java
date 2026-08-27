package net.enchadd;

import net.enchadd.enchants.HomewardEnchant;
import net.enchadd.listeners.HomewardListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class HomewardEscapeBehaviorTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void homewardArmsAnEscapeWindowAndConsumesItOnFallDamage() throws Exception {
        PlayerMock defender = server.addPlayer("homeward_defender");
        PlayerMock enemy = server.addPlayer("homeward_enemy");

        ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        leggings.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        defender.getInventory().setLeggings(leggings);
        defender.setSprinting(true);

        HomewardListener listener = new HomewardListener();
        HomewardEnchant config = Mockito.mock(HomewardEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getEscapeWindowTicks()).thenReturn(60);
        when(config.getSpeedSecondsPerLevel()).thenReturn(1);
        when(config.getSpeedAmplifier()).thenReturn(0);
        when(config.getFallDamageReductionPerLevel()).thenReturn(0.20);
        when(config.getMaxFallDamageReduction()).thenReturn(0.60);

        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "homeward_test_cooldown");
        NamespacedKey windowKey = new NamespacedKey("enchadd", "homeward_test_window");
        NamespacedKey levelKey = new NamespacedKey("enchadd", "homeward_test_level");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "cooldownKey", cooldownKey);
        setField(listener, "windowKey", windowKey);
        setField(listener, "levelKey", levelKey);

        EntityDamageByEntityEvent combatEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(combatEvent.getEntity()).thenReturn(defender);
        when(combatEvent.getDamager()).thenReturn(enemy);

        listener.onCombatDamage(combatEvent);

        PersistentDataContainer pdc = defender.getPersistentDataContainer();
        assertTrue(PerformanceUtils.isWindowActive(pdc, windowKey), "combat sprint hit should arm the homeward escape window");
        assertTrue(defender.hasPotionEffect(PotionEffectType.SPEED), "combat sprint hit should grant a short escape speed burst");

        EntityDamageEvent fallEvent = Mockito.mock(EntityDamageEvent.class);
        when(fallEvent.getEntity()).thenReturn(defender);
        when(fallEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
        when(fallEvent.getDamage()).thenReturn(10.0);

        AtomicReference<Double> reducedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            reducedDamage.set(invocation.getArgument(0));
            return null;
        }).when(fallEvent).setDamage(anyDouble());

        listener.onFallDamage(fallEvent);
        assertEquals(6.0, reducedDamage.get(), 0.0001, "armed homeward window should reduce the next fall damage");
        assertFalse(PerformanceUtils.isWindowActive(pdc, windowKey), "homeward window should be consumed by the protected fall");

        EntityDamageEvent secondFallEvent = Mockito.mock(EntityDamageEvent.class);
        when(secondFallEvent.getEntity()).thenReturn(defender);
        when(secondFallEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
        when(secondFallEvent.getDamage()).thenReturn(10.0);
        reducedDamage.set(null);
        doAnswer(invocation -> {
            reducedDamage.set(invocation.getArgument(0));
            return null;
        }).when(secondFallEvent).setDamage(anyDouble());

        listener.onFallDamage(secondFallEvent);
        assertNull(reducedDamage.get(), "without a fresh combat window homeward should not keep reducing fall damage");
    }

    @Test
    void homewardUsesTheLeggingsLevelFromTheSprintHitThatArmedTheWindow() throws Exception {
        PlayerMock defender = server.addPlayer("homeward_snapshot_defender");
        PlayerMock enemy = server.addPlayer("homeward_snapshot_enemy");

        ItemStack initialLeggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        initialLeggings.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        defender.getInventory().setLeggings(initialLeggings);
        defender.setSprinting(true);

        HomewardListener listener = new HomewardListener();
        HomewardEnchant config = Mockito.mock(HomewardEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getEscapeWindowTicks()).thenReturn(60);
        when(config.getSpeedSecondsPerLevel()).thenReturn(1);
        when(config.getSpeedAmplifier()).thenReturn(0);
        when(config.getFallDamageReductionPerLevel()).thenReturn(0.20);
        when(config.getMaxFallDamageReduction()).thenReturn(0.80);

        NamespacedKey cooldownKey = new NamespacedKey("enchadd", "homeward_snapshot_cooldown");
        NamespacedKey windowKey = new NamespacedKey("enchadd", "homeward_snapshot_window");
        NamespacedKey levelKey = new NamespacedKey("enchadd", "homeward_snapshot_level");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "cooldownKey", cooldownKey);
        setField(listener, "windowKey", windowKey);
        setField(listener, "levelKey", levelKey);

        EntityDamageByEntityEvent combatEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(combatEvent.getEntity()).thenReturn(defender);
        when(combatEvent.getDamager()).thenReturn(enemy);
        listener.onCombatDamage(combatEvent);

        ItemStack swappedLeggings = new ItemStack(Material.DIAMOND_LEGGINGS);
        swappedLeggings.addUnsafeEnchantment(Enchantment.SHARPNESS, 4);
        defender.getInventory().setLeggings(swappedLeggings);

        EntityDamageEvent fallEvent = Mockito.mock(EntityDamageEvent.class);
        when(fallEvent.getEntity()).thenReturn(defender);
        when(fallEvent.getCause()).thenReturn(EntityDamageEvent.DamageCause.FALL);
        when(fallEvent.getDamage()).thenReturn(10.0);

        AtomicReference<Double> reducedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            reducedDamage.set(invocation.getArgument(0));
            return null;
        }).when(fallEvent).setDamage(anyDouble());

        listener.onFallDamage(fallEvent);
        assertEquals(8.0, reducedDamage.get(), 0.0001, "homeward should use the level from when the escape window was armed");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
