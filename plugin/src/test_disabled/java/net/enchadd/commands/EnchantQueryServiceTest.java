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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;

class EnchantQueryServiceTest {

    @Test
    void findEnchantByArgMatchesFullAndShortKeyIgnoringCase() {
        TestEnchant alpha = enchant("enchadd:alpha", "Alpha", 2, 5, EquipmentSlotGroup.MAINHAND);
        Map<Key, EnchADDEnchant> enchants = new LinkedHashMap<>();
        enchants.put(alpha.key(), alpha);

        assertSame(alpha, EnchantQueryService.findEnchantByArg(enchants, "enchadd:alpha"));
        assertSame(alpha, EnchantQueryService.findEnchantByArg(enchants, "ALPHA"));
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
