package net.enchadd;

import net.enchadd.utils.EnchantCache;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("UnstableApiUsage")
class CombatSlotBoundaryMathTest {

    private static final int ITERATIONS = 400;

    @Test
    void highestAndSumRespectActiveSlotGroupsAcrossRandomBoundaryScenarios() {
        for (int i = 0; i < ITERATIONS; i++) {
            EnchantCache.clear();

            Set<EquipmentSlotGroup> activeGroups = randomActiveGroups();
            Enchantment enchantment = mock(Enchantment.class);
            when(enchantment.getActiveSlotGroups()).thenReturn(activeGroups);

            SlotState boots = randomSlotState(enchantment);
            SlotState legs = randomSlotState(enchantment);
            SlotState chest = randomSlotState(enchantment);
            SlotState head = randomSlotState(enchantment);
            SlotState main = randomSlotState(enchantment);
            SlotState off = randomSlotState(enchantment);

            EntityEquipment equipment = mock(EntityEquipment.class);
            when(equipment.getBoots()).thenReturn(boots.item);
            when(equipment.getLeggings()).thenReturn(legs.item);
            when(equipment.getChestplate()).thenReturn(chest.item);
            when(equipment.getHelmet()).thenReturn(head.item);
            when(equipment.getItemInMainHand()).thenReturn(main.item);
            when(equipment.getItemInOffHand()).thenReturn(off.item);

            int expectedSum = 0;
            int expectedHighest = 0;

            int bootsContribution = contribution(boots.level, isArmorSlotEnabled(activeGroups, EquipmentSlotGroup.FEET));
            expectedSum += bootsContribution;
            expectedHighest = Math.max(expectedHighest, bootsContribution);

            int legsContribution = contribution(legs.level, isArmorSlotEnabled(activeGroups, EquipmentSlotGroup.LEGS));
            expectedSum += legsContribution;
            expectedHighest = Math.max(expectedHighest, legsContribution);

            int chestContribution = contribution(chest.level, isArmorSlotEnabled(activeGroups, EquipmentSlotGroup.CHEST));
            expectedSum += chestContribution;
            expectedHighest = Math.max(expectedHighest, chestContribution);

            int headContribution = contribution(head.level, isArmorSlotEnabled(activeGroups, EquipmentSlotGroup.HEAD));
            expectedSum += headContribution;
            expectedHighest = Math.max(expectedHighest, headContribution);

            int mainContribution = contribution(main.level, isHandSlotEnabled(activeGroups, EquipmentSlotGroup.MAINHAND));
            expectedSum += mainContribution;
            expectedHighest = Math.max(expectedHighest, mainContribution);

            int offContribution = contribution(off.level, isHandSlotEnabled(activeGroups, EquipmentSlotGroup.OFFHAND));
            expectedSum += offContribution;
            expectedHighest = Math.max(expectedHighest, offContribution);

            assertEquals(expectedHighest, EnchADD.getHighestEnchantLevel(equipment, enchantment));
            assertEquals(expectedSum, EnchADD.getSumOfEnchantLevels(equipment, enchantment));
        }
    }

    private static SlotState randomSlotState(Enchantment enchantment) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextDouble() < 0.25) {
            return new SlotState(null, 0);
        }
        int level = random.nextInt(0, 7);
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(Material.DIAMOND_SWORD);
        when(item.getEnchantmentLevel(enchantment)).thenReturn(level);
        return new SlotState(item, level);
    }

    private static Set<EquipmentSlotGroup> randomActiveGroups() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        EquipmentSlotGroup[] candidates = {
                EquipmentSlotGroup.ANY,
                EquipmentSlotGroup.ARMOR,
                EquipmentSlotGroup.HAND,
                EquipmentSlotGroup.HEAD,
                EquipmentSlotGroup.CHEST,
                EquipmentSlotGroup.LEGS,
                EquipmentSlotGroup.FEET,
                EquipmentSlotGroup.MAINHAND,
                EquipmentSlotGroup.OFFHAND
        };
        Set<EquipmentSlotGroup> groups = new HashSet<>();
        for (EquipmentSlotGroup candidate : candidates) {
            if (random.nextBoolean()) {
                groups.add(candidate);
            }
        }
        if (groups.isEmpty()) {
            groups.add(candidates[random.nextInt(candidates.length)]);
        }
        return groups;
    }

    private static boolean isArmorSlotEnabled(Set<EquipmentSlotGroup> groups, EquipmentSlotGroup slot) {
        return groups.contains(EquipmentSlotGroup.ANY)
                || groups.contains(EquipmentSlotGroup.ARMOR)
                || groups.contains(slot);
    }

    private static boolean isHandSlotEnabled(Set<EquipmentSlotGroup> groups, EquipmentSlotGroup slot) {
        return groups.contains(EquipmentSlotGroup.ANY)
                || groups.contains(EquipmentSlotGroup.HAND)
                || groups.contains(slot);
    }

    private static int contribution(int level, boolean enabled) {
        return enabled ? level : 0;
    }

    private record SlotState(ItemStack item, int level) {
    }
}
