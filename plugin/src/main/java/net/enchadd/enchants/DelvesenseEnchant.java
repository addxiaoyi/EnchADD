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
public final class DelvesenseEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:delvesense");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final double radius;
    private final int glowSeconds;
    private final int nightVisionSeconds;
    private final double maxActivationY;

    @SuppressWarnings("squid:S00107")
    private DelvesenseEnchant(int anvilCost,
                              int weight,
                              EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                              EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                              Collection<TagKey<Enchantment>> enchantTagKeys,
                              Collection<TagEntry<ItemType>> supportedItemTags,
                              Collection<EquipmentSlotGroup> activeSlots,
                              int maxLevel,
                              int cooldownTicks,
                              double radius,
                              int glowSeconds,
                              int nightVisionSeconds,
                              double maxActivationY,
                              boolean enabled,
                              String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.radius = Math.max(0.0, radius);
        this.glowSeconds = Math.max(0, glowSeconds);
        this.nightVisionSeconds = Math.max(0, nightVisionSeconds);
        this.maxActivationY = maxActivationY;
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public double getRadius() {
        return radius;
    }

    public int getGlowSeconds() {
        return glowSeconds;
    }

    public int getNightVisionSeconds() {
        return nightVisionSeconds;
    }

    public double getMaxActivationY() {
        return maxActivationY;
    }

    public static DelvesenseEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "RARE");
        EnchADDConfig.getBoolean(configurationSection, "canGetFromEnchantingTable", false);

        DelvesenseEnchant enchant = new DelvesenseEnchant(
                EnchADDConfig.getInt(configurationSection, "anvilCost", 3),
                EnchADDConfig.getInt(configurationSection, "weight", 1),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 18),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 2)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 42),
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
                        List.of("minecraft:compass")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 1),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 600),
                EnchADDConfig.getDouble(configurationSection, "radius", 10.0),
                EnchADDConfig.getInt(configurationSection, "glowSeconds", 6),
                EnchADDConfig.getInt(configurationSection, "nightVisionSeconds", 4),
                EnchADDConfig.getDouble(configurationSection, "maxActivationY", 56.0),
                enabled,
                rarity
        );

        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
