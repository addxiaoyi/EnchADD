package net.enchadd.listeners.support;

import net.enchadd.utils.LegacyEnchantReport;
import net.enchadd.utils.LegacyEnchantSanitizer;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class LegacyEnchantSanitizerSupport {

    private final Registry<Enchantment> registry;

    public LegacyEnchantSanitizerSupport(@NotNull Registry<Enchantment> registry) {
        this.registry = registry;
    }

    /**
     * Guard check for grindstone single-slot exploit prevention.
     * Returns true if the grindstone is safe to process (both slots filled),
     * false if only one or no slots are filled (potential exploit attempt).
     *
     * @param grindstone the grindstone inventory to check
     * @return true if both slots have items (safe), false otherwise (exploit attempt)
     */
    public boolean addGuardCheck(@NotNull GrindstoneInventory grindstone) {
        ItemStack[] contents = grindstone.getContents();
        boolean slot0HasItem = contents.length > 0 && contents[0] != null && !contents[0].getType().isAir();
        boolean slot1HasItem = contents.length > 1 && contents[1] != null && !contents[1].getType().isAir();
        return slot0HasItem && slot1HasItem;
    }

    /**
     * Guard check for anvil single-slot exploit prevention.
     * Returns true if the anvil is safe to process (both input slots filled),
     * false if only one or no slots are filled (potential exploit attempt).
     *
     * @param anvil the anvil inventory to check
     * @return true if both input slots have items (safe), false otherwise (exploit attempt)
     */
    public boolean addGuardCheck(@NotNull AnvilInventory anvil) {
        ItemStack left = anvil.getFirstItem();
        ItemStack right = anvil.getSecondItem();
        boolean leftHasItem = left != null && !left.getType().isAir();
        boolean rightHasItem = right != null && !right.getType().isAir();
        return leftHasItem && rightHasItem;
    }

    /**
     * Guard check for smithing single-slot exploit prevention.
     * Returns true if the smithing table is safe to process (both slots filled),
     * false if only one or no slots are filled (potential exploit attempt).
     *
     * @param smithing the smithing inventory to check
     * @return true if both slots have items (safe), false otherwise (exploit attempt)
     */
    public boolean addGuardCheck(@NotNull SmithingInventory smithing) {
        ItemStack[] contents = smithing.getContents();
        boolean slot0HasItem = contents.length > 0 && contents[0] != null && !contents[0].getType().isAir();
        boolean slot1HasItem = contents.length > 1 && contents[1] != null && !contents[1].getType().isAir();
        return slot0HasItem && slot1HasItem;
    }

    public void sanitizeInventory(@Nullable Player player, @NotNull String container, @Nullable Inventory inventory) {
        if (inventory == null) {
            return;
        }
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (sanitizeItem(player, container, slot, item)) {
                inventory.setItem(slot, item);
            }
        }
    }

    public boolean sanitizeItem(@Nullable Player player, @NotNull String container, int slot, @Nullable ItemStack item) {
        List<LegacyEnchantReport.LegacyEnchantHit> hits = LegacyEnchantReport.scanItem(registry, container, slot, item);
        boolean changed = LegacyEnchantSanitizer.sanitize(registry, item);
        if (changed && !hits.isEmpty()) {
            logHits(player, hits);
        }
        return changed;
    }

    private void logHits(@Nullable Player player, @NotNull List<LegacyEnchantReport.LegacyEnchantHit> hits) {
        String actor = player == null ? "unknown" : player.getName();
        for (LegacyEnchantReport.LegacyEnchantHit hit : hits) {
            Bukkit.getLogger().warning(String.format(
                    "[EnchADD-Legacy] player=%s container=%s slot=%d material=%s legacy=%s target=%s level=%d",
                    actor,
                    hit.container(),
                    hit.slot(),
                    hit.material(),
                    hit.legacyKey(),
                    hit.migrationTarget(),
                    hit.level()
            ));
        }
    }
}
