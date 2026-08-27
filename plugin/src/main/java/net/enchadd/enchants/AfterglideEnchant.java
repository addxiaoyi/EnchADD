package net.enchadd.enchants;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class AfterglideEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:afterglide");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int slowFallingSecondsPerLevel;
    private final int slowFallingAmplifier;

    @SuppressWarnings("squid:S00107")
    private AfterglideEnchant(int anvilCost,
                              int weight,
                              EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                              EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                              Collection<TagKey<Enchantment>> enchantTagKeys,
                              Collection<TagEntry<ItemType>> supportedItemTags,
                              Collection<EquipmentSlotGroup> activeSlots,
                              int maxLevel,
                              int cooldownTicks,
                              int slowFallingSecondsPerLevel,
                              int slowFallingAmplifier,
                              boolean enabled,
                              String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.slowFallingSecondsPerLevel = slowFallingSecondsPerLevel;
        this.slowFallingAmplifier = slowFallingAmplifier;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getSlowFallingSecondsPerLevel() {
        return slowFallingSecondsPerLevel;
    }

    public int getSlowFallingAmplifier() {
        return slowFallingAmplifier;
    }

    public static AfterglideEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        AfterglideEnchant enchant = new AfterglideEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 2),
                EnchADDConfig.getInt(configurationSection, "weight", 5),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 22),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 46),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 2)
                ),
                EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "enchantmentTags",
                        List.of("#in_enchanting_table")
                )),
                EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "supportedItemTags",
                        List.of("minecraft:elytra")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("CHEST")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 3),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 100),
                EnchADDConfig.getInt(configurationSection, "slowFallingSecondsPerLevel", 2),
                EnchADDConfig.getInt(configurationSection, "slowFallingAmplifier", 0),
                enabled,
                rarity
        );
        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
