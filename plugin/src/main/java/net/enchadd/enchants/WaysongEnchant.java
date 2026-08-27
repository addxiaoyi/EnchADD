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
public final class WaysongEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:waysong");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final double radius;
    private final int speedSeconds;
    private final int speedAmplifier;

    @SuppressWarnings("squid:S00107")
    private WaysongEnchant(int anvilCost,
                           int weight,
                           EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                           EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                           Collection<TagKey<Enchantment>> enchantTagKeys,
                           Collection<TagEntry<ItemType>> supportedItemTags,
                           Collection<EquipmentSlotGroup> activeSlots,
                           int maxLevel,
                           int cooldownTicks,
                           double radius,
                           int speedSeconds,
                           int speedAmplifier,
                           boolean enabled,
                           String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.radius = Math.max(0.0, radius);
        this.speedSeconds = Math.max(0, speedSeconds);
        this.speedAmplifier = Math.max(0, speedAmplifier);
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public double getRadius() {
        return radius;
    }

    public int getSpeedSeconds() {
        return speedSeconds;
    }

    public int getSpeedAmplifier() {
        return speedAmplifier;
    }

    public static WaysongEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "RARE");
        EnchADDConfig.getBoolean(configurationSection, "canGetFromEnchantingTable", false);

        WaysongEnchant enchant = new WaysongEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 3),
                EnchADDConfig.getInt(configurationSection, "weight", 1),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 20),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 44),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 2)
                ),
                EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "enchantmentTags",
                        List.of(
                                "#treasure",
                                "#on_random_loot",
                                "#tradeable",
                                "#double_trade_price",
                                "#trades/desert_special",
                                "#trades/jungle_special",
                                "#trades/plains_special",
                                "#trades/savanna_special",
                                "#trades/snow_special",
                                "#trades/swamp_special",
                                "#trades/taiga_special"
                        )
                )),
                EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "supportedItemTags",
                        List.of("minecraft:goat_horn")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 1),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 900),
                EnchADDConfig.getDouble(configurationSection, "radius", 8.0),
                EnchADDConfig.getInt(configurationSection, "speedSeconds", 6),
                EnchADDConfig.getInt(configurationSection, "speedAmplifier", 0),
                enabled,
                rarity
        );

        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
