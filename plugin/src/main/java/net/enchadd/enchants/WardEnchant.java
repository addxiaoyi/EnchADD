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

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class WardEnchant extends CooldownEnchant {

    public static final Key KEY = Key.key("enchadd:ward");

    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    private final Set<EquipmentSlotGroup> activeSlotsView = Collections.unmodifiableSet(activeSlots);
    private final String blockSound;

    private WardEnchant(int anvilCost,
                        int weight,
                        EnchantmentRegistryEntry.EnchantmentCost minimumCost,
                        EnchantmentRegistryEntry.EnchantmentCost maximumCost,
                        Collection<TagKey<Enchantment>> enchantTagKeys,
                        Collection<TagEntry<ItemType>> supportedItemTags,
                        Collection<EquipmentSlotGroup> activeSlots,
                        int maxLevel,
                        int cooldownTicks,
                        String blockSound,
                        boolean enabled,
                        String rarity) {
        super(KEY, anvilCost, maxLevel, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity, cooldownTicks);
        this.activeSlots.addAll(activeSlots);
        this.blockSound = blockSound;
    }
    
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return activeSlotsView;
    }

    public @NotNull String getBlockSound() {
        return blockSound;
    }

    public static WardEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
        WardEnchant wardEnchant = new WardEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 12),
                EnchADDConfig.getInt(configurationSection, "weight", 2),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 35),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 1)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 65),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 1)
                ),
                EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "enchantmentTags",
                        List.of("#in_enchanting_table")
                )),
                EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "supportedItemTags",
                        List.of(
                                "minecraft:shield"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of(
                                "OFFHAND"
                        )
                )),
                EnchADDConfig.getInt(configurationSection, "maxLevel", 1),
                EnchADDConfig.getInt(configurationSection, "cooldownTicks", 100),
                EnchADDConfig.getString(configurationSection, "blockSound", "minecraft:item.shield.block"), enabled, rarity);

        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(WardEnchant.KEY, wardEnchant);
        }

        return wardEnchant;
    }

}
