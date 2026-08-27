package net.enchadd;

import net.enchadd.enchants.DispelEnchant;
import net.enchadd.enchants.QuellEnchant;
import net.enchadd.listeners.DispelListener;
import net.enchadd.listeners.QuellListener;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

class ListenerBehaviorTest {

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
    void quellEventuallyScalesMagicDamage() throws Exception {
        PlayerMock player = server.addPlayer("quell_tester");
        ItemStack chest = new ItemStack(org.bukkit.Material.DIAMOND_CHESTPLATE);
        chest.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        player.getInventory().setChestplate(chest);

        QuellListener listener = new QuellListener();
        QuellEnchant config = Mockito.mock(QuellEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0); // capped to 0.6 in listener
        when(config.getReductionPerLevel()).thenReturn(0.2);

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", new NamespacedKey("enchadd", "quell_test"));

        AtomicReference<Double> reducedDamage = new AtomicReference<>();
        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.MAGIC);
        when(event.getDamage()).thenReturn(10.0);
        doAnswer(inv -> {
            reducedDamage.set(inv.getArgument(0));
            return null;
        }).when(event).setDamage(anyDouble());

        for (int i = 0; i < 30 && reducedDamage.get() == null; i++) {
            listener.onDamage(event);
        }

        assertNotNull(reducedDamage.get(), "Quell should eventually trigger and scale damage");
        assertTrue(reducedDamage.get() < 10.0, "Scaled damage should be lower than original");
    }

    @Test
    void dispelEventuallyRemovesPositiveEffect() throws Exception {
        PlayerMock attacker = server.addPlayer("dispel_attacker");
        PlayerMock victim = server.addPlayer("dispel_victim");

        ItemStack sword = new ItemStack(org.bukkit.Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        attacker.getInventory().setItemInMainHand(sword);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 1));

        DispelListener listener = new DispelListener();
        DispelEnchant config = Mockito.mock(DispelEnchant.class);
        when(config.getCooldownTicks()).thenReturn(0);
        when(config.getTriggerChance()).thenReturn(1.0); // capped to 0.95 in listener

        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", new NamespacedKey("enchadd", "dispel_test"));

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);

        for (int i = 0; i < 20 && victim.hasPotionEffect(PotionEffectType.SPEED); i++) {
            listener.onHit(event);
        }

        assertTrue(!victim.hasPotionEffect(PotionEffectType.SPEED), "Dispel should eventually remove SPEED effect");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
