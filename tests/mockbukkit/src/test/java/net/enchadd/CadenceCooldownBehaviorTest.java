package net.enchadd;

import net.enchadd.enchants.CadenceEnchant;
import net.enchadd.listeners.CadenceListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class CadenceCooldownBehaviorTest {

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
    void cadenceFirstHitBoostsThenCooldownBlocksUntilExpired() throws Exception {
        PlayerMock attacker = server.addPlayer("cadence_attacker");
        PlayerMock victim = server.addPlayer("cadence_victim");

        ItemStack weapon = new ItemStack(Material.DIAMOND_SWORD);
        weapon.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        attacker.getInventory().setItemInMainHand(weapon);

        CadenceListener listener = new CadenceListener();
        CadenceEnchant config = Mockito.mock(CadenceEnchant.class);
        when(config.getCooldownTicks()).thenReturn(100);
        when(config.getBonusDamagePerLevel()).thenReturn(1.0);
        when(config.getMaxBonusDamage()).thenReturn(2.5);

        NamespacedKey key = new NamespacedKey("enchadd", "cadence_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);
        when(event.getDamage()).thenReturn(6.0);

        AtomicReference<Double> appliedDamage = new AtomicReference<>();
        doAnswer(invocation -> {
            appliedDamage.set(invocation.getArgument(0));
            return null;
        }).when(event).setDamage(anyDouble());

        listener.onMeleeHit(event);
        assertEquals(8.5, appliedDamage.get(), 0.0001, "first cadence hit should receive capped bonus damage");

        PersistentDataContainer pdc = attacker.getPersistentDataContainer();
        Long stamp = pdc.get(key, PersistentDataType.LONG);
        assertNotNull(stamp, "cadence should write cooldown timestamp");

        appliedDamage.set(null);
        listener.onMeleeHit(event);
        assertNull(appliedDamage.get(), "second immediate hit should be blocked by cooldown");

        long cooldownNanos = 100L * 50L * 1_000_000L;
        pdc.set(key, PersistentDataType.LONG, System.nanoTime() - cooldownNanos - 10_000_000L);

        listener.onMeleeHit(event);
        assertEquals(8.5, appliedDamage.get(), 0.0001, "after cooldown expiry cadence should trigger again");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
