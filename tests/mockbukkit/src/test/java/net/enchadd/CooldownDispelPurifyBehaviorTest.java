package net.enchadd;

import net.enchadd.enchants.DispelEnchant;
import net.enchadd.enchants.PurifyEnchant;
import net.enchadd.listeners.DispelListener;
import net.enchadd.listeners.PurifyListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CooldownDispelPurifyBehaviorTest {

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
    void dispelSecondHitIsBlockedByCooldown() throws Exception {
        PlayerMock attacker = server.addPlayer("dispel_cd_attacker");
        PlayerMock victim = server.addPlayer("dispel_cd_victim");

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        attacker.getInventory().setItemInMainHand(sword);

        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 0));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 60, 0));
        assertTrue(victim.hasPotionEffect(PotionEffectType.SPEED));
        assertTrue(victim.hasPotionEffect(PotionEffectType.REGENERATION));

        DispelListener listener = new DispelListener();
        DispelEnchant config = Mockito.mock(DispelEnchant.class);
        when(config.getCooldownTicks()).thenReturn(200); // ensure cooldown window
        when(config.getTriggerChance()).thenReturn(1.0);

        NamespacedKey key = new NamespacedKey("enchadd", "dispel_cd_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);

        try (MockedStatic<PerformanceUtils> mocked = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);

            // First hit should remove exactly one positive effect and set cooldown
            listener.onHit(event);
            boolean removedOne = victim.getActivePotionEffects().size() < 2;
            assertTrue(removedOne, "First dispel hit should remove one positive effect");

            // Second hit immediately should be blocked by cooldown and not remove the other
            listener.onHit(event);
            int remaining = 0;
            if (victim.hasPotionEffect(PotionEffectType.SPEED)) remaining++;
            if (victim.hasPotionEffect(PotionEffectType.REGENERATION)) remaining++;
            assertEquals(1, remaining, "Cooldown should prevent dispelling the second effect immediately");
        }
    }

    @Test
    void dispelTriggersAgainAfterCooldownExpires() throws Exception {
        PlayerMock attacker = server.addPlayer("dispel_expiry_attacker");
        PlayerMock victim = server.addPlayer("dispel_expiry_victim");

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        attacker.getInventory().setItemInMainHand(sword);

        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 0));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 60, 0));

        DispelListener listener = new DispelListener();
        DispelEnchant config = Mockito.mock(DispelEnchant.class);
        when(config.getCooldownTicks()).thenReturn(200);
        when(config.getTriggerChance()).thenReturn(1.0);

        NamespacedKey key = new NamespacedKey("enchadd", "dispel_expiry_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getEntity()).thenReturn(victim);

        try (MockedStatic<PerformanceUtils> mocked = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);

            listener.onHit(event);
            int remainingAfterFirst = 0;
            if (victim.hasPotionEffect(PotionEffectType.SPEED)) remainingAfterFirst++;
            if (victim.hasPotionEffect(PotionEffectType.REGENERATION)) remainingAfterFirst++;
            assertTrue(remainingAfterFirst < 2, "First dispel hit should remove one positive effect");

            Long stored = attacker.getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.LONG);
            assertTrue(stored != null && stored > 0L, "Dispel should store a cooldown timestamp in PDC");

            attacker.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.LONG,
                    System.nanoTime() - (200L * 50L * 1_000_000L) - 10_000_000L);

            listener.onHit(event);
            assertTrue(!victim.hasPotionEffect(PotionEffectType.SPEED) || !victim.hasPotionEffect(PotionEffectType.REGENERATION),
                    "After cooldown expiry, Dispel should be able to remove the remaining effect");
        }
    }

    @Test
    void purifySecondConsumeIsBlockedByCooldown() throws Exception {
        PlayerMock player = server.addPlayer("purify_cd_player");

        ItemStack helmet = new ItemStack(Material.DIAMOND_HELMET);
        helmet.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        player.getInventory().setHelmet(helmet);

        player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 60, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 20 * 60, 0));
        assertTrue(player.hasPotionEffect(PotionEffectType.POISON));
        assertTrue(player.hasPotionEffect(PotionEffectType.WITHER));

        PurifyListener listener = new PurifyListener();
        PurifyEnchant config = Mockito.mock(PurifyEnchant.class);
        when(config.getCooldownTicks()).thenReturn(200);
        when(config.getTriggerChance()).thenReturn(1.0);

        NamespacedKey key = new NamespacedKey("enchadd", "purify_cd_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        server.getPluginManager().registerEvents(listener, TestPluginSupport.enabledPlugin(server, "purify-cooldown"));

        PlayerItemConsumeEvent consume = new PlayerItemConsumeEvent(player, new ItemStack(Material.APPLE), EquipmentSlot.HAND);
        server.getPluginManager().callEvent(consume);

        boolean removedOne = !(player.hasPotionEffect(PotionEffectType.POISON) && player.hasPotionEffect(PotionEffectType.WITHER));
        assertTrue(removedOne, "First purify consume should remove one negative effect");

        // second consume immediately should be blocked by cooldown
        PlayerItemConsumeEvent consume2 = new PlayerItemConsumeEvent(player, new ItemStack(Material.APPLE), EquipmentSlot.HAND);
        server.getPluginManager().callEvent(consume2);

        int remaining = 0;
        if (player.hasPotionEffect(PotionEffectType.POISON)) remaining++;
        if (player.hasPotionEffect(PotionEffectType.WITHER)) remaining++;
        assertEquals(1, remaining, "Cooldown should prevent purifying the second effect immediately");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

