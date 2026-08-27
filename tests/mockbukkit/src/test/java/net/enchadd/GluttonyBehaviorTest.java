package net.enchadd;

import net.enchadd.enchants.GluttonyEnchant;
import net.enchadd.listeners.GluttonyListener;
import net.enchadd.utils.EnchantCache;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GluttonyBehaviorTest {

    @Test
    void gluttonySanitizesClickedItemsDownToTheCurseOnly() throws Exception {
        GluttonyListener listener = new GluttonyListener();
        setField(listener, "enchant", Enchantment.UNBREAKING);

        ItemStack item = Mockito.mock(ItemStack.class);
        ItemMeta meta = Mockito.mock(ItemMeta.class);
        when(item.getType()).thenReturn(Material.DIAMOND_PICKAXE);
        when(item.getEnchantments()).thenReturn(Map.of(
                Enchantment.UNBREAKING, 1,
                Enchantment.SHARPNESS, 3,
                Enchantment.LOOTING, 2
        ));
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.removeEnchant(Enchantment.SHARPNESS)).thenReturn(true);
        when(meta.removeEnchant(Enchantment.LOOTING)).thenReturn(true);

        InventoryClickEvent event = Mockito.mock(InventoryClickEvent.class);
        when(event.getCurrentItem()).thenReturn(item);
        when(event.getCursor()).thenReturn(null);

        try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
            listener.onInventoryClick(event);
            verify(meta).removeEnchant(Enchantment.SHARPNESS);
            verify(meta).removeEnchant(Enchantment.LOOTING);
            verify(meta, never()).removeEnchant(Enchantment.UNBREAKING);
            verify(item).setItemMeta(meta);
            verify(event).setCurrentItem(item);
            cache.verify(() -> EnchantCache.invalidate(item));
        }
    }

    @Test
    void gluttonyTakesOverEnchantTableOutputAndPurgesExistingEnchants() throws Exception {
        GluttonyListener listener = new GluttonyListener();
        setField(listener, "enchant", Enchantment.UNBREAKING);

        ItemStack item = Mockito.mock(ItemStack.class);
        ItemMeta meta = Mockito.mock(ItemMeta.class);
        when(item.getType()).thenReturn(Material.DIAMOND_SWORD);
        when(item.getEnchantments()).thenReturn(Map.of(
                Enchantment.SHARPNESS, 4,
                Enchantment.LOOTING, 3
        ));
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.removeEnchant(Enchantment.SHARPNESS)).thenReturn(true);
        when(meta.removeEnchant(Enchantment.LOOTING)).thenReturn(true);
        when(item.getEnchantmentLevel(Enchantment.UNBREAKING)).thenReturn(0);

        Map<Enchantment, Integer> toAdd = new LinkedHashMap<>();
        toAdd.put(Enchantment.UNBREAKING, 1);
        toAdd.put(Enchantment.SHARPNESS, 4);
        toAdd.put(Enchantment.LOOTING, 3);

        EnchantItemEvent event = Mockito.mock(EnchantItemEvent.class);
        when(event.getItem()).thenReturn(item);
        when(event.getEnchantsToAdd()).thenReturn(toAdd);

        try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
            listener.onEnchantItem(event);
            assertEquals(1, toAdd.size());
            assertEquals(1, toAdd.get(Enchantment.UNBREAKING));
            verify(meta).removeEnchant(Enchantment.SHARPNESS);
            verify(meta).removeEnchant(Enchantment.LOOTING);
            verify(item).setItemMeta(meta);
            cache.verify(() -> EnchantCache.invalidate(item));
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
