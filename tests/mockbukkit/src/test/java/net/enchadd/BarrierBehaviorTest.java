package net.enchadd;

import net.enchadd.enchants.BarrierEnchant;
import net.enchadd.listeners.BarrierListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BarrierBehaviorTest {

    @Test
    void barrierWritesCooldownWhenShieldProcTriggers() throws Exception {
        BarrierListener listener = new BarrierListener();
        BarrierEnchant config = Mockito.mock(BarrierEnchant.class);
        when(config.getCooldownTicks()).thenReturn(200);
        when(config.getTriggerChance()).thenReturn(0.3);
        when(config.getMaxTriggerChance()).thenReturn(0.6);
        when(config.getKnockbackRadiusPerLevel()).thenReturn(2.0);

        NamespacedKey key = new NamespacedKey("enchadd", "barrier_test_key");
        setField(listener, "config", config);
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "key", key);

        Player player = Mockito.mock(Player.class);
        PlayerInventory inventory = Mockito.mock(PlayerInventory.class);
        ItemStack shield = Mockito.mock(ItemStack.class);
        when(shield.getType()).thenReturn(Material.SHIELD);
        when(inventory.getItemInOffHand()).thenReturn(shield);
        when(player.getInventory()).thenReturn(inventory);
        when(player.getHandRaisedTime()).thenReturn(6);

        World world = Mockito.mock(World.class);
        when(world.getNearbyEntities(Mockito.any(Location.class), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble()))
                .thenReturn(Collections.emptySet());
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(new Location(world, 0.0, 64.0, 0.0));

        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(player);

        PersistentDataContainer pdc = Mockito.mock(PersistentDataContainer.class);
        try (MockedStatic<PerformanceUtils> mocked = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> PerformanceUtils.isSuccessfulShieldBlock(player, event)).thenReturn(true);
            mocked.when(() -> PerformanceUtils.getEnchantLevel(shield, Enchantment.SHARPNESS)).thenReturn(2);
            mocked.when(() -> PerformanceUtils.getPDCSafe(player)).thenReturn(pdc);
            mocked.when(() -> PerformanceUtils.isOnCooldown(pdc, key, 200)).thenReturn(false);
            mocked.when(() -> PerformanceUtils.rollChance(Mockito.anyDouble())).thenReturn(true);

            listener.onShieldHit(event);

            mocked.verify(() -> PerformanceUtils.setCooldown(pdc, key));
        }
    }

    @Test
    void barrierDoesNothingWhenShieldGateFails() throws Exception {
        BarrierListener listener = new BarrierListener();
        BarrierEnchant config = Mockito.mock(BarrierEnchant.class);
        when(config.getCooldownTicks()).thenReturn(200);
        when(config.getTriggerChance()).thenReturn(0.3);
        when(config.getMaxTriggerChance()).thenReturn(0.6);
        when(config.getKnockbackRadiusPerLevel()).thenReturn(2.0);

        NamespacedKey key = new NamespacedKey("enchadd", "barrier_test_key");
        setField(listener, "config", config);
        setField(listener, "enchant", Enchantment.SHARPNESS);
        setField(listener, "key", key);

        Player player = Mockito.mock(Player.class);
        EntityDamageByEntityEvent event = Mockito.mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(player);

        try (MockedStatic<PerformanceUtils> mocked = Mockito.mockStatic(PerformanceUtils.class, Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> PerformanceUtils.isSuccessfulShieldBlock(player, event)).thenReturn(false);

            listener.onShieldHit(event);

            mocked.verify(() -> PerformanceUtils.setCooldown(Mockito.any(), Mockito.any()), never());
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
