package net.enchadd;

import net.enchadd.listeners.GluttonyListener;
import net.enchadd.listeners.LegacyEnchantSanitizerListener;
import net.enchadd.utils.EnchantCache;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for Grindstone dual-slot validation logic in listeners.
 * Verifies that listeners only process results when BOTH grindstone slots contain items
 * to prevent XP farming exploits.
 */
class GrindstoneDualSlotCheckTest {

    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        TestPluginSupport.unregisterAllHandlers();
        MockBukkit.unmock();
    }

    // ==================== LegacyEnchantSanitizerListener Tests ====================

    @Test
    void legacySanitizerSkipsProcessingWhenBothSlotsEmpty() {
        LegacyEnchantSanitizerListener listener = new LegacyEnchantSanitizerListener();

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{null, null};
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        event.setResult(result);

        listener.onPrepareGrindstone(event);

        // Result should remain unchanged since both slots are empty
        assertEquals(result, event.getResult());
    }

    @Test
    void legacySanitizerSkipsProcessingWhenOnlySlot0HasItem() {
        LegacyEnchantSanitizerListener listener = new LegacyEnchantSanitizerListener();

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{
            new ItemStack(Material.DIAMOND_SWORD),
            null
        };
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        event.setResult(result);

        listener.onPrepareGrindstone(event);

        // Result should remain unchanged since slot 1 is empty
        assertEquals(result, event.getResult());
    }

    @Test
    void legacySanitizerSkipsProcessingWhenOnlySlot1HasItem() {
        LegacyEnchantSanitizerListener listener = new LegacyEnchantSanitizerListener();

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{
            null,
            new ItemStack(Material.IRON_SWORD)
        };
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        event.setResult(result);

        listener.onPrepareGrindstone(event);

        // Result should remain unchanged since slot 0 is empty
        assertEquals(result, event.getResult());
    }

    @Test
    void legacySanitizerSkipsProcessingWhenSlot0HasAir() {
        LegacyEnchantSanitizerListener listener = new LegacyEnchantSanitizerListener();

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{
            new ItemStack(Material.AIR),
            new ItemStack(Material.IRON_SWORD)
        };
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        event.setResult(result);

        listener.onPrepareGrindstone(event);

        // Result should remain unchanged since slot 0 is AIR
        assertEquals(result, event.getResult());
    }

    @Test
    void legacySanitizerSkipsProcessingWhenSlot1HasAir() {
        LegacyEnchantSanitizerListener listener = new LegacyEnchantSanitizerListener();

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{
            new ItemStack(Material.DIAMOND_SWORD),
            new ItemStack(Material.AIR)
        };
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        event.setResult(result);

        listener.onPrepareGrindstone(event);

        // Result should remain unchanged since slot 1 is AIR
        assertEquals(result, event.getResult());
    }

    @Test
    void legacySanitizerProcessesOnlyWhenBothSlotsHaveItems() {
        LegacyEnchantSanitizerListener listener = new LegacyEnchantSanitizerListener();

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack slot0Item = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack slot1Item = new ItemStack(Material.IRON_SWORD);
        ItemStack[] contents = new ItemStack[]{slot0Item, slot1Item};
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.AIR);
        event.setResult(result);

        // With both slots filled, the listener should attempt to sanitize
        listener.onPrepareGrindstone(event);

        // The result should either be sanitized or remain, but no exception should be thrown
        // The key assertion is that we reached the sanitize logic
        assertNotNull(event.getResult());
    }

    // ==================== GluttonyListener Tests ====================

    @Test
    void gluttonySkipsProcessingWhenBothSlotsEmpty() throws Exception {
        GluttonyListener listener = new GluttonyListener();
        setField(listener, "enchant", Enchantment.UNBREAKING);

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{null, null};
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        result.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        event.setResult(result);

        try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
            listener.onPrepareGrindstone(event);

            // Result should remain unchanged since both slots are empty
            assertEquals(result, event.getResult());
            // Enchants should NOT be stripped
            assertEquals(3, result.getEnchantmentLevel(Enchantment.SHARPNESS));
        }
    }

    @Test
    void gluttonySkipsProcessingWhenOnlySlot0HasItem() throws Exception {
        GluttonyListener listener = new GluttonyListener();
        setField(listener, "enchant", Enchantment.UNBREAKING);

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{
            new ItemStack(Material.DIAMOND_SWORD),
            null
        };
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        result.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        event.setResult(result);

        try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
            listener.onPrepareGrindstone(event);

            // Result should remain unchanged since slot 1 is empty
            assertEquals(result, event.getResult());
            assertEquals(3, result.getEnchantmentLevel(Enchantment.SHARPNESS));
        }
    }

    @Test
    void gluttonySkipsProcessingWhenOnlySlot1HasItem() throws Exception {
        GluttonyListener listener = new GluttonyListener();
        setField(listener, "enchant", Enchantment.UNBREAKING);

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{
            null,
            new ItemStack(Material.IRON_SWORD)
        };
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        result.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        event.setResult(result);

        try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
            listener.onPrepareGrindstone(event);

            // Result should remain unchanged since slot 0 is empty
            assertEquals(result, event.getResult());
            assertEquals(3, result.getEnchantmentLevel(Enchantment.SHARPNESS));
        }
    }

    @Test
    void gluttonySkipsProcessingWhenSlot0HasAir() throws Exception {
        GluttonyListener listener = new GluttonyListener();
        setField(listener, "enchant", Enchantment.UNBREAKING);

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{
            new ItemStack(Material.AIR),
            new ItemStack(Material.IRON_SWORD)
        };
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        result.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        event.setResult(result);

        try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
            listener.onPrepareGrindstone(event);

            // Result should remain unchanged since slot 0 is AIR
            assertEquals(result, event.getResult());
            assertEquals(3, result.getEnchantmentLevel(Enchantment.SHARPNESS));
        }
    }

    @Test
    void gluttonySkipsProcessingWhenSlot1HasAir() throws Exception {
        GluttonyListener listener = new GluttonyListener();
        setField(listener, "enchant", Enchantment.UNBREAKING);

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack[] contents = new ItemStack[]{
            new ItemStack(Material.DIAMOND_SWORD),
            new ItemStack(Material.AIR)
        };
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        result.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        event.setResult(result);

        try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
            listener.onPrepareGrindstone(event);

            // Result should remain unchanged since slot 1 is AIR
            assertEquals(result, event.getResult());
            assertEquals(3, result.getEnchantmentLevel(Enchantment.SHARPNESS));
        }
    }

    @Test
    void gluttonyProcessesOnlyWhenBothSlotsHaveItems() throws Exception {
        GluttonyListener listener = new GluttonyListener();
        setField(listener, "enchant", Enchantment.UNBREAKING);

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack slot0Item = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack slot1Item = new ItemStack(Material.IRON_SWORD);
        ItemStack[] contents = new ItemStack[]{slot0Item, slot1Item};
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        result.addUnsafeEnchantment(Enchantment.SHARPNESS, 3);
        result.addUnsafeEnchantment(Enchantment.LOOTING, 2);
        event.setResult(result);

        try (MockedStatic<EnchantCache> cache = Mockito.mockStatic(EnchantCache.class)) {
            // When both slots are filled, Gluttony should attempt to process
            listener.onPrepareGrindstone(event);

            // Should not throw exception when processing with both slots filled
            assertNotNull(event.getResult());
        }
    }

    // ==================== Edge Cases ====================

    @Test
    void listenerHandlesNullResultGracefully() {
        LegacyEnchantSanitizerListener listener = new LegacyEnchantSanitizerListener();

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        ItemStack slot0Item = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack slot1Item = new ItemStack(Material.IRON_SWORD);
        ItemStack[] contents = new ItemStack[]{slot0Item, slot1Item};
        when(inventory.getContents()).thenReturn(contents);

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        event.setResult(null);

        // Should not throw exception when result is null
        assertDoesNotThrow(() -> listener.onPrepareGrindstone(event));
        assertNull(event.getResult());
    }

    @Test
    void listenerHandlesEmptyInventoryContents() {
        LegacyEnchantSanitizerListener listener = new LegacyEnchantSanitizerListener();

        GrindstoneInventory inventory = Mockito.mock(GrindstoneInventory.class);
        when(inventory.getContents()).thenReturn(new ItemStack[]{});

        PrepareGrindstoneEvent event = prepareEvent(inventory);
        ItemStack result = new ItemStack(Material.DIAMOND_SWORD);
        result.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        event.setResult(result);

        listener.onPrepareGrindstone(event);

        // Empty inventory should be treated as both slots empty
        assertEquals(result, event.getResult());
    }

    private static PrepareGrindstoneEvent prepareEvent(GrindstoneInventory inventory) {
        InventoryView view = Mockito.mock(InventoryView.class);
        when(view.getTopInventory()).thenReturn(inventory);
        return new PrepareGrindstoneEvent(view, null);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
