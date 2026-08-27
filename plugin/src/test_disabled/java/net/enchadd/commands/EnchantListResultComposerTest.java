package net.enchadd.commands;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantListResultComposerTest {

    @Test
    void composeFindResultsIncludesHeaderAndFormattedRows() {
        List<String> lines = EnchantListResultComposer.composeFindResults(List.of(enchant("enchadd:beheading", "Alpha Guard", 3, 8, "UNCOMMON", EquipmentSlotGroup.MAINHAND))).stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .toList();

        assertEquals("搜索结果", lines.get(0));
        assertTrue(lines.get(1).contains("Alpha Guard"));
        assertTrue(lines.get(1).contains("主手"));
        assertTrue(lines.get(1).contains("常见(8)"));
        assertTrue(lines.get(1).contains("附魔台常见"));
    }

    @Test
    void composeListResultsIncludesAdjustmentPageAndHighlightedRows() {
        EnchantListOptions options = new EnchantListOptions();
        options.keywords = List.of("guard");

        List<String> lines = EnchantListResultComposer.composeListResults(
                        List.of(
                                enchant("enchadd:beheading", "Alpha Guard", 3, 8, "UNCOMMON", EquipmentSlotGroup.MAINHAND),
                                enchant("enchadd:barrier", "Beta Strike", 1, 3, "COMMON", EquipmentSlotGroup.OFFHAND)
                        ),
                        1,
                        1,
                        true,
                        options
                ).stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .toList();

        assertEquals("附魔百科", lines.get(0));
        assertEquals("当前筛选: 关键词 guard · 结果数: 2", lines.get(1));
        assertEquals("页码或每页数量已调整", lines.get(2));
        assertEquals("页码 1/2", lines.get(3));
        assertTrue(lines.get(4).contains("Alpha Guard"));
        assertTrue(lines.get(4).contains("主手"));
        assertTrue(lines.get(4).contains("常见(8)"));
        assertTrue(lines.get(4).contains("附魔台常见"));
    }

    @Test
    void composeListResultsShowsSourceTierFilterInSummary() {
        EnchantListOptions options = new EnchantListOptions();
        options.sourceTier = EnchantListDisplayMetadataSupport.SourceTier.TREASURE_ONLY;

        List<String> lines = EnchantListResultComposer.composeListResults(
                        List.of(enchant("enchadd:soulbound", "Soulbound", 1, 1, "TREASURE", EquipmentSlotGroup.ANY)),
                        1,
                        10,
                        false,
                        options
                ).stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .toList();

        assertTrue(lines.get(1).contains("来源层级"));
        assertTrue(lines.get(1).contains("宝藏专属"));
    }

    @Test
    void composeListAndFindResultsShowEmptyStates() {
        List<String> listLines = EnchantListResultComposer.composeListResults(List.of(), 1, 10, false, new EnchantListOptions()).stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .toList();
        List<String> findLines = EnchantListResultComposer.composeFindResults(List.of()).stream()
                .map(PlainTextComponentSerializer.plainText()::serialize)
                .toList();

        assertEquals("附魔百科", listLines.get(0));
        assertEquals("当前筛选: 无 · 结果数: 0", listLines.get(1));
        assertEquals("没有符合条件的附魔", listLines.get(2));
        assertTrue(listLines.stream().anyMatch(line -> line.contains("sort=weight order=asc slot=MAINHAND")));
        assertTrue(listLines.stream().anyMatch(line -> line.contains("去掉 keywords")));
        assertEquals("搜索结果", findLines.get(0));
        assertEquals("没有找到符合条件的附魔", findLines.get(1));
        assertTrue(findLines.stream().anyMatch(line -> line.contains("更短的关键词")));
        assertTrue(findLines.stream().anyMatch(line -> line.contains("list 浏览")));
    }

    private static TestEnchant enchant(String key, String name, int maxLevel, int weight, String rarity, EquipmentSlotGroup slot) {
        return new TestEnchant(Key.key(key), name, maxLevel, weight, rarity, slot);
    }

    private record TestEnchant(Key key, String name, int maxLevel, int weight, String rarity, EquipmentSlotGroup slot) implements EnchADDEnchant {

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
        public String getRarity() {
            return rarity;
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
