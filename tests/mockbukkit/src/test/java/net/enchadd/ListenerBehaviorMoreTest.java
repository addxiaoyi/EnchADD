package net.enchadd;

import net.enchadd.enchants.PurifyEnchant;
import net.enchadd.listeners.PurifyListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ListenerBehaviorMoreTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        TestPluginSupport.unregisterAllHandlers();
        MockBukkit.unmock();
    }

    @Test
    void purifyRemovesNegativeEffectOnConsume() throws Exception {
        PlayerMock player = server.addPlayer("purify_tester");

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setHelmet(helmet);

        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 60, 0));
        assertTrue(player.hasPotionEffect(PotionEffectType.POISON));

        PurifyListener listener = new PurifyListener();
        // keep constructor-initialized fields (negatives list), inject test doubles
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "key", new NamespacedKey("enchadd", "purify_test"));
        Object config = Mockito.mock(PurifyEnchant.class);
        when(((PurifyEnchant) config).getCooldownTicks()).thenReturn(0);
        when(((PurifyEnchant) config).getTriggerChance()).thenReturn(1.0);
        setField(listener, "config", config);

        // register and fire real event
        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "listener-more"));
        PlayerItemConsumeEvent event = new PlayerItemConsumeEvent(player, new ItemStack(Material.APPLE), EquipmentSlot.HAND);
        server.getPluginManager().callEvent(event);

        assertFalse(player.hasPotionEffect(PotionEffectType.POISON), "Purify should remove POISON");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

