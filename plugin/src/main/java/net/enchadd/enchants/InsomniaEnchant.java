package net.enchadd.enchants;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class InsomniaEnchant extends AbstractEnchADDEnchant {

    public static final Key KEY = Key.key("enchadd:insomnia_curse");

    
    
    
    
    
    private final Set<EquipmentSlotGroup> activeSlots = new HashSet<>();
    @Override
    public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
        return Collections.unmodifiableSet(this.activeSlots);
    }

    public InsomniaEnchant(int anvilCost,
            int weight,
            EnchantmentRegistryEntry.EnchantmentCost minimumCost,
            EnchantmentRegistryEntry.EnchantmentCost maximumCost,
            Collection<TagKey<Enchantment>> enchantTagKeys,
            Collection<TagEntry<ItemType>> supportedItemTags,
            Collection<EquipmentSlotGroup> activeSlots, boolean enabled, String rarity) {
        super(KEY, anvilCost, 1, weight, minimumCost, maximumCost, enchantTagKeys, supportedItemTags, enabled, rarity);
        this.activeSlots.addAll(activeSlots);
    }

    

    

    

    

    

    

    

    

    public static InsomniaEnchant create(ConfigurationSection configurationSection) {
        boolean enabled = EnchADDConfig.getBoolean(configurationSection, "enabled", true);
        String rarity = configurationSection.getString("rarity", "COMMON");
InsomniaEnchant insomniaEnchant = new InsomniaEnchant(EnchADDConfig.getInt(configurationSection, "anvilCost", 1),
                EnchADDConfig.getInt(configurationSection, "weight", 2),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "minimumCost.base", 0),
                        EnchADDConfig.getInt(configurationSection, "minimumCost.additionalPerLevel", 3)
                ),
                EnchantmentRegistryEntry.EnchantmentCost.of(
                        EnchADDConfig.getInt(configurationSection, "maximumCost.base", 30),
                        EnchADDConfig.getInt(configurationSection, "maximumCost.additionalPerLevel", 1)
                ),
                EnchADDConfig.getEnchantmentTagKeysFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "enchantmentTags",
                        List.of("#in_enchanting_table", "#curse")
                )),
                EnchADDConfig.getItemTagEntriesFromList(EnchADDConfig.getStringList(
                        configurationSection,
                        "supportedItemTags",
                        List.of(
                                "#minecraft:enchantable/armor"
                        )
                )),
                EnchADDConfig.getEquipmentSlotGroups(EnchADDConfig.getStringList(
                        configurationSection,
                        "activeSlots",
                        List.of(
                                "ARMOR"
                        )
                )), enabled, rarity);

        if (enabled) {
            net.enchadd.EnchADDConfig.ENCHANTS.put(InsomniaEnchant.KEY, insomniaEnchant);
    }

        return insomniaEnchant;
    }

}
