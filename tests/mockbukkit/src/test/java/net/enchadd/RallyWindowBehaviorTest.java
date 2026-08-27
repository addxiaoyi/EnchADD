package net.enchadd;

import net.enchadd.enchants.RallyEnchant;
import net.enchadd.listeners.RallyListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
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
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class RallyWindowBehaviorTest {

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
    void rallyArmsAfterDamageAndIsConsumedByNextCounterattack() throws Exception {
        PlayerMock defender = server.addPlayer("rally_defender");
        PlayerMock enemy = server.addPlayer("rally_enemy");

        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chestplate.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        defender.getInventory().setChestplate(chestplate);

        RallyListener listener = new RallyListener();
        RallyEnchant config = Mockito.mock(RallyEnchant.class);
        when(config.getRetaliationWindowTicks()).thenReturn(80);
        when(config.getBonusDamagePerLevel()).thenReturn(1.0);
        when(config.getMaxBonusDamage()).thenReturn(2.0);

        NamespacedKey windowKey = new NamespacedKey("enchadd", "rally_test_window");
        NamespacedKey levelKey = new NamespacedKey("enchadd", "rally_test_level");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "windowKey", windowKey);
        setField(listener, "levelKey", levelKey);

        EntityDamageByEntityEvent damagedEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(damagedEvent.getEntity()).thenReturn(defender);
        when(damagedEvent.getDamager()).thenReturn(enemy);
        when(damagedEvent.getFinalDamage()).thenReturn(4.0);

        listener.onDamaged(damagedEvent);
        PersistentDataContainer pdc = defender.getPersistentDataContainer();
        assertFalse(!PerformanceUtils.isWindowActive(pdc, windowKey), "taking damage should arm the rally retaliation window");

        EntityDamageByEntityEvent counterEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(counterEvent.getDamager()).thenReturn(defender);
        when(counterEvent.getEntity()).thenReturn(enemy);
        when(counterEvent.getDamage()).thenReturn(5.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(counterEvent).setDamage(anyDouble());

        listener.onCounterattack(counterEvent);
        assertEquals(7.0, appliedDamage.get(), 0.0001, "armed rally window should boost the next counterattack");
        assertFalse(PerformanceUtils.isWindowActive(pdc, windowKey), "rally window should be consumed after the empowered hit");

        appliedDamage.set(null);
        listener.onCounterattack(counterEvent);
        assertNull(appliedDamage.get(), "without a fresh damage window rally should not trigger again");
    }

    @Test
    void rallyUsesTheChestplateLevelFromWhenDamageWasTaken() throws Exception {
        PlayerMock defender = server.addPlayer("rally_snapshot_defender");
        PlayerMock enemy = server.addPlayer("rally_snapshot_enemy");

        ItemStack initialChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        initialChestplate.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        defender.getInventory().setChestplate(initialChestplate);

        RallyListener listener = new RallyListener();
        RallyEnchant config = Mockito.mock(RallyEnchant.class);
        when(config.getRetaliationWindowTicks()).thenReturn(80);
        when(config.getBonusDamagePerLevel()).thenReturn(1.0);
        when(config.getMaxBonusDamage()).thenReturn(10.0);

        NamespacedKey windowKey = new NamespacedKey("enchadd", "rally_snapshot_window");
        NamespacedKey levelKey = new NamespacedKey("enchadd", "rally_snapshot_level");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "windowKey", windowKey);
        setField(listener, "levelKey", levelKey);

        EntityDamageByEntityEvent damagedEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(damagedEvent.getEntity()).thenReturn(defender);
        when(damagedEvent.getDamager()).thenReturn(enemy);
        when(damagedEvent.getFinalDamage()).thenReturn(4.0);
        listener.onDamaged(damagedEvent);

        ItemStack swappedChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        swappedChestplate.addUnsafeEnchantment(Enchantment.SHARPNESS, 4);
        defender.getInventory().setChestplate(swappedChestplate);

        EntityDamageByEntityEvent counterEvent = Mockito.mock(EntityDamageByEntityEvent.class);
        when(counterEvent.getDamager()).thenReturn(defender);
        when(counterEvent.getEntity()).thenReturn(enemy);
        when(counterEvent.getDamage()).thenReturn(5.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(counterEvent).setDamage(anyDouble());

        listener.onCounterattack(counterEvent);
        assertEquals(6.0, appliedDamage.get(), 0.0001, "rally should use the retaliation level captured on the damage event");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
