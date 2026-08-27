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
 * Abstract base class for enchantments that have both a cooldown and a chance to trigger.
 */
@SuppressWarnings("UnstableApiUsage")
public abstract class ChanceEnchant extends CooldownEnchant {

    protected final double triggerChance;

    protected ChanceEnchant(
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
            int cooldownTicks,
            double triggerChance
    ) {
        super(key, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.triggerChance = triggerChance;
    }

    public double getTriggerChance() {
        return triggerChance;
    }
}
