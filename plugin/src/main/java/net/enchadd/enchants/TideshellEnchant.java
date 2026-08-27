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
public final class TideshellEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:tideshell");

    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final int waterBreathingSeconds;
    private final int dolphinsGraceSeconds;

    @SuppressWarnings("squid:S00107")
    private TideshellEnchant(int anvilCost,
                             int weight,
                             EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                             EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                             Collection<TagKey<Enchantment>> enchantTagKeys,
                             Collection<TagEntry<ItemType>> supportedItemTags,
                             Collection<EquipmentSlotGroup> activeSlots,
                             int maxLevel,
                             int cooldownTicks,
                             int waterBreathingSeconds,
                             int dolphinsGraceSeconds,
                             boolean enabled,
                             String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.waterBreathingSeconds = Math.max(0, waterBreathingSeconds);
        this.dolphinsGraceSeconds = Math.max(0, dolphinsGraceSeconds);
    }

    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public int getWaterBreathingSeconds() {
        return waterBreathingSeconds;
    }

    public int getDolphinsGraceSeconds() {
        return dolphinsGraceSeconds;
    }

    public static TideshellEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "RARE");
        EnchADDConfig.getBoolean(configurationSection, "canGetFromEnchantingTable", false);

        TideshellEnchant enchant = new TideshellEnchant(
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
                        List.of("minecraft:nautilus_shell")
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of("MAINHAND")
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 1),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 700),
                EnchADDConfig.getInt(configurationSection, "waterBreathingSeconds", 8),
                EnchADDConfig.getInt(configurationSection, "dolphinsGraceSeconds", 5),
                enabled,
                rarity
        );

        if (enabled) {
            EnchADDConfig.ENCHANTS.put(KEY, enchant);
        }
        return enchant;
    }
}
