package net.enchadd.enchants;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.utils.PerformanceUtils;
import net.enchadd.utils.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Suggestion 11: Base class to reduce redundancy across 70+ enchantments.
 * Suggestion 4: Centralized constants for lightweight object creation.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class AbstractEnchADDEnchant implements EnchADDEnchant {

    private static final int MAX_BALANCED_LEVEL = 5;
    private static final int MAX_BALANCED_WEIGHT = 30;
    private static final int MAX_BALANCED_ANVIL_COST = 10;

    protected final Key key;
    protected final int anvilCost;
    protected final int maxLevel;
    protected final int weight;
    protected final EnchantmentRegistryEntry.EnchantmentCost minimumCost;
    protected final EnchantmentRegistryEntry.EnchantmentCost maximumCost;
    protected final Set<TagEntry<ItemType>> supportedItemTags = new HashSet<>();
    protected final Set<TagKey<Enchantment>> enchantTagKeys = new HashSet<>();
    protected final boolean enabled;

    // Suggestion 16: Rarity/Quality support
    protected final String rarity;
    
    // Suggestion: Cache Bukkit NamespacedKey
    protected final NamespacedKey namespacedKey;

    protected AbstractEnchADDEnchant(
            @NotNull Key key,
            int anvilCost,
            int maxLevel,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            boolean enabled,
            String rarity
    ) {
        this.key = key;
        this.namespacedKey = PerformanceUtils.namespacedKey(key);
        // Keep malformed or extreme server configs from creating runaway power or loot rates.
        this.anvilCost = clamp(anvilCost, 1, MAX_BALANCED_ANVIL_COST);
        this.maxLevel = clamp(maxLevel, 1, MAX_BALANCED_LEVEL);
        this.weight = clamp(weight, 1, MAX_BALANCED_WEIGHT);
        this.minimumCost = minimumCost;
        this.maximumCost = maximumCost;
        this.enchantTagKeys.addAll(enchantTagKeys);
        this.supportedItemTags.addAll(supportedItemTags);
        this.enabled = enabled;
        this.rarity = rarity;
    }

    @Override
    public @NotNull Key getKey() {
        return key;
    }

    @Override
    public int getAnvilCost() {
        return anvilCost;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public EnchantmentRegistryEntry.@NotNull EnchantmentCost getMinimumCost() {
        return minimumCost;
    }

    @Override
    public EnchantmentRegistryEntry.@NotNull EnchantmentCost getMaximumCost() {
        return maximumCost;
    }

    @Override
    public @NotNull Set<TagEntry<ItemType>> getSupportedItems() {
        return Collections.unmodifiableSet(supportedItemTags);
    }

    @Override
    public @NotNull Set<TagKey<Enchantment>> getEnchantTagKeys() {
        return Collections.unmodifiableSet(enchantTagKeys);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRarity() {
        return rarity;
    }

    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    @NotNull
    protected String getMinecraftTranslationKey() {
        return "enchantment." + key.namespace() + "." + key.value();
    }

    @NotNull
    protected String getCustomTranslationKey() {
        return "EnchADD.enchant." + key.value();
    }

    @NotNull
    protected String getLegacyCustomTranslationKey() {
        String value = key.value();
        if (value.endsWith("_curse")) {
            value = value.substring(0, value.length() - "_curse".length());
        }
        return "EnchADD.enchant." + value;
    }

    @NotNull
    protected String resolveDescriptionFallback() {
        String fallback = LangManager.get(getLegacyCustomTranslationKey());
        String customResolved = LangManager.get(getCustomTranslationKey());
        if (!customResolved.equals(getCustomTranslationKey())) {
            fallback = customResolved;
        }
        return fallback;
    }

    @Override
    public @NotNull String getDescriptionText() {
        String minecraftKey = getMinecraftTranslationKey();
        String resolved = LangManager.get(minecraftKey);
        String fallback = resolveDescriptionFallback();
        if (!resolved.equals(minecraftKey)) {
            fallback = resolved;
        }
        return fallback;
    }

    @Override
    public @NotNull Component getDescriptionComponent() {
        // 直接下发可读文本，确保客户端无资源包也能稳定显示附魔名
        return Component.text(getDescriptionText());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
