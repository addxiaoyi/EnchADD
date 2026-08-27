package net.enchadd;

import net.enchadd.utils.LegacyEnchantSanitizer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyEnchantSanitizerBehaviorTest {

    @AfterEach
    void tearDown() {
        TestPluginSupport.unregisterAllHandlers();
        MockBukkit.unmock();
    }

    @Test
    void sanitizerMigratesLegacyAliasToCurrentCurseKey() {
        @SuppressWarnings("unchecked")
        org.bukkit.Registry<Enchantment> registry = Mockito.mock(org.bukkit.Registry.class);
        Enchantment legacy = Mockito.mock(Enchantment.class);
        Enchantment current = Mockito.mock(Enchantment.class);
        ItemStack item = Mockito.mock(ItemStack.class);
        ItemMeta meta = Mockito.mock(ItemMeta.class);

        Mockito.when(legacy.getKey()).thenReturn(new NamespacedKey("enchadd", "panic"));
        Mockito.when(current.getKey()).thenReturn(new NamespacedKey("enchadd", "panic_curse"));
        Mockito.when(registry.get(net.kyori.adventure.key.Key.key("enchadd:panic"))).thenReturn(legacy);
        Mockito.when(registry.get(net.kyori.adventure.key.Key.key("enchadd:panic_curse"))).thenReturn(current);
        Mockito.when(item.getType()).thenReturn(Material.IRON_HELMET);
        Mockito.when(item.getEnchantments()).thenReturn(Map.of(legacy, 1));
        Mockito.when(item.getItemMeta()).thenReturn(meta);
        Mockito.when(meta.removeEnchant(legacy)).thenReturn(true);
        Mockito.when(meta.addEnchant(current, 1, true)).thenReturn(true);

        try (MockedStatic<net.enchadd.utils.EnchantCache> cache = Mockito.mockStatic(net.enchadd.utils.EnchantCache.class)) {
            boolean changed = LegacyEnchantSanitizer.sanitize(registry, item);

            assertTrue(changed);
            Mockito.verify(meta).removeEnchant(legacy);
            Mockito.verify(meta).addEnchant(current, 1, true);
            Mockito.verify(item).setItemMeta(meta);
            cache.verify(() -> net.enchadd.utils.EnchantCache.invalidate(item));
        }
    }

    @Test
    void sanitizerRemovesDeletedLegacyEnchantWithoutMigrationTarget() {
        @SuppressWarnings("unchecked")
        org.bukkit.Registry<Enchantment> registry = Mockito.mock(org.bukkit.Registry.class);
        Enchantment removed = Mockito.mock(Enchantment.class);
        ItemStack item = Mockito.mock(ItemStack.class);
        ItemMeta meta = Mockito.mock(ItemMeta.class);

        Mockito.when(removed.getKey()).thenReturn(new NamespacedKey("enchadd", "cloaking"));
        Mockito.when(registry.get(net.kyori.adventure.key.Key.key("enchadd:cloaking"))).thenReturn(removed);
        Mockito.when(item.getType()).thenReturn(Material.IRON_HELMET);
        Mockito.when(item.getEnchantments()).thenReturn(Map.of(removed, 1));
        Mockito.when(item.getItemMeta()).thenReturn(meta);
        Mockito.when(meta.removeEnchant(removed)).thenReturn(true);

        try (MockedStatic<net.enchadd.utils.EnchantCache> cache = Mockito.mockStatic(net.enchadd.utils.EnchantCache.class)) {
            boolean changed = LegacyEnchantSanitizer.sanitize(registry, item);

            assertTrue(changed);
            Mockito.verify(meta).removeEnchant(removed);
            Mockito.verify(item).setItemMeta(meta);
            cache.verify(() -> net.enchadd.utils.EnchantCache.invalidate(item));
        }
    }
}
