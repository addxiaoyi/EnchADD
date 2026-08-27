package net.enchadd.commands;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.LegacyEnchantCompat;
import net.enchadd.utils.ItemEnchantSupport;
import net.enchadd.utils.LegacyEnchantSanitizer;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

final class VerifyBoundaryRules {

    private static final List<Material> ANVIL_TARGET_MATERIALS = List.of(
            Material.DIAMOND_SWORD,
            Material.DIAMOND_AXE,
            Material.DIAMOND_PICKAXE,
            Material.DIAMOND_SHOVEL,
            Material.DIAMOND_BOOTS,
            Material.DIAMOND_LEGGINGS,
            Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_HELMET,
            Material.BOW,
            Material.CROSSBOW,
            Material.TRIDENT,
            Material.FISHING_ROD,
            Material.ELYTRA,
            Material.SHIELD
    );
    private static final int ENCHANTING_TABLE_LEVEL = 30;
    private static final long ENCHANTING_TABLE_SEED = 0L;

    private VerifyBoundaryRules() {
    }

    static void run(VerifySupport.VerifyTally tally, Function<String, Boolean> translationReadyForVerify) {
        runCooldownChecks(tally);
        runBoundaryChecks(tally, translationReadyForVerify);
        runItemPathChecks(tally);
        runLegacyChecks(tally);
    }

    private static void runCooldownChecks(VerifySupport.VerifyTally tally) {
        ItemStack probeItem = new ItemStack(Material.STICK);
        ItemMeta probeMeta = probeItem.getItemMeta();
        if (probeMeta == null) {
            tally.cooldownChecks += 8;
            tally.failures.add("cooldown probe item meta unavailable");
            return;
        }
        PersistentDataContainer pdc = probeMeta.getPersistentDataContainer();
        NamespacedKey cooldownKey = PerformanceUtils.enchaddKey("verify_cooldown");
        NamespacedKey windowKey = PerformanceUtils.enchaddKey("verify_window");

        tally.cooldownChecks++;
        PerformanceUtils.setCooldown(pdc, cooldownKey);
        VerifySupport.passOrFail(PerformanceUtils.isOnCooldown(pdc, cooldownKey, 20), "cooldown active check failed", tally);

        tally.cooldownChecks++;
        long expiredNano = System.nanoTime() - (21L * 50L * 1_000_000L);
        pdc.set(cooldownKey, PersistentDataType.LONG, expiredNano);
        VerifySupport.passOrFail(!PerformanceUtils.isOnCooldown(pdc, cooldownKey, 20), "cooldown expiry check failed", tally);

        tally.cooldownChecks++;
        VerifySupport.passOrFail(PerformanceUtils.getRemainingCooldown(pdc, cooldownKey, 20) == 0L, "cooldown remaining after expiry check failed", tally);

        tally.cooldownChecks++;
        PerformanceUtils.setWindowUntilTicks(pdc, windowKey, 5);
        VerifySupport.passOrFail(PerformanceUtils.isWindowActive(pdc, windowKey), "window active check failed", tally);

        tally.cooldownChecks++;
        pdc.set(windowKey, PersistentDataType.LONG, System.nanoTime() - 1L);
        VerifySupport.passOrFail(!PerformanceUtils.isWindowActive(pdc, windowKey), "window expiry check failed", tally);

        tally.cooldownChecks++;
        VerifySupport.passOrFail(!PerformanceUtils.isOnCooldown(pdc, cooldownKey, 0), "zero cooldown should never block", tally);

        tally.cooldownChecks++;
        VerifySupport.passOrFail(PerformanceUtils.getRemainingCooldown(pdc, PerformanceUtils.enchaddKey("missing"), 20) == 0L, "missing cooldown key should report zero remaining", tally);

        tally.cooldownChecks++;
        PerformanceUtils.setWindowUntilSeconds(pdc, windowKey, 1);
        VerifySupport.passOrFail(PerformanceUtils.isWindowActive(pdc, windowKey), "window seconds setter check failed", tally);
    }

    private static void runBoundaryChecks(VerifySupport.VerifyTally tally, Function<String, Boolean> translationReadyForVerify) {
        tally.boundaryChecks++;
        VerifySupport.passOrFail(Math.abs(PerformanceUtils.safeDivide(4.0, 0.0, -1.0) - (-1.0)) < 1.0E-9, "safeDivide default fallback invalid", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(Math.abs(PerformanceUtils.safeDivide(9.0, 3.0, -1.0) - 3.0) < 1.0E-9, "safeDivide regular division invalid", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(PerformanceUtils.clamp(-3, 0, 5) == 0, "clamp int lower bound invalid", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(PerformanceUtils.clamp(9, 0, 5) == 5, "clamp int upper bound invalid", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(Math.abs(PerformanceUtils.clamp(-2.5, 0.0, 1.0) - 0.0) < 1.0E-9, "clamp double lower bound invalid", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(Math.abs(PerformanceUtils.clamp(3.5, 0.0, 1.0) - 1.0) < 1.0E-9, "clamp double upper bound invalid", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(PerformanceUtils.calculateDurationTicks(0, 0) == 20, "duration fallback lower bound invalid", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(PerformanceUtils.calculateDurationTicksPerLevel(0, 0) == 20, "duration per-level fallback lower bound invalid", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(!PerformanceUtils.rollChance(0.0) && PerformanceUtils.rollChance(1.0), "rollChance boundary behavior invalid", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(EnchADDConfig.ENCHANTS.size() >= 90, "registered enchant count below 90", tally);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(translationReadyForVerify.apply("airbag") && translationReadyForVerify.apply("brace"), "translation mapping not ready", tally);
    }

    // Validate the three player-facing acquisition paths against item NBT, not just registry metadata.
    private static void runItemPathChecks(VerifySupport.VerifyTally tally) {
        Registry<Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        if (enchantmentRegistry == null) {
            tally.itemChecks += 3;
            tally.failures.add("item path probe registry unavailable");
            return;
        }

        PathProbe probe = findPathProbe(enchantmentRegistry);
        if (probe == null) {
            tally.itemChecks += 3;
            tally.failures.add("item path probe missing compatible enchant");
            return;
        }

        tally.itemChecks++;
        VerifySupport.passOrFail(
                checkBookPath(probe.enchantment()),
                "book path failed to preserve stored enchant NBT",
                tally
        );

        tally.itemChecks++;
        VerifySupport.passOrFail(
                checkAnvilPath(probe),
                "anvil path failed to transfer stored enchant NBT",
                tally
        );

        tally.itemChecks++;
        VerifySupport.passOrFail(
                checkEnchantingTablePath(enchantmentRegistry),
                "enchanting table path failed to preserve enchant NBT",
                tally
        );
    }

    private static boolean checkBookPath(Enchantment enchantment) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        if (!ItemEnchantSupport.applyEnchant(book, enchantment, 1)) {
            return false;
        }
        ItemMeta meta = book.getItemMeta();
        return meta instanceof EnchantmentStorageMeta storageMeta
                && storageMeta.hasStoredEnchant(enchantment)
                && storageMeta.getStoredEnchantLevel(enchantment) == 1
                && ItemEnchantSupport.roundTripKeepsStoredEnchant(book, enchantment, 1);
    }

    private static boolean checkAnvilPath(PathProbe probe) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        if (!ItemEnchantSupport.applyEnchant(book, probe.enchantment(), 1)) {
            return false;
        }

        ItemStack result = new ItemStack(probe.targetMaterial());
        if (!transferStoredEnchant(book, result, probe.enchantment())) {
            return false;
        }
        return result.containsEnchantment(probe.enchantment())
                && result.getEnchantmentLevel(probe.enchantment()) == 1
                && ItemEnchantSupport.roundTripKeepsEnchant(result, probe.enchantment(), 1);
    }

    private static boolean transferStoredEnchant(ItemStack sourceBook, ItemStack targetItem, Enchantment enchantment) {
        ItemMeta meta = sourceBook.getItemMeta();
        if (!(meta instanceof EnchantmentStorageMeta storageMeta) || !storageMeta.hasStoredEnchant(enchantment)) {
            return false;
        }
        int level = storageMeta.getStoredEnchantLevel(enchantment);
        return level > 0 && ItemEnchantSupport.applyEnchant(targetItem, enchantment, level);
    }

    private static boolean checkEnchantingTablePath(Registry<Enchantment> enchantmentRegistry) {
        RegistryKeySet<Enchantment> tablePool = enchantmentRegistry.getTag(EnchantmentTagKeys.IN_ENCHANTING_TABLE);
        if (tablePool == null) {
            return false;
        }

        ItemStack enchanted = Bukkit.getItemFactory().enchantWithLevels(
                new ItemStack(Material.DIAMOND_SWORD),
                ENCHANTING_TABLE_LEVEL,
                tablePool,
                new Random(ENCHANTING_TABLE_SEED)
        );
        if (enchanted == null || enchanted.getEnchantments().isEmpty()) {
            return false;
        }

        Enchantment firstEnchant = enchanted.getEnchantments().keySet().iterator().next();
        Integer firstLevel = enchanted.getEnchantments().get(firstEnchant);
        return firstLevel != null
                && firstLevel > 0
                && ItemEnchantSupport.roundTripKeepsEnchant(enchanted, firstEnchant, firstLevel);
    }

    private static PathProbe findPathProbe(Registry<Enchantment> enchantmentRegistry) {
        for (EnchADDEnchant enchant : EnchADDConfig.ENCHANTS.values()) {
            Enchantment registeredEnchant = enchantmentRegistry.get(enchant.getKey());
            if (registeredEnchant == null) {
                continue;
            }
            for (Material targetMaterial : ANVIL_TARGET_MATERIALS) {
                if (registeredEnchant.canEnchantItem(new ItemStack(targetMaterial))) {
                    return new PathProbe(registeredEnchant, targetMaterial);
                }
            }
        }
        return null;
    }

    private static void runLegacyChecks(VerifySupport.VerifyTally tally) {
        Registry<org.bukkit.enchantments.Enchantment> enchantmentRegistry =
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        tally.boundaryChecks++;
        VerifySupport.passOrFail(
                enchantmentRegistry != null && LegacyEnchantCompat.legacyKeys().stream().allMatch(key -> enchantmentRegistry.get(key) != null),
                "legacy compatibility registry missing",
                tally
        );

        tally.boundaryChecks++;
        ItemStack legacyProbe = new ItemStack(Material.STICK);
        org.bukkit.enchantments.Enchantment legacyPanic = enchantmentRegistry == null ? null : enchantmentRegistry.get(net.kyori.adventure.key.Key.key("enchadd:panic"));
        org.bukkit.enchantments.Enchantment migratedPanic = enchantmentRegistry == null ? null : enchantmentRegistry.get(net.kyori.adventure.key.Key.key("enchadd:panic_curse"));
        if (legacyPanic != null && migratedPanic != null) {
            legacyProbe.addUnsafeEnchantment(legacyPanic, 1);
            boolean migrated = LegacyEnchantSanitizer.sanitize(enchantmentRegistry, legacyProbe)
                    && !legacyProbe.containsEnchantment(legacyPanic)
                    && legacyProbe.getEnchantmentLevel(migratedPanic) == 1;
            VerifySupport.passOrFail(migrated, "legacy migration sanitize failed", tally);
        } else {
            tally.failures.add("legacy migration probe setup missing");
        }
    }

    private record PathProbe(Enchantment enchantment, Material targetMaterial) {
    }
}
