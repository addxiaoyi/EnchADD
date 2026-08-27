package net.enchadd.commands;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantListSearchSupportTest {

    @Test
    void findByNameMatchesCaseInsensitivelyAndSortsByName() {
        TestEnchant zeta = enchant("enchadd:zeta", "Zeta Guard");
        TestEnchant alpha = enchant("enchadd:alpha", "Alpha Guard");
        TestEnchant ignored = enchant("enchadd:ignored", "Swift Strike");

        List<EnchADDEnchant> found = EnchantListSearchSupport.findByName(List.of(zeta, ignored, alpha), "guard");

        assertEquals(List.of(alpha, zeta), found);
    }

    @Test
    void prepareListResultsFiltersSortsAndAppliesKeywords() {
        TestEnchant alpha = enchant("enchadd:alpha", "Alpha Strike", 3, 8, EquipmentSlotGroup.MAINHAND);
        TestEnchant beta = enchant("enchadd:beta", "Beta Guard", 1, 3, EquipmentSlotGroup.CHEST);
        TestEnchant gamma = enchant("other:gamma", "Gamma Strike", 5, 9, EquipmentSlotGroup.MAINHAND);

        EnchantListOptions options = new EnchantListOptions();
        options.namespace = "enchadd";
        options.slot = EquipmentSlotGroup.MAINHAND;
        options.minWeight = 5;
        options.sort = "weight";
        options.order = "desc";
        options.keywords = List.of("strike");

        List<EnchADDEnchant> found = EnchantListSearchSupport.prepareListResults(List.of(beta, gamma, alpha), options);

        assertEquals(List.of(alpha), found);
    }

    @Test
    void prepareListResultsCanFilterBySourceTier() {
        TestEnchant tableCommon = enchant("enchadd:telepathy", "Telepathy", 1, 5, EquipmentSlotGroup.MAINHAND);
        TestEnchant treasureOnly = enchant("enchadd:soulbound", "Soulbound", 1, 1, EquipmentSlotGroup.ANY);
        TestEnchant curse = enchant("enchadd:vampirism", "Vampirism", 1, 1, EquipmentSlotGroup.ANY);

        EnchantListOptions options = new EnchantListOptions();
        options.sourceTier = EnchantListDisplayMetadataSupport.SourceTier.TREASURE_ONLY;

        List<EnchADDEnchant> found = EnchantListSearchSupport.prepareListResults(List.of(tableCommon, curse, treasureOnly), options);

        assertEquals(List.of(treasureOnly), found);
    }

    private static TestEnchant enchant(String key, String name) {
        return enchant(key, name, 1, 1, EquipmentSlotGroup.ANY);
    }

    private static TestEnchant enchant(String key, String name, int maxLevel, int weight, EquipmentSlotGroup slot) {
        return new TestEnchant(Key.key(key), name, maxLevel, weight, slot);
    }

    private record TestEnchant(Key key, String name, int maxLevel, int weight, EquipmentSlotGroup slot) implements EnchADDEnchant {

        @Override
        public Key getKey() {
            return key;
        }

        @Override
        public Component getDescriptionComponent() {
            return Component.text(name);
        }

        @Override
        public String getDescriptionText() {
            return name;
        }

        @Override
        public int getAnvilCost() {
            return 1;
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
        public EnchantmentRegistryEntry.EnchantmentCost getMinimumCost() {
            return EnchantmentRegistryEntry.EnchantmentCost.of(1, 1);
        }

        @Override
        public EnchantmentRegistryEntry.EnchantmentCost getMaximumCost() {
            return EnchantmentRegistryEntry.EnchantmentCost.of(1, 1);
        }

        @Override
        public Iterable<EquipmentSlotGroup> getActiveSlots() {
            return List.of(slot);
        }

        @Override
        public Set<TagEntry<ItemType>> getSupportedItems() {
            return Set.of();
        }

        @Override
        public Set<TagKey<Enchantment>> getEnchantTagKeys() {
            return Set.of();
        }
    }
}
