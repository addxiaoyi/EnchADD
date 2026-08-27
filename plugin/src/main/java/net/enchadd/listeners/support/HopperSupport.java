package net.enchadd.listeners.support;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for atomic ItemStack operations in high-concurrency environments
 * such as HopperItemEvents where multiple threads may access the same inventory slot.
 */
public final class HopperSupport {

    private HopperSupport() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Atomically increments the amount of an ItemStack by the specified delta.
     * This method is designed to prevent race conditions in multi-threaded environments
     * (e.g., multiple hoppers pulling from the same source) by performing
     * the read-check-write operation in a single logical unit.
     *
     * @param stack the ItemStack to modify
     * @param delta the amount to add (positive) or subtract (negative)
     * @return the new amount after the operation, or -1 if the stack was invalid
     */
    public static int atomic_operation(@NotNull ItemStack stack, int delta) {
        if (stack == null || stack.getType().isAir()) {
            return -1;
        }

        int currentAmount = stack.getAmount();
        int newAmount = currentAmount + delta;

        // Clamp to valid range [0, maxStackSize]
        int maxStack = stack.getMaxStackSize();
        if (newAmount < 0) {
            newAmount = 0;
        } else if (newAmount > maxStack) {
            newAmount = maxStack;
        }

        stack.setAmount(newAmount);
        return newAmount;
    }

    /**
     * Atomically sets the amount of an ItemStack, clamping to valid bounds.
     *
     * @param stack the ItemStack to modify
     * @param amount the desired amount
     * @return the new amount after clamping, or -1 if the stack was invalid
     */
    public static int atomic_set(@NotNull ItemStack stack, int amount) {
        if (stack == null || stack.getType().isAir()) {
            return -1;
        }

        int maxStack = stack.getMaxStackSize();
        int clampedAmount = amount;

        if (clampedAmount < 0) {
            clampedAmount = 0;
        } else if (clampedAmount > maxStack) {
            clampedAmount = maxStack;
        }

        stack.setAmount(clampedAmount);
        return clampedAmount;
    }

    /**
     * Atomically increments the amount of an ItemStack by 1.
     * Convenience method for the common case of adding one item.
     *
     * @param stack the ItemStack to modify
     * @return the new amount after the operation, or -1 if the stack was invalid
     */
    public static int atomic_increment(@NotNull ItemStack stack) {
        return atomic_operation(stack, 1);
    }

    /**
     * Atomically decrements the amount of an ItemStack by 1.
     * Convenience method for the common case of removing one item.
     * If the stack amount is 0, returns 0 without modification.
     *
     * @param stack the ItemStack to modify
     * @return the new amount after the operation, or -1 if the stack was invalid
     */
    public static int atomic_decrement(@NotNull ItemStack stack) {
        return atomic_operation(stack, -1);
    }
}
