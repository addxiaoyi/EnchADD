package net.enchadd;

import net.enchadd.utils.ItemStackSync;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ItemStackSync utility class.
 * Tests cover mergeInto, synchronizeAmount, splitAt, findSimilarSlots,
 * countSimilarItems, addToInventory, areCompatible, and calculateRemainingCapacity.
 */
class ItemStackSyncBehaviorTest {

    private ServerMock server;
    private PlayerMock player;
    private PlayerInventory inventory;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        inventory = player.getInventory();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // mergeInto tests

    @Test
    void mergeIntoWithNullSourceReturnsNull() {
        ItemStack target = new ItemStack(Material.DIAMOND, 10);
        ItemStack result = ItemStackSync.mergeInto(null, target);
        assertNull(result, "mergeInto should return null when source is null");
        assertEquals(10, target.getAmount(), "target amount should remain unchanged");
    }

    @Test
    void mergeIntoWithAirSourceReturnsNull() {
        ItemStack source = new ItemStack(Material.AIR);
        ItemStack target = new ItemStack(Material.DIAMOND, 10);
        ItemStack result = ItemStackSync.mergeInto(source, target);
        assertNull(result, "mergeInto should return null when source is air");
    }

    @Test
    void mergeIntoWithNullTargetReturnsSource() {
        ItemStack source = new ItemStack(Material.DIAMOND, 10);
        ItemStack target = new ItemStack(Material.DIAMOND, 5);
        ItemStack remaining = ItemStackSync.mergeInto(source, null);
        assertNotNull(remaining, "mergeInto should return source when target is null");
        assertEquals(10, remaining.getAmount(), "returned source should have original amount");
    }

    @Test
    void mergeIntoWithDifferentMaterialsReturnsSource() {
        ItemStack source = new ItemStack(Material.DIAMOND, 10);
        ItemStack target = new ItemStack(Material.GOLD_INGOT, 5);
        ItemStack remaining = ItemStackSync.mergeInto(source, target);
        assertNotNull(remaining, "mergeInto should return source when materials differ");
        assertEquals(Material.DIAMOND, remaining.getType(), "returned item should be diamond");
        assertEquals(10, remaining.getAmount(), "source amount should remain unchanged");
    }

    @Test
    void mergeIntoWithSimilarItemsMergesCompletely() {
        ItemStack source = new ItemStack(Material.DIAMOND, 5);
        ItemStack target = new ItemStack(Material.DIAMOND, 10);
        ItemStack remaining = ItemStackSync.mergeInto(source, target);
        assertNull(remaining, "mergeInto should return null when fully merged");
        assertEquals(15, target.getAmount(), "target should have combined amount");
    }

    @Test
    void mergeIntoPartialMergeReturnsRemainder() {
        ItemStack source = new ItemStack(Material.DIAMOND, 32);
        ItemStack target = new ItemStack(Material.DIAMOND, 50);
        ItemStack remaining = ItemStackSync.mergeInto(source, target);
        assertNotNull(remaining, "mergeInto should return remaining items");
        assertEquals(64, target.getAmount(), "target should be at max stack size");
        assertEquals(18, remaining.getAmount(), "remaining should be 18 (82 - 64)");
    }

    @Test
    void mergeIntoWithMaxStackLimit() {
        ItemStack source = new ItemStack(Material.DIAMOND, 64);
        ItemStack target = new ItemStack(Material.DIAMOND, 64);
        ItemStack remaining = ItemStackSync.mergeInto(source, target);
        assertNotNull(remaining, "mergeInto should return source when both at max");
        assertEquals(64, target.getAmount(), "target should remain at max");
        assertEquals(64, remaining.getAmount(), "remaining should be full source");
    }

    // synchronizeAmount tests

    @Test
    void synchronizeAmountReducesExcessAmount() {
        ItemStack stack = new ItemStack(Material.DIAMOND, 128);
        ItemStackSync.synchronizeAmount(stack);
        assertEquals(64, stack.getAmount(), "amount should be capped at max stack size");
    }

    @Test
    void synchronizeAmountWithNegativeAmountSetsToZero() {
        ItemStack stack = new ItemStack(Material.DIAMOND, 10);
        stack.setAmount(-5);
        ItemStackSync.synchronizeAmount(stack);
        assertEquals(0, stack.getAmount(), "negative amount should become zero");
    }

    @Test
    void synchronizeAmountAirItemReturnsUnchanged() {
        ItemStack stack = new ItemStack(Material.AIR, 64);
        ItemStackSync.synchronizeAmount(stack);
        assertEquals(64, stack.getAmount(), "air item should remain unchanged");
    }

    @Test
    void synchronizeAmountValidAmountUnchanged() {
        ItemStack stack = new ItemStack(Material.DIAMOND, 32);
        ItemStackSync.synchronizeAmount(stack);
        assertEquals(32, stack.getAmount(), "valid amount should remain unchanged");
    }

    // splitAt tests

    @Test
    void splitAtWithAirReturnsOriginal() {
        ItemStack stack = new ItemStack(Material.AIR, 10);
        ItemStack[] result = ItemStackSync.splitAt(stack, 5);
        assertNotNull(result, "splitAt should return non-null array");
        assertEquals(10, result[0].getAmount(), "first part should be air with 10");
        assertNull(result[1], "second part should be null");
    }

    @Test
    void splitAtAtZeroReturnsNullAndClone() {
        ItemStack stack = new ItemStack(Material.DIAMOND, 20);
        ItemStack[] result = ItemStackSync.splitAt(stack, 0);
        assertNull(result[0], "first part should be null");
        assertNotNull(result[1], "second part should not be null");
        assertEquals(20, result[1].getAmount(), "second part should have full amount");
    }

    @Test
    void splitAtAtEndReturnsCloneAndNull() {
        ItemStack stack = new ItemStack(Material.DIAMOND, 20);
        ItemStack[] result = ItemStackSync.splitAt(stack, 20);
        assertNotNull(result[0], "first part should not be null");
        assertEquals(20, result[0].getAmount(), "first part should have full amount");
        assertNull(result[1], "second part should be null");
    }

    @Test
    void splitAtNormalSplit() {
        ItemStack stack = new ItemStack(Material.DIAMOND, 20);
        ItemStack[] result = ItemStackSync.splitAt(stack, 7);
        assertEquals(7, result[0].getAmount(), "first part should have 7");
        assertEquals(13, result[1].getAmount(), "second part should have 13");
    }

    // findSimilarSlots tests

    @Test
    void findSimilarSlotsWithNullItemReturnsEmpty() {
        List<Integer> slots = ItemStackSync.findSimilarSlots(inventory, null);
        assertTrue(slots.isEmpty(), "should return empty list for null item");
    }

    @Test
    void findSimilarSlotsWithAirReturnsEmpty() {
        List<Integer> slots = ItemStackSync.findSimilarSlots(inventory, new ItemStack(Material.AIR));
        assertTrue(slots.isEmpty(), "should return empty list for air item");
    }

    @Test
    void findSimilarSlotsFindsMatchingSlots() {
        inventory.setItem(0, new ItemStack(Material.DIAMOND, 10));
        inventory.setItem(5, new ItemStack(Material.DIAMOND, 5));
        inventory.setItem(10, new ItemStack(Material.GOLD_INGOT, 10));

        List<Integer> slots = ItemStackSync.findSimilarSlots(inventory, new ItemStack(Material.DIAMOND, 1));
        assertEquals(2, slots.size(), "should find 2 slots with diamond");
        assertTrue(slots.contains(0), "should contain slot 0");
        assertTrue(slots.contains(5), "should contain slot 5");
        assertFalse(slots.contains(10), "should not contain slot 10 with gold");
    }

    // countSimilarItems tests

    @Test
    void countSimilarItemsWithNullReturnsZero() {
        int count = ItemStackSync.countSimilarItems(inventory, null);
        assertEquals(0, count, "should return 0 for null item");
    }

    @Test
    void countSimilarItemsCountsAllSimilar() {
        inventory.setItem(0, new ItemStack(Material.DIAMOND, 10));
        inventory.setItem(5, new ItemStack(Material.DIAMOND, 32));
        inventory.setItem(8, new ItemStack(Material.GOLD_INGOT, 10));

        int count = ItemStackSync.countSimilarItems(inventory, new ItemStack(Material.DIAMOND, 1));
        assertEquals(42, count, "should count all diamonds");
    }

    // areCompatible tests

    @Test
    void areCompatibleWithNullReturnsFalse() {
        assertFalse(ItemStackSync.areCompatible(null, new ItemStack(Material.DIAMOND)));
        assertFalse(ItemStackSync.areCompatible(new ItemStack(Material.DIAMOND), null));
        assertFalse(ItemStackSync.areCompatible(null, null));
    }

    @Test
    void areCompatibleWithAirReturnsFalse() {
        assertFalse(ItemStackSync.areCompatible(new ItemStack(Material.AIR), new ItemStack(Material.DIAMOND)));
        assertFalse(ItemStackSync.areCompatible(new ItemStack(Material.DIAMOND), new ItemStack(Material.AIR)));
    }

    @Test
    void areCompatibleWithSameMaterialReturnsTrue() {
        assertTrue(ItemStackSync.areCompatible(
                new ItemStack(Material.DIAMOND, 10),
                new ItemStack(Material.DIAMOND, 20)
        ));
    }

    @Test
    void areCompatibleWithDifferentMaterialReturnsFalse() {
        assertFalse(ItemStackSync.areCompatible(
                new ItemStack(Material.DIAMOND, 10),
                new ItemStack(Material.GOLD_INGOT, 20)
        ));
    }

    // calculateRemainingCapacity tests

    @Test
    void calculateRemainingCapacityWithNullReturnsZero() {
        int capacity = ItemStackSync.calculateRemainingCapacity(inventory, null);
        assertEquals(0, capacity, "should return 0 for null item");
    }

    @Test
    void calculateRemainingCapacityWithAirReturnsZero() {
        int capacity = ItemStackSync.calculateRemainingCapacity(inventory, new ItemStack(Material.AIR));
        assertEquals(0, capacity, "should return 0 for air item");
    }

    @Test
    void calculateRemainingCapacityWithEmptyInventoryReturnsMaxStacks() {
        int capacity = ItemStackSync.calculateRemainingCapacity(inventory, new ItemStack(Material.DIAMOND, 1));
        assertEquals(36 * 64, capacity, "empty inventory should have full capacity for 36 slots");
    }

    @Test
    void calculateRemainingCapacityAccountsForExistingItems() {
        inventory.setItem(0, new ItemStack(Material.DIAMOND, 32));
        inventory.setItem(1, new ItemStack(Material.DIAMOND, 10));

        int capacity = ItemStackSync.calculateRemainingCapacity(inventory, new ItemStack(Material.DIAMOND, 1));
        assertEquals((36 * 64) - 42, capacity, "should subtract existing diamond amounts");
    }

    // addToInventory tests

    @Test
    void addToInventoryWithNullReturnsNull() {
        ItemStack remaining = ItemStackSync.addToInventory(inventory, null);
        assertNull(remaining, "should return null for null item");
    }

    @Test
    void addToInventoryWithAirReturnsNull() {
        ItemStack remaining = ItemStackSync.addToInventory(inventory, new ItemStack(Material.AIR));
        assertNull(remaining, "should return null for air item");
    }

    @Test
    void addToInventoryToEmptyInventoryAddsCompletely() {
        ItemStack item = new ItemStack(Material.DIAMOND, 64);
        ItemStack remaining = ItemStackSync.addToInventory(inventory, item);
        assertNull(remaining, "item should be fully added to empty inventory");
        assertEquals(64, inventory.getItem(0).getAmount(), "first slot should have 64 diamonds");
    }

    @Test
    void addToInventoryMergesWithExistingStacks() {
        inventory.setItem(0, new ItemStack(Material.DIAMOND, 32));
        inventory.setItem(1, new ItemStack(Material.DIAMOND, 10));

        ItemStack item = new ItemStack(Material.DIAMOND, 30);
        ItemStack remaining = ItemStackSync.addToInventory(inventory, item);

        assertNull(remaining, "item should be fully added when merging");
        assertEquals(64, inventory.getItem(0).getAmount(), "first slot should be at max");
        assertEquals(8, inventory.getItem(1).getAmount(), "second slot should have 8 remaining");
    }

    @Test
    void addToInventoryReturnsRemainingWhenFull() {
        // Fill most of the inventory
        for (int i = 0; i < 35; i++) {
            inventory.setItem(i, new ItemStack(Material.GOLD_INGOT, 64));
        }

        ItemStack item = new ItemStack(Material.DIAMOND, 128);
        ItemStack remaining = ItemStackSync.addToInventory(inventory, item);

        assertNotNull(remaining, "should return remaining items");
        assertEquals(64, remaining.getAmount(), "64 should remain (one full stack used, 64 left over)");
    }

    // Edge cases

    @Test
    void mergeIntoWithOneEmptyStack() {
        ItemStack source = new ItemStack(Material.DIAMOND, 20);
        ItemStack target = new ItemStack(Material.AIR, 0);
        ItemStack remaining = ItemStackSync.mergeInto(source, target);
        assertNull(remaining, "source should be absorbed into empty target");
        assertEquals(20, target.getAmount(), "target should now have 20");
    }

    @Test
    void splitAtWithOneItem() {
        ItemStack stack = new ItemStack(Material.DIAMOND, 1);
        ItemStack[] result = ItemStackSync.splitAt(stack, 1);
        assertEquals(1, result[0].getAmount(), "first part should have 1");
        assertNull(result[1], "second part should be null");
    }
}
