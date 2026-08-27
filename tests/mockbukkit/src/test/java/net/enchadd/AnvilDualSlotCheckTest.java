package net.enchadd;

import net.enchadd.enchants.GluttonyEnchant;
import net.enchadd.listeners.GluttonyListener;
import net.enchadd.listeners.support.GluttonySanitizerSupport;
import net.enchadd.utils.EnchantCache;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for Anvil dual-slot check logic in GluttonyListener.
 * Verifies that Gluttony enchant sanitization only processes when BOTH
 * anvil slots contain items, to prevent single-slot exploit vulnerabilities.
 */
class AnvilDualSlotCheckTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("GluttonySanitizerSupport.add_guard_check tests")
    class GuardCheckTests {

        @Test
        @DisplayName("Returns true when both slots have valid items")
        void returnsTrueWhenBothSlotsHaveItems() {
            ItemStack left = new ItemStack(Material.DIAMOND_SWORD);
            ItemStack right = new ItemStack(Material.IRON_SWORD);

            boolean result = GluttonySanitizerSupport.add_guard_check(left, right);

            assertTrue(result, "Should return true when both slots have non-air items");
        }

        @Test
        @DisplayName("Returns false when left slot is null")
        void returnsFalseWhenLeftSlotIsNull() {
            ItemStack right = new ItemStack(Material.IRON_SWORD);

            boolean result = GluttonySanitizerSupport.add_guard_check(null, right);

            assertFalse(result, "Should return false when left slot is null");
        }

        @Test
        @DisplayName("Returns false when right slot is null")
        void returnsFalseWhenRightSlotIsNull() {
            ItemStack left = new ItemStack(Material.DIAMOND_SWORD);

            boolean result = GluttonySanitizerSupport.add_guard_check(left, null);

            assertFalse(result, "Should return false when right slot is null");
        }

        @Test
        @DisplayName("Returns false when both slots are null")
        void returnsFalseWhenBothSlotsAreNull() {
            boolean result = GluttonySanitizerSupport.add_guard_check(null, null);

            assertFalse(result, "Should return false when both slots are null");
        }

        @Test
        @DisplayName("Returns false when left slot is AIR")
        void returnsFalseWhenLeftSlotIsAir() {
            ItemStack left = new ItemStack(Material.AIR);
            ItemStack right = new ItemStack(Material.IRON_SWORD);

            boolean result = GluttonySanitizerSupport.add_guard_check(left, right);

            assertFalse(result, "Should return false when left slot is AIR");
        }

        @Test
        @DisplayName("Returns false when right slot is AIR")
        void returnsFalseWhenRightSlotIsAir() {
            ItemStack left = new ItemStack(Material.DIAMOND_SWORD);
            ItemStack right = new ItemStack(Material.AIR);

            boolean result = GluttonySanitizerSupport.add_guard_check(left, right);

            assertFalse(result, "Should return false when right slot is AIR");
        }

        @Test
        @DisplayName("Returns false when both slots are AIR")
        void returnsFalseWhenBothSlotsAreAir() {
            ItemStack left = new ItemStack(Material.AIR);
            ItemStack right = new ItemStack(Material.AIR);

            boolean result = GluttonySanitizerSupport.add_guard_check(left, right);

            assertFalse(result, "Should return false when both slots are AIR");
        }
    }

    @Nested
    @DisplayName("onPrepareAnvil event handler tests")
    class OnPrepareAnvilTests {

        @Test
        @DisplayName("Processes Gluttony sanitization when both slots have items")
        void processesWhenBothSlotsHaveItems() throws Exception {
            GluttonyListener listener = new GluttonyListener();
            setField(listener, "enchant", Enchantment.UNBREAKING);

            // Create items for both slots
            ItemStack left = new ItemStack(Material.DIAMOND_SWORD);
            left.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);

            ItemStack right = new ItemStack(Material.IRON_SWORD);
            right.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);

            // Result with Gluttony curse
            ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
            result.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
            result.addUnsafeEnchantment(Enchantment.UNBREAKING, 2);

            AnvilInventory inventory = mock(AnvilInventory.class);
            when(inventory.getFirstItem()).thenReturn(left);
            when(inventory.getSecondItem()).thenReturn(right);

            PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
            when(event.getInventory()).thenReturn(inventory);
            when(event.getResult()).thenReturn(result);

            try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
                listener.onPrepareAnvil(event);

                // Verify the event was processed
                verify(event).getResult();
                verify(event).setResult(any(ItemStack.class));
            }
        }

        @Test
        @DisplayName("Skips processing when left slot is empty")
        void skipsWhenLeftSlotIsEmpty() throws Exception {
            GluttonyListener listener = new GluttonyListener();
            setField(listener, "enchant", Enchantment.UNBREAKING);

            // Only right slot has item
            ItemStack right = new ItemStack(Material.IRON_SWORD);
            right.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);

            ItemStack result = new ItemStack(Material.DIAMOND_SWORD);

            AnvilInventory inventory = mock(AnvilInventory.class);
            when(inventory.getFirstItem()).thenReturn(null);
            when(inventory.getSecondItem()).thenReturn(right);

            PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
            when(event.getInventory()).thenReturn(inventory);
            when(event.getResult()).thenReturn(result);

            try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
                listener.onPrepareAnvil(event);

                // Verify setResult was NOT called (early return)
                verify(event, never()).setResult(any());
            }
        }

        @Test
        @DisplayName("Skips processing when right slot is empty")
        void skipsWhenRightSlotIsEmpty() throws Exception {
            GluttonyListener listener = new GluttonyListener();
            setField(listener, "enchant", Enchantment.UNBREAKING);

            // Only left slot has item
            ItemStack left = new ItemStack(Material.DIAMOND_SWORD);
            left.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);

            ItemStack result = new ItemStack(Material.DIAMOND_SWORD);

            AnvilInventory inventory = mock(AnvilInventory.class);
            when(inventory.getFirstItem()).thenReturn(left);
            when(inventory.getSecondItem()).thenReturn(null);

            PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
            when(event.getInventory()).thenReturn(inventory);
            when(event.getResult()).thenReturn(result);

            try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
                listener.onPrepareAnvil(event);

                // Verify setResult was NOT called (early return)
                verify(event, never()).setResult(any());
            }
        }

        @Test
        @DisplayName("Skips processing when both slots are empty")
        void skipsWhenBothSlotsAreEmpty() throws Exception {
            GluttonyListener listener = new GluttonyListener();
            setField(listener, "enchant", Enchantment.UNBREAKING);

            AnvilInventory inventory = mock(AnvilInventory.class);
            when(inventory.getFirstItem()).thenReturn(null);
            when(inventory.getSecondItem()).thenReturn(null);

            PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
            when(event.getInventory()).thenReturn(inventory);

            try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
                listener.onPrepareAnvil(event);

                // Verify getResult was NOT called (early return)
                verify(event, never()).getResult();
                verify(event, never()).setResult(any());
            }
        }

        @Test
        @DisplayName("Skips processing when left slot is AIR")
        void skipsWhenLeftSlotIsAir() throws Exception {
            GluttonyListener listener = new GluttonyListener();
            setField(listener, "enchant", Enchantment.UNBREAKING);

            ItemStack airLeft = new ItemStack(Material.AIR);
            ItemStack right = new ItemStack(Material.IRON_SWORD);
            right.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);

            ItemStack result = new ItemStack(Material.DIAMOND_SWORD);

            AnvilInventory inventory = mock(AnvilInventory.class);
            when(inventory.getFirstItem()).thenReturn(airLeft);
            when(inventory.getSecondItem()).thenReturn(right);

            PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
            when(event.getInventory()).thenReturn(inventory);
            when(event.getResult()).thenReturn(result);

            try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
                listener.onPrepareAnvil(event);

                // Verify setResult was NOT called (early return)
                verify(event, never()).setResult(any());
            }
        }

        @Test
        @DisplayName("Skips processing when right slot is AIR")
        void skipsWhenRightSlotIsAir() throws Exception {
            GluttonyListener listener = new GluttonyListener();
            setField(listener, "enchant", Enchantment.UNBREAKING);

            ItemStack left = new ItemStack(Material.DIAMOND_SWORD);
            left.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
            ItemStack airRight = new ItemStack(Material.AIR);

            ItemStack result = new ItemStack(Material.DIAMOND_SWORD);

            AnvilInventory inventory = mock(AnvilInventory.class);
            when(inventory.getFirstItem()).thenReturn(left);
            when(inventory.getSecondItem()).thenReturn(airRight);

            PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
            when(event.getInventory()).thenReturn(inventory);
            when(event.getResult()).thenReturn(result);

            try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
                listener.onPrepareAnvil(event);

                // Verify setResult was NOT called (early return)
                verify(event, never()).setResult(any());
            }
        }

        @Test
        @DisplayName("Skips processing when enchant is null")
        void skipsWhenEnchantIsNull() throws Exception {
            GluttonyListener listener = new GluttonyListener();
            setField(listener, "enchant", null);

            ItemStack left = new ItemStack(Material.DIAMOND_SWORD);
            left.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
            ItemStack right = new ItemStack(Material.IRON_SWORD);
            right.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);

            ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
            result.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);

            AnvilInventory inventory = mock(AnvilInventory.class);
            when(inventory.getFirstItem()).thenReturn(left);
            when(inventory.getSecondItem()).thenReturn(right);

            PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
            when(event.getInventory()).thenReturn(inventory);
            when(event.getResult()).thenReturn(result);

            try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
                listener.onPrepareAnvil(event);

                // When enchant is null, shouldSanitize returns false early
                // The event should still be processed but setResult won't be called
                verify(event, never()).setResult(any());
            }
        }
    }

    @Nested
    @DisplayName("AnvilInventory slot access edge cases")
    class SlotAccessEdgeCases {

        @Test
        @DisplayName("Handles firstItem returning itself when in single input mode")
        void handlesSingleInputMode() throws Exception {
            GluttonyListener listener = new GluttonyListener();
            setField(listener, "enchant", Enchantment.UNBREAKING);

            // Some anvil implementations return the same item when only one slot is used
            ItemStack singleItem = new ItemStack(Material.DIAMOND_SWORD);
            singleItem.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);

            ItemStack result = new ItemStack(Material.DIAMOND_SWORD);

            AnvilInventory inventory = mock(AnvilInventory.class);
            when(inventory.getFirstItem()).thenReturn(singleItem);
            when(inventory.getSecondItem()).thenReturn(null);

            PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
            when(event.getInventory()).thenReturn(inventory);
            when(event.getResult()).thenReturn(result);

            try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
                listener.onPrepareAnvil(event);

                // Should skip because right slot is empty
                verify(event, never()).setResult(any());
            }
        }

        @Test
        @DisplayName("Handles null result from anvil")
        void handlesNullResult() throws Exception {
            GluttonyListener listener = new GluttonyListener();
            setField(listener, "enchant", Enchantment.UNBREAKING);

            ItemStack left = new ItemStack(Material.DIAMOND_SWORD);
            left.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
            ItemStack right = new ItemStack(Material.IRON_SWORD);

            AnvilInventory inventory = mock(AnvilInventory.class);
            when(inventory.getFirstItem()).thenReturn(left);
            when(inventory.getSecondItem()).thenReturn(right);

            PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
            when(event.getInventory()).thenReturn(inventory);
            when(event.getResult()).thenReturn(null);

            try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
                // Should not throw exception
                assertDoesNotThrow(() -> listener.onPrepareAnvil(event));

                // verify that sanitization was attempted (null result is passed through)
                verify(event).getResult();
            }
        }
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
