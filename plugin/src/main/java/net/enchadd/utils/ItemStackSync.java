package net.enchadd.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for synchronizing ItemStack operations in Minecraft inventories.
 * Provides methods for merging, splitting, and validating ItemStacks across stacks.
 */
public final class ItemStackSync {

    private ItemStackSync() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Attempts to merge source ItemStack into target ItemStack.
     * Returns the remaining amount that could not be merged.
     *
     * @param source the ItemStack to merge from
     * @param target the ItemStack to merge into
     * @return the remaining ItemStack that could not be merged, or null if fully merged
     */
    @Nullable
    public static ItemStack mergeInto(@Nullable ItemStack source, @Nullable ItemStack target) {
        if (source == null || source.getType().isAir()) {
            return null;
        }
        if (target == null || target.getType().isAir()) {
            return source;
        }

        if (!source.isSimilar(target)) {
            return source;
        }

        int maxStack = target.getMaxStackSize();
        int targetAmount = target.getAmount();
        int sourceAmount = source.getAmount();

        int totalAmount = targetAmount + sourceAmount;
        if (totalAmount <= maxStack) {
            target.setAmount(totalAmount);
            return null;
        } else {
            target.setAmount(maxStack);
            int remaining = totalAmount - maxStack;
            source.setAmount(remaining);
            return source;
        }
    }

    /**
     * Synchronizes the amount of an ItemStack to not exceed its maximum stack size.
     *
     * @param stack the ItemStack to synchronize
     * @return the synchronized ItemStack with valid amount
     */
    @NotNull
    public static ItemStack synchronizeAmount(@NotNull ItemStack stack) {
        if (stack.getType().isAir()) {
            return stack;
        }
        int maxStack = stack.getMaxStackSize();
        if (stack.getAmount() > maxStack) {
            stack.setAmount(maxStack);
        }
        if (stack.getAmount() < 0) {
            stack.setAmount(0);
        }
        return stack;
    }

    /**
     * Splits an ItemStack into two parts at the specified index.
     *
     * @param stack the ItemStack to split
     * @param splitIndex the index at which to split (first part size)
     * @return array of two ItemStacks, second may be null if splitIndex equals original amount
     */
    @NotNull
    public static ItemStack[] splitAt(@NotNull ItemStack stack, int splitIndex) {
        if (stack.getType().isAir()) {
            return new ItemStack[]{stack, null};
        }

        if (splitIndex <= 0) {
            return new ItemStack[]{null, stack.clone()};
        }

        if (splitIndex >= stack.getAmount()) {
            return new ItemStack[]{stack.clone(), null};
        }

        ItemStack first = stack.clone();
        first.setAmount(splitIndex);

        ItemStack second = stack.clone();
        second.setAmount(stack.getAmount() - splitIndex);

        return new ItemStack[]{first, second};
    }

    /**
     * Finds all slots in an inventory that contain a similar ItemStack.
     *
     * @param inventory the inventory to search
     * @param item the ItemStack to match against
     * @return list of slot indices containing similar items
     */
    @NotNull
    public static List<Integer> findSimilarSlots(@NotNull PlayerInventory inventory, @Nullable ItemStack item) {
        List<Integer> slots = new ArrayList<>();
        if (item == null || item.getType().isAir()) {
            return slots;
        }

        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack slot = contents[i];
            if (slot != null && slot.isSimilar(item)) {
                slots.add(i);
            }
        }
        return slots;
    }

    /**
     * Calculates the total amount of similar items in an inventory.
     *
     * @param inventory the inventory to search
     * @param item the ItemStack to match against
     * @return total amount of similar items
     */
    public static int countSimilarItems(@NotNull PlayerInventory inventory, @Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0;
        }

        int total = 0;
        for (ItemStack slot : inventory.getContents()) {
            if (slot != null && slot.isSimilar(item)) {
                total += slot.getAmount();
            }
        }
        return total;
    }

    /**
     * Attempts to add an ItemStack to a player's inventory by merging with existing stacks
     * and placing in empty slots.
     *
     * @param inventory the inventory to add to
     * @param item the ItemStack to add
     * @return the remaining ItemStack that could not be added, or null if fully added
     */
    @Nullable
    public static ItemStack addToInventory(@NotNull PlayerInventory inventory, @Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }

        ItemStack remaining = item.clone();

        // First, try to merge with existing stacks
        for (ItemStack slot : inventory.getContents()) {
            if (remaining == null || remaining.getType().isAir()) {
                break;
            }
            remaining = mergeInto(remaining, slot);
        }

        // Then, place remaining in empty slots
        if (remaining != null && !remaining.getType().isAir()) {
            int maxStack = remaining.getMaxStackSize();
            while (remaining.getAmount() > 0) {
                int emptySlot = inventory.firstEmpty();
                if (emptySlot == -1) {
                    break;
                }
                int toPlace = Math.min(remaining.getAmount(), maxStack);
                ItemStack toAdd = remaining.clone();
                toAdd.setAmount(toPlace);
                inventory.setItem(emptySlot, toAdd);
                remaining.setAmount(remaining.getAmount() - toPlace);
            }
        }

        return remaining;
    }

    /**
     * Validates if two ItemStacks are compatible for stack operations.
     * Two stacks are compatible if they have the same material.
     *
     * @param a the first ItemStack
     * @param b the second ItemStack
     * @return true if the stacks are compatible for merging
     */
    public static boolean areCompatible(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getType().isAir() || b.getType().isAir()) {
            return false;
        }
        return a.getType() == b.getType();
    }

    /**
     * Calculates the remaining capacity in an inventory for a specific item type.
     *
     * @param inventory the inventory to check
     * @param item the ItemStack to calculate capacity for
     * @return total remaining capacity
     */
    public static int calculateRemainingCapacity(@NotNull PlayerInventory inventory, @Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return 0;
        }

        int maxStack = item.getMaxStackSize();
        int remaining = 0;

        for (ItemStack slot : inventory.getContents()) {
            if (slot == null) {
                remaining += maxStack;
            } else if (slot.isSimilar(item)) {
                remaining += (maxStack - slot.getAmount());
            }
        }

        return remaining;
    }
}
