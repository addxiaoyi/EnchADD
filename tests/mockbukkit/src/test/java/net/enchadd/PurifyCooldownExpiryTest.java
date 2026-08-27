package net.enchadd;

import net.enchadd.enchants.PurifyEnchant;
import net.enchadd.listeners.PurifyListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PurifyCooldownExpiryTest {

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
    void purifyTriggersAgainAfterCooldownExpires() throws Exception {
        PlayerMock player = server.addPlayer("purify_cd_player");
        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setHelmet(helmet);

        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 60, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 60, 0));

        PurifyListener listener = new PurifyListener();
        PurifyEnchant config = Mockito.mock(PurifyEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getTriggerChance()).thenReturn(1.0);

        NamespacedKey key = new NamespacedKey("enchadd", "purify_cd_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        PlayerItemConsumeEvent event = new PlayerItemConsumeEvent(player, new ItemStack(Material.APPLE), EquipmentSlot.HAND);

        listener.onConsume(event);
        assertTrue(!player.hasPotionEffect(PotionEffectType.POISON) || !player.hasPotionEffect(PotionEffectType.WITHER),
                "Purify should remove at least one negative effect on first trigger");

        Long stored = player.getPersistentDataContainer().get(key, PersistentDataType.LONG);
        assertTrue(stored != null && stored > 0L, "Purify should store a cooldown timestamp in PDC");

        boolean beforePoison = player.hasPotionEffect(PotionEffectType.POISON);
        boolean beforeWither = player.hasPotionEffect(PotionEffectType.WITHER);
        listener.onConsume(event);
        assertEquals(beforePoison, player.hasPotionEffect(PotionEffectType.POISON));
        assertEquals(beforeWither, player.hasPotionEffect(PotionEffectType.WITHER));

        player.getPersistentDataContainer().set(key, PersistentDataType.LONG,
                System.nanoTime() - (80L * 50L * 1_000_000L) - 10_000_000L);

        listener.onConsume(event);
        assertTrue(beforePoison != player.hasPotionEffect(PotionEffectType.POISON)
                        || beforeWither != player.hasPotionEffect(PotionEffectType.WITHER),
                "Purify should trigger again after cooldown expires");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
