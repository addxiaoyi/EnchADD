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

class BalanceScoreEstimatorTest {

    @Test
    void estimateBurstScoreReturnsZeroForNullEnchant() {
        assertEquals(0.0, BalanceScoreEstimator.estimateBurstScore(null), 0.0001);
    }

    @Test
    void estimateBurstScoreFallsBackToMaxLevelScaling() {
        assertEquals(3.0, BalanceScoreEstimator.estimateBurstScore(enchant(5)), 0.0001);
        assertEquals(0.4, BalanceScoreEstimator.estimateBurstScore(enchant(0)), 0.0001);
    }

    @Test
    void estimateTeamBurstScoreReturnsZeroForNullTeam() {
        assertEquals(0.0, BalanceScoreEstimator.estimateTeamBurstScore(null), 0.0001);
    }

    private static TestEnchant enchant(int maxLevel) {
        return new TestEnchant(maxLevel);
    }

    private record TestEnchant(int maxLevel) implements EnchADDEnchant {

        @Override
        public Key getKey() {
            return Key.key("enchadd:test");
        }

        @Override
        public Component getDescriptionComponent() {
            return Component.text("Test");
        }

        @Override
        public String getDescriptionText() {
            return "Test";
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
