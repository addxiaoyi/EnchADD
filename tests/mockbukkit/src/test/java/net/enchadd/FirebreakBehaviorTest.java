package net.enchadd;

import net.enchadd.enchants.FirebreakEnchant;
import net.enchadd.listeners.FirebreakListener;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebreakBehaviorTest {

    @Test
    void firebreakShortensCombustionDurationOnChestplateWearer() throws Exception {
        ItemStack chestplate = Mockito.mock(ItemStack.class);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getChestplate()).thenReturn(chestplate);

        Player player = Mockito.mock(Player.class);

        FirebreakListener listener = new FirebreakListener();
        FirebreakEnchant config = Mockito.mock(FirebreakEnchant.class);
        when(config.getCombustionReductionPerLevel()).thenReturn(0.20);
        when(config.getMaxCombustionReduction()).thenReturn(0.60);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityCombustEvent event = Mockito.mock(EntityCombustEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getDuration()).thenReturn(100.0f);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(chestplate, Enchantment.UNBREAKING)).thenReturn(3);

            listener.onCombust(event);
            verify(event).setDuration(40.0f);
        }
    }

    @Test
    void firebreakReducesDirectFireDamageOnChestplateWearer() throws Exception {
        ItemStack chestplate = Mockito.mock(ItemStack.class);
        EntityEquipment equipment = Mockito.mock(EntityEquipment.class);
        when(equipment.getChestplate()).thenReturn(chestplate);

        Player player = Mockito.mock(Player.class);

        FirebreakListener listener = new FirebreakListener();
        FirebreakEnchant config = Mockito.mock(FirebreakEnchant.class);
        when(config.getDirectDamageReductionPerLevel()).thenReturn(0.08);
        when(config.getMaxDirectDamageReduction()).thenReturn(0.24);

        setField(listener, "enchant", Enchantment.UNBREAKING);
        setField(listener, "config", config);

        EntityDamageEvent event = Mockito.mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.FIRE_TICK);
        when(event.getDamage()).thenReturn(10.0);

        try (MockedStatic<PerformanceUtils> utils = Mockito.mockStatic(PerformanceUtils.class)) {
            utils.when(() -> PerformanceUtils.getEquipmentSafe(player)).thenReturn(equipment);
            utils.when(() -> PerformanceUtils.getEnchantLevel(chestplate, Enchantment.UNBREAKING)).thenReturn(2);

            listener.onFireDamage(event);
            verify(event).setDamage(8.4);
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
