package net.enchadd.listeners.support;

import net.enchadd.utils.EnchantCache;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GluttonySanitizerSupport {

    private final Enchantment enchant;

    public GluttonySanitizerSupport(@Nullable Enchantment enchant) {
        this.enchant = enchant;
    }

    /**
     * Guard check for anvil single-slot exploit prevention.
     * Returns true only if both anvil slots contain valid items.
     */
    public static boolean add_guard_check(@Nullable ItemStack left, @Nullable ItemStack right) {
        boolean leftHasItem = left != null && !left.getType().isAir();
        boolean rightHasItem = right != null && !right.getType().isAir();
        return leftHasItem && rightHasItem;
    }

    public boolean hasEnchant() {
        return enchant != null;
    }

    public boolean shouldSanitizeEnchantRoll(@Nullable ItemStack item,
                                      @NotNull Map<Enchantment, Integer> toAdd) {
        if (enchant == null || item == null || item.getType().isAir() || toAdd.isEmpty()) {
            return false;
        }
        boolean addsGluttony = toAdd.containsKey(enchant);
        boolean alreadyGluttonous = item.getEnchantmentLevel(enchant) > 0;
        return addsGluttony || alreadyGluttonous;
    }

    public void sanitizeEnchantRoll(@NotNull ItemStack item, @NotNull Map<Enchantment, Integer> toAdd) {
        if (enchant == null) {
            return;
        }
        boolean addsGluttony = toAdd.containsKey(enchant);
        toAdd.keySet().removeIf(other -> !other.equals(enchant));
        if (addsGluttony) {
            stripAllBut(item, enchant, false);
            return;
        }
        sanitizeIfGluttonous(item);
    }

    public boolean sanitizeIfGluttonous(@Nullable ItemStack item) {
        return stripAllBut(item, enchant, true);
    }

    public void sanitizeInventory(@Nullable Inventory inventory) {
        if (inventory == null) {
            return;
        }
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (sanitizeIfGluttonous(item)) {
                inventory.setItem(slot, item);
            }
        }
    }

    private boolean stripAllBut(@Nullable ItemStack item,
                                @Nullable Enchantment keeper,
                                boolean requireKeeperPresent) {
        if (item == null || keeper == null || item.getType().isAir()) {
            return false;
        }

        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (enchantments.isEmpty()) {
            return false;
        }
        if (requireKeeperPresent && !enchantments.containsKey(keeper)) {
            return false;
        }
        if (enchantments.size() == 1 && enchantments.containsKey(keeper)) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        List<Enchantment> toRemove = new ArrayList<>(enchantments.keySet());
        boolean changed = false;
        for (Enchantment enchantment : toRemove) {
            if (enchantment.equals(keeper)) {
                continue;
            }
            changed |= meta.removeEnchant(enchantment);
        }
        if (!changed) {
            return false;
        }

        item.setItemMeta(meta);
        EnchantCache.invalidate(item);
        return true;
    }
}
