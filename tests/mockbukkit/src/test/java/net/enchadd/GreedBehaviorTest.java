package net.enchadd;

import net.enchadd.enchants.GreedEnchant;
import net.enchadd.listeners.GreedListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

class GreedBehaviorTest {

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
    void greedBoostsDroppedExperienceAndStoresVulnerabilityWindow() throws Exception {
        PlayerMock killer = server.addPlayer("greed_killer");
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 2);
        killer.getInventory().setItemInMainHand(sword);

        GreedListener listener = new GreedListener();
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", greedConfig());

        EntityDeathEvent event = mock(EntityDeathEvent.class);
        var damageSource = TestDamageSources.directPlayerDamage(killer);
        when(event.getDamageSource()).thenReturn(damageSource);
        when(event.getDroppedExp()).thenReturn(10);

        listener.onEntityDeath(event);

        Mockito.verify(event).setDroppedExp(16);
        PersistentDataContainer pdc = killer.getPersistentDataContainer();
        NamespacedKey scaleKey = (NamespacedKey) getField(listener, "scaleKey");
        Double scale = pdc.get(scaleKey, PersistentDataType.DOUBLE);
        assertEquals(1.4d, scale, 1e-9);
    }

    @Test
    void greedScalesIncomingDamageWhileVulnerabilityWindowIsActive() throws Exception {
        PlayerMock player = server.addPlayer("greed_target");
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setItemInMainHand(sword);

        GreedListener listener = new GreedListener();
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", greedConfig());

        NamespacedKey untilKey = (NamespacedKey) getField(listener, "untilKey");
        NamespacedKey scaleKey = (NamespacedKey) getField(listener, "scaleKey");
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        PerformanceUtils.setWindowUntilSeconds(pdc, untilKey, 30);
        pdc.set(scaleKey, PersistentDataType.DOUBLE, 1.5d);

        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getDamage()).thenReturn(8.0d);
        doAnswer(invocation -> {
            assertEquals(12.0d, invocation.getArgument(0), 1e-9);
            return null;
        }).when(event).setDamage(Mockito.anyDouble());

        listener.onDamage(event);

        Mockito.verify(event).setDamage(12.0d);
        assertTrue(pdc.has(scaleKey), "Active greed vulnerability should remain stored while window is valid");
    }

    private static GreedEnchant greedConfig() {
        GreedEnchant config = Mockito.mock(GreedEnchant.class);
        when(config.getXpBonusPerLevel()).thenReturn(0.3d);
        when(config.getMaxXpMultiplier()).thenReturn(3.0d);
        when(config.getVulnerabilityPerLevel()).thenReturn(0.2d);
        when(config.getMaxVulnerabilityMultiplier()).thenReturn(1.0d);
        when(config.getVulnerabilitySecondsPerLevel()).thenReturn(6);
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
