package net.enchadd;

import net.enchadd.enchants.QuellEnchant;
import net.enchadd.listeners.QuellListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuellCooldownExpiryTest {

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
    void quellTriggersAgainAfterCooldownExpires() throws Exception {
        PlayerMock player = server.addPlayer("quell_cd_player");
        ItemStack chest = new ItemStack(Material.DIAMOND_CHESTPLATE);
        chest.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        player.getInventory().setChestplate(chest);

        QuellListener listener = new QuellListener();
        QuellEnchant config = Mockito.mock(QuellEnchant.class);
        when(config.getCooldownTicks()).thenReturn(80);
        when(config.getTriggerChance()).thenReturn(1.0);
        when(config.getReductionPerLevel()).thenReturn(0.2);

        NamespacedKey key = new NamespacedKey("enchadd", "quell_cd_test");
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "config", config);
        setField(listener, "key", key);

        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.MAGIC);
        when(event.getDamage()).thenReturn(10.0);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            utils.when(() -> PerformanceUtils.isOnCooldown(Mockito.any(), Mockito.any(), Mockito.anyInt())).thenReturn(false, true, false);
            utils.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(player.getInventory());
            utils.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(player.getPersistentDataContainer());
            utils.when(() -> PerformanceUtils.setCooldown(Mockito.any(), Mockito.any())).thenCallRealMethod();

            final int[] damageCalls = {0};
            final double[] lastDamage = {0.0};
            doAnswer(inv -> {
                damageCalls[0]++;
                lastDamage[0] = inv.getArgument(0);
                return null;
            }).when(event).setDamage(Mockito.anyDouble());

            listener.onDamage(event);
            assertTrue(damageCalls[0] == 1 && lastDamage[0] > 0.0, "Quell should scale damage on first trigger");

            Long stored = player.getPersistentDataContainer().get(key, PersistentDataType.LONG);
            assertTrue(stored != null && stored > 0L, "Quell should store a cooldown timestamp in PDC");

            listener.onDamage(event);
            assertTrue(damageCalls[0] == 1, "Cooldown should block immediate retrigger");

            player.getPersistentDataContainer().set(key, PersistentDataType.LONG,
                    System.nanoTime() - (80L * 50L * 1_000_000L) - 10_000_000L);

            listener.onDamage(event);
            assertTrue(damageCalls[0] >= 2 && lastDamage[0] > 0.0, "Quell should trigger again after cooldown expires");
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
