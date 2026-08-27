package net.enchadd.utils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.legacy.LegacyEnchantDefinition;
import net.enchadd.legacy.LegacyEnchantDefinitions;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public final class LegacyEnchantReport {

    private LegacyEnchantReport() {
    }

    public record LegacyEnchantHit(
            String container,
            int slot,
            String material,
            String legacyKey,
            String migrationTarget,
            int level
    ) {
    }

    public static @NotNull List<LegacyEnchantHit> scanInventory(String container, Inventory inventory) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return scanInventory(registry, container, inventory);
    }

    public static @NotNull List<LegacyEnchantHit> scanInventory(@NotNull Registry<Enchantment> registry, String container, Inventory inventory) {
        List<LegacyEnchantHit> hits = new ArrayList<>();
        if (inventory == null) {
            return hits;
        }
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            hits.addAll(scanItem(registry, container, slot, contents[slot]));
        }
        return hits;
    }

    public static @NotNull List<LegacyEnchantHit> scanItem(String container, int slot, ItemStack item) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return scanItem(registry, container, slot, item);
    }

    public static @NotNull List<LegacyEnchantHit> scanItem(@NotNull Registry<Enchantment> registry, String container, int slot, ItemStack item) {
        List<LegacyEnchantHit> hits = new ArrayList<>();
        if (item == null || item.getType().isAir()) {
            return hits;
        }
        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (enchantments.isEmpty()) {
            return hits;
        }

        for (LegacyEnchantDefinition definition : LegacyEnchantDefinitions.entries()) {
            Key legacyKey = definition.key();
            Enchantment legacyEnchant = registry.get(legacyKey);
            if (legacyEnchant == null) {
                continue;
            }
            Integer level = enchantments.get(legacyEnchant);
            if (level == null || level <= 0) {
                continue;
            }
            hits.add(new LegacyEnchantHit(
                    container,
                    slot,
                    item.getType().key().asString(),
                    legacyKey.asString(),
                    definition.hasMigrationTarget() ? definition.migrationTarget().asString() : "-",
                    level
            ));
        }
        return hits;
    }

    public static @NotNull String summarize(Collection<LegacyEnchantHit> hits) {
        int count = hits == null ? 0 : hits.size();
        if (count == 0) {
            return "none";
        }
        long migrated = hits.stream().filter(hit -> !"-".equals(hit.migrationTarget())).count();
        long removed = count - migrated;
        return "hits=" + count + " migrate=" + migrated + " remove=" + removed;
    }
}
