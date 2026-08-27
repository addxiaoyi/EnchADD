package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.key.Key;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BalanceTeamValidatorTest {

    @AfterEach
    void resetConfigState() {
        EnchADDConfig.ENCHANTS.clear();
    }

    @Test
    void missingOrEmptyTeamsAreBlocked() {
        assertTrue(BalanceTeamValidator.hasMissingOrConflict(null));
        assertTrue(BalanceTeamValidator.hasMissingOrConflict(new Key[0]));
        assertTrue(BalanceTeamValidator.hasMissingOrConflict(new Key[] {Key.key("enchadd:missing")}));
    }

    @Test
    void validTeamWithoutConflictsPasses() {
        Key first = Key.key("enchadd:first");
        Key second = Key.key("enchadd:second");
        EnchADDConfig.ENCHANTS.put(first, new TestEnchant(first));
        EnchADDConfig.ENCHANTS.put(second, new TestEnchant(second));

        assertFalse(BalanceTeamValidator.hasMissingOrConflict(new Key[] {first, second}));
    }

    private record TestEnchant(Key key) implements EnchADDEnchant {

        @Override
        public Key getKey() {
            return key;
        }

        @Override
        public Component getDescriptionComponent() {
            return Component.text(key.asString());
        }

        @Override
        public String getDescriptionText() {
            return key.asString();
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
