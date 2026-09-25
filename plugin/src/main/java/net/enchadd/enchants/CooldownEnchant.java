package net.enchadd.enchants;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Abstract base class for enchantments that have a cooldown.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class CooldownEnchant extends AbstractEnchADDEnchant {

    private static final int MAX_COOLDOWN_TICKS = 20 * 60 * 60;

    protected final int cooldownTicks;

    protected CooldownEnchant(
            @NotNull Key key,
            int anvilCost,
            int maxLevel,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            boolean enabled,
            String rarity,
            int cooldownTicks
    ) {
        super(key, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.cooldownTicks = Math.max(0, Math.min(MAX_COOLDOWN_TICKS, cooldownTicks));
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }
}
