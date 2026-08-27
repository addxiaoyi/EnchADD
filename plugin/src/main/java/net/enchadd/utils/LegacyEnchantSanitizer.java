package net.enchadd.utils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.legacy.LegacyEnchantDefinition;
import net.enchadd.legacy.LegacyEnchantDefinitions;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public final class LegacyEnchantSanitizer {

    private LegacyEnchantSanitizer() {
    }

    public static boolean sanitize(@NotNull Registry<Enchantment> registry, ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        Map<Enchantment, Integer> enchantments = item.getEnchantments();
        if (enchantments.isEmpty()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        List<Enchantment> presentLegacyEnchants = new ArrayList<>();
        for (LegacyEnchantDefinition definition : LegacyEnchantDefinitions.entries()) {
            Enchantment legacyEnchant = registry.get(definition.key());
            if (legacyEnchant != null && enchantments.containsKey(legacyEnchant)) {
                presentLegacyEnchants.add(legacyEnchant);
            }
        }

        if (presentLegacyEnchants.isEmpty()) {
            return false;
        }

        boolean changed = false;
        for (Enchantment legacyEnchant : presentLegacyEnchants) {
            int legacyLevel = enchantments.getOrDefault(legacyEnchant, 0);
            changed |= meta.removeEnchant(legacyEnchant);

            org.bukkit.NamespacedKey namespacedKey = legacyEnchant.getKey();
            if (namespacedKey == null) {
                continue;
            }

            net.kyori.adventure.key.Key legacyKey = net.kyori.adventure.key.Key.key(namespacedKey.asString());
            LegacyEnchantDefinition definition = LegacyEnchantDefinitions.get(legacyKey);
            net.kyori.adventure.key.Key migrationTarget = definition == null ? null : definition.migrationTarget();
            if (migrationTarget == null || legacyLevel <= 0) {
                continue;
            }

            Enchantment migratedEnchant = registry.get(migrationTarget);
            if (migratedEnchant == null) {
                continue;
            }

            int currentLevel = enchantments.getOrDefault(migratedEnchant, 0);
            if (legacyLevel > currentLevel) {
                changed |= meta.addEnchant(migratedEnchant, legacyLevel, true);
            }
        }

        if (!changed) {
            return false;
        }

        item.setItemMeta(meta);
        EnchantCache.invalidate(item);
        return true;
    }

    public static boolean sanitize(ItemStack item) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return sanitize(registry, item);
    }
}
