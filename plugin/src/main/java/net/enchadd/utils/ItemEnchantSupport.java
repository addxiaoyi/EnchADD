package net.enchadd.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemEnchantSupport {

    private ItemEnchantSupport() {
    }

    public static boolean applyEnchant(@Nullable ItemStack item,
                                       @Nullable Enchantment enchantment,
                                       int level) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !applyEnchant(meta, enchantment, level)) {
            return false;
        }

        item.setItemMeta(meta);
        EnchantCache.invalidate(item);
        return true;
    }

    public static boolean applyEnchant(@Nullable ItemMeta meta,
                                       @Nullable Enchantment enchantment,
                                       int level) {
        if (meta == null || enchantment == null || level <= 0) {
            return false;
        }

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return storageMeta.addStoredEnchant(enchantment, level, true);
        }
        return meta.addEnchant(enchantment, level, true);
    }

    public static boolean roundTripKeepsEnchant(@NotNull ItemStack item,
                                                @NotNull Enchantment enchantment,
                                                int expectedLevel) {
        ItemStack restored = ItemStack.deserialize(item.serialize());
        return restored.containsEnchantment(enchantment)
                && restored.getEnchantmentLevel(enchantment) == expectedLevel;
    }

    public static boolean roundTripKeepsStoredEnchant(@NotNull ItemStack book,
                                                      @NotNull Enchantment enchantment,
                                                      int expectedLevel) {
        ItemStack restored = ItemStack.deserialize(book.serialize());
        ItemMeta meta = restored.getItemMeta();
        if (!(meta instanceof EnchantmentStorageMeta storageMeta)) {
            return false;
        }
        return storageMeta.hasStoredEnchant(enchantment)
                && storageMeta.getStoredEnchantLevel(enchantment) == expectedLevel;
    }
}
