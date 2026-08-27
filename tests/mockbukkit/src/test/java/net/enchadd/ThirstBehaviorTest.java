package net.enchadd;

import net.enchadd.enchants.ThirstEnchant;
import net.enchadd.listeners.ThirstListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ThirstBehaviorTest {

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
    void thirstMarksCombatWindowOnPlayerVsPlayerDamage() throws Exception {
        PlayerMock victim = armPlayer("thirst_victim");
        PlayerMock damager = armPlayer("thirst_damager");

        ThirstListener listener = new ThirstListener();
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", thirstConfig());

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamager()).thenReturn(damager);

        listener.onCombatDamage(event);

        PersistentDataContainer victimPdc = victim.getPersistentDataContainer();
        PersistentDataContainer damagerPdc = damager.getPersistentDataContainer();
        org.bukkit.NamespacedKey combatKey = (org.bukkit.NamespacedKey) getField(listener, "combatKey");
        assertTrue(victimPdc.has(combatKey), "Victim should receive the thirst combat window");
        assertTrue(damagerPdc.has(combatKey), "Damager should receive the thirst combat window");
    }

    @Test
    void thirstScalesNaturalRegenAndExtraHungerLossDuringCombatWindow() throws Exception {
        PlayerMock player = armPlayer("thirst_target");
        player.setFoodLevel(12);

        ThirstListener listener = new ThirstListener();
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", thirstConfig());

        org.bukkit.NamespacedKey combatKey = (org.bukkit.NamespacedKey) getField(listener, "combatKey");
        PerformanceUtils.setWindowUntilTicks(player.getPersistentDataContainer(), combatKey, 200);

        EntityRegainHealthEvent regainEvent = mock(EntityRegainHealthEvent.class);
        when(regainEvent.getEntity()).thenReturn(player);
        when(regainEvent.getRegainReason()).thenReturn(EntityRegainHealthEvent.RegainReason.REGEN);
        when(regainEvent.getAmount()).thenReturn(10.0d);
        doAnswer(invocation -> {
            assertEquals(4.0d, invocation.getArgument(0), 1e-9);
            return null;
        }).when(regainEvent).setAmount(Mockito.anyDouble());

        listener.onRegen(regainEvent);
        Mockito.verify(regainEvent).setAmount(4.0d);

        FoodLevelChangeEvent foodEvent = mock(FoodLevelChangeEvent.class);
        when(foodEvent.getEntity()).thenReturn(player);
        when(foodEvent.getFoodLevel()).thenReturn(9);

        listener.onFoodLevelChange(foodEvent);
        Mockito.verify(foodEvent).setFoodLevel(7);
    }

    private PlayerMock armPlayer(String name) {
        PlayerMock player = server.addPlayer(name);
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chest.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        player.getInventory().setChestplate(chest);
        return player;
    }

    private static ThirstEnchant thirstConfig() {
        ThirstEnchant config = Mockito.mock(ThirstEnchant.class);
        when(config.getCombatWindowTicks()).thenReturn(160);
        when(config.getRegenReductionPerLevel()).thenReturn(0.3d);
        when(config.getExtraHungerLossPerLevel()).thenReturn(1);
        return config;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
