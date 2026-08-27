package net.enchadd;

import net.enchadd.enchants.DispelEnchant;
import net.enchadd.enchants.PurifyEnchant;
import net.enchadd.listeners.DispelListener;
import net.enchadd.listeners.PurifyListener;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CooldownPersistenceAndExpiryTest {

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
    void dispelWritesCooldownAndAllowsAfterExpiry() throws Exception {
        PlayerMock attacker = server.addPlayer("dispel_persist_attacker");
        PlayerMock victim = server.addPlayer("dispel_persist_victim");

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        attacker.getInventory().setItemInMainHand(sword);

        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 0));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 60, 0));

        DispelListener listener = new DispelListener();
        DispelEnchant config = Mockito.mock(DispelEnchant.class);
        int cooldownTicks = 200;
        when(config.getCooldownTicks()).thenReturn(cooldownTicks);
        when(config.getTriggerChance()).thenReturn(1.0);

        NamespacedKey key = new NamespacedKey("enchadd", "dispel_persist_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);

        // First: run until first success (chance is capped to 0.95)
        int guard = 0;
        while (victim.hasPotionEffect(PotionEffectType.SPEED) && victim.hasPotionEffect(PotionEffectType.REGENERATION) && guard++ < 50) {
            listener.onHit(event);
        }
        assertTrue(guard < 50, "Dispel should trigger within reasonable attempts");

        PersistentDataContainer pdc = attacker.getPersistentDataContainer();
        Long stamp = pdc.get(key, PersistentDataType.LONG);
        assertNotNull(stamp, "Dispel should write cooldown timestamp to PDC");

        // Second immediately: should be blocked by cooldown (remaining positive effect must stay)
        boolean beforeHasSpeed = victim.hasPotionEffect(PotionEffectType.SPEED);
        boolean beforeHasRegen = victim.hasPotionEffect(PotionEffectType.REGENERATION);
        listener.onHit(event);
        assertEquals(beforeHasSpeed, victim.hasPotionEffect(PotionEffectType.SPEED));
        assertEquals(beforeHasRegen, victim.hasPotionEffect(PotionEffectType.REGENERATION));

        // Expire cooldown by rewinding timestamp
        long cooldownNanos = cooldownTicks * 50L * 1_000_000L;
        pdc.set(key, PersistentDataType.LONG, System.nanoTime() - cooldownNanos - 10_000_000L);

        // Third: should be able to dispel the remaining effect
        int remainingBefore = (victim.hasPotionEffect(PotionEffectType.SPEED) ? 1 : 0) + (victim.hasPotionEffect(PotionEffectType.REGENERATION) ? 1 : 0);
        int guard2 = 0;
        while (remainingBefore == ((victim.hasPotionEffect(PotionEffectType.SPEED) ? 1 : 0) + (victim.hasPotionEffect(PotionEffectType.REGENERATION) ? 1 : 0)) && guard2++ < 50) {
            listener.onHit(event);
        }
        int remainingAfter = (victim.hasPotionEffect(PotionEffectType.SPEED) ? 1 : 0) + (victim.hasPotionEffect(PotionEffectType.REGENERATION) ? 1 : 0);
        assertTrue(remainingAfter < remainingBefore, "After cooldown expiry, dispel should be able to remove another effect");
    }

    @Test
    void purifyWritesCooldownAndAllowsAfterExpiry() throws Exception {
        PlayerMock player = server.addPlayer("purify_persist_player");

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setHelmet(helmet);

        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 60, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 60, 0));

        PurifyListener listener = new PurifyListener();
        PurifyEnchant config = Mockito.mock(PurifyEnchant.class);
        int cooldownTicks = 200;
        when(config.getCooldownTicks()).thenReturn(cooldownTicks);
        when(config.getTriggerChance()).thenReturn(1.0);

        NamespacedKey key = new NamespacedKey("enchadd", "purify_persist_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "purify-expiry"));

        // First: should remove one effect and write cooldown
        server.getPluginManager().callEvent(new PlayerItemConsumeEvent(player, new ItemStack(Material.APPLE), EquipmentSlot.HAND));
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        assertNotNull(pdc.get(key, PersistentDataType.LONG), "Purify should write cooldown timestamp to PDC");

        // Second immediately: should be blocked by cooldown (remaining negative must stay)
        boolean beforePoison = player.hasPotionEffect(PotionEffectType.POISON);
        boolean beforeWither = player.hasPotionEffect(PotionEffectType.WITHER);
        server.getPluginManager().callEvent(new PlayerItemConsumeEvent(player, new ItemStack(Material.APPLE), EquipmentSlot.HAND));
        assertEquals(beforePoison, player.hasPotionEffect(PotionEffectType.POISON));
        assertEquals(beforeWither, player.hasPotionEffect(PotionEffectType.WITHER));

        // Expire cooldown by rewinding timestamp
        long cooldownNanos = cooldownTicks * 50L * 1_000_000L;
        pdc.set(key, PersistentDataType.LONG, System.nanoTime() - cooldownNanos - 10_000_000L);

        // Third: should be able to remove the remaining effect
        int remainingBefore = (player.hasPotionEffect(PotionEffectType.POISON) ? 1 : 0) + (player.hasPotionEffect(PotionEffectType.WITHER) ? 1 : 0);
        server.getPluginManager().callEvent(new PlayerItemConsumeEvent(player, new ItemStack(Material.APPLE), EquipmentSlot.HAND));
        int remainingAfter = (player.hasPotionEffect(PotionEffectType.POISON) ? 1 : 0) + (player.hasPotionEffect(PotionEffectType.WITHER) ? 1 : 0);
        assertTrue(remainingAfter < remainingBefore, "After cooldown expiry, purify should be able to remove another effect");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

