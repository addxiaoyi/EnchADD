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
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantListInfoComposerTest {

    @Test
    void composeIncludesPlayerFriendlyMetadata() {
        List<Component> lines = EnchantListInfoComposer.compose(enchant("enchadd:beheading", "Alpha Guard", 3, 8, 5, "UNCOMMON", EquipmentSlotGroup.MAINHAND));

        assertEquals(15, lines.size());
        assertEquals("附魔信息", plain(lines.get(0)));
        assertEquals("名称: Alpha Guard", plain(lines.get(1)));
        assertEquals("键: enchadd:beheading", plain(lines.get(2)));
        assertEquals("最大等级: 3", plain(lines.get(3)));
        assertEquals("权重: 常见(8)", plain(lines.get(4)));
        assertEquals("稀有度: 不常见", plain(lines.get(5)));
        assertEquals("来源层级: 附魔台常见", plain(lines.get(6)));
        assertEquals("获取难度: 低", plain(lines.get(7)));
        assertEquals("宝藏: 否", plain(lines.get(8)));
        assertEquals("诅咒: 否", plain(lines.get(9)));
        assertTrue(plain(lines.get(10)).contains("战斗"));
        assertTrue(plain(lines.get(11)).contains("主手"));
        assertTrue(plain(lines.get(12)).startsWith("常见冲突:"));
        assertEquals("支持物品标签数: 0", plain(lines.get(13)));
        assertEquals("附魔标签数: 0", plain(lines.get(14)));
    }

    private static TestEnchant enchant(String key, String name, int maxLevel, int weight, int anvilCost, String rarity, EquipmentSlotGroup slot) {
        return new TestEnchant(Key.key(key), name, maxLevel, weight, anvilCost, rarity, slot);
    }

    private static String plain(Component component) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component);
    }

    private record TestEnchant(Key key, String name, int maxLevel, int weight, int anvilCost, String rarity, EquipmentSlotGroup slot) implements EnchADDEnchant {

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
            return anvilCost;
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
