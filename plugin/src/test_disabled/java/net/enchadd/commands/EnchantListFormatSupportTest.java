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

class EnchantListFormatSupportTest {

    @Test
    void resolveNameUsesEnchantDescriptionText() {
        assertEquals("Alpha Guard", EnchantListFormatSupport.resolveName(enchant("Alpha Guard")));
    }

    @Test
    void slotsToStringJoinsSlotNamesInEncounterOrder() {
        assertEquals("mainhand,offhand", EnchantListFormatSupport.slotsToString(List.of(
                EquipmentSlotGroup.MAINHAND,
                EquipmentSlotGroup.OFFHAND
        )));
    }

    private static TestEnchant enchant(String name) {
        return new TestEnchant(Key.key("enchadd:alpha"), name);
    }

    private record TestEnchant(Key key, String name) implements EnchADDEnchant {

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
            return 1;
        }

        @Override
        public int getWeight() {
            return 1;
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
            return List.of(EquipmentSlotGroup.ANY);
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
