package net.enchadd;

import net.enchadd.listeners.CurseConflictListener;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CurseConflictListenerBehaviorTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        resetEnchantConfigState();
        EnchADDConfig.init(tempDir);
    }

    @AfterEach
    void tearDown() throws Exception {
        resetEnchantConfigState();
    }

    @Test
    void enchantItemEventRemovesRolledEnchantThatConflictsWithExistingItem() {
        CurseConflictListener listener = new CurseConflictListener();

        Enchantment sharpness = mockEnchantment("minecraft", "sharpness");
        Enchantment hemorrhage = mockEnchantment("enchadd", "hemorrhage");
        Enchantment unbreaking = mockEnchantment("minecraft", "unbreaking");

        ItemStack item = mockItemWithEnchants(Map.of(sharpness, 4));
        Map<Enchantment, Integer> toAdd = new LinkedHashMap<>();
        toAdd.put(hemorrhage, 2);
        toAdd.put(unbreaking, 3);

        EnchantItemEvent event = mock(EnchantItemEvent.class);
        when(event.getItem()).thenReturn(item);
        when(event.getEnchantsToAdd()).thenReturn(toAdd);

        listener.onEnchantItem(event);

        assertFalse(toAdd.containsKey(hemorrhage));
        assertEquals(1, toAdd.size());
        assertEquals(3, toAdd.get(unbreaking));
    }

    @Test
    void enchantItemEventKeepsHigherLevelEnchantWhenRolledPairConflicts() {
        CurseConflictListener listener = new CurseConflictListener();

        Enchantment ward = mockEnchantment("enchadd", "ward");
        Enchantment barrier = mockEnchantment("enchadd", "barrier");

        ItemStack item = mockItemWithEnchants(Map.of());
        Map<Enchantment, Integer> toAdd = new LinkedHashMap<>();
        toAdd.put(ward, 1);
        toAdd.put(barrier, 3);

        EnchantItemEvent event = mock(EnchantItemEvent.class);
        when(event.getItem()).thenReturn(item);
        when(event.getEnchantsToAdd()).thenReturn(toAdd);

        listener.onEnchantItem(event);

        assertFalse(toAdd.containsKey(ward));
        assertEquals(Map.of(barrier, 3), toAdd);
    }

    @Test
    void enchantItemEventBreaksEqualLevelConflictTiesDeterministically() {
        CurseConflictListener listener = new CurseConflictListener();

        Enchantment ward = mockEnchantment("enchadd", "ward");
        Enchantment barrier = mockEnchantment("enchadd", "barrier");

        ItemStack item = mockItemWithEnchants(Map.of());
        Map<Enchantment, Integer> toAdd = new LinkedHashMap<>();
        toAdd.put(ward, 2);
        toAdd.put(barrier, 2);

        EnchantItemEvent event = mock(EnchantItemEvent.class);
        when(event.getItem()).thenReturn(item);
        when(event.getEnchantsToAdd()).thenReturn(toAdd);

        listener.onEnchantItem(event);

        assertFalse(toAdd.containsKey(ward));
        assertEquals(Map.of(barrier, 2), toAdd);
    }

    @Test
    void enchantItemEventKeepsOnlyHighestLevelCustomCurse() {
        CurseConflictListener listener = new CurseConflictListener();

        Enchantment panic = mockEnchantment("enchadd", "panic_curse");
        Enchantment gluttony = mockEnchantment("enchadd", "gluttony_curse");

        ItemStack item = mockItemWithEnchants(Map.of());
        Map<Enchantment, Integer> toAdd = new LinkedHashMap<>();
        toAdd.put(panic, 1);
        toAdd.put(gluttony, 3);

        EnchantItemEvent event = mock(EnchantItemEvent.class);
        when(event.getItem()).thenReturn(item);
        when(event.getEnchantsToAdd()).thenReturn(toAdd);

        listener.onEnchantItem(event);

        assertFalse(toAdd.containsKey(panic));
        assertEquals(Map.of(gluttony, 3), toAdd);
    }

    @Test
    void prepareAnvilEventNullsResultForIncompatiblePair() {
        CurseConflictListener listener = new CurseConflictListener();

        Enchantment ward = mockEnchantment("enchadd", "ward");
        Enchantment barrier = mockEnchantment("enchadd", "barrier");

        ItemStack left = mockItemWithEnchants(Map.of());
        ItemStack right = mockItemWithEnchants(Map.of());
        ItemStack result = mockItemWithEnchants(Map.of(ward, 1, barrier, 2));

        AnvilInventory inventory = mock(AnvilInventory.class);
        when(inventory.getFirstItem()).thenReturn(left);
        when(inventory.getSecondItem()).thenReturn(right);

        PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
        when(event.getInventory()).thenReturn(inventory);
        when(event.getResult()).thenReturn(result);

        listener.onPrepareAnvil(event);

        verify(event).setResult(null);
    }

    @Test
    void prepareAnvilEventNullsResultWhenSecondCurseWouldBeMergedOntoCursedItem() {
        CurseConflictListener listener = new CurseConflictListener();

        Enchantment panic = mockEnchantment("enchadd", "panic_curse");
        Enchantment gluttony = mockEnchantment("enchadd", "gluttony_curse");

        ItemStack left = mockItemWithEnchants(Map.of(panic, 1));
        ItemStack right = mockItemWithEnchants(Map.of());
        ItemStack result = mockItemWithEnchants(Map.of(panic, 1, gluttony, 2));

        AnvilInventory inventory = mock(AnvilInventory.class);
        when(inventory.getFirstItem()).thenReturn(left);
        when(inventory.getSecondItem()).thenReturn(right);

        PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
        when(event.getInventory()).thenReturn(inventory);
        when(event.getResult()).thenReturn(result);

        listener.onPrepareAnvil(event);

        verify(event).setResult(null);
    }

    @Test
    void prepareAnvilEventLeavesCompatibleResultUntouched() {
        CurseConflictListener listener = new CurseConflictListener();

        Enchantment ward = mockEnchantment("enchadd", "ward");
        Enchantment unbreaking = mockEnchantment("minecraft", "unbreaking");

        ItemStack left = mockItemWithEnchants(Map.of());
        ItemStack right = mockItemWithEnchants(Map.of());
        ItemStack result = mockItemWithEnchants(Map.of(ward, 1, unbreaking, 3));

        AnvilInventory inventory = mock(AnvilInventory.class);
        when(inventory.getFirstItem()).thenReturn(left);
        when(inventory.getSecondItem()).thenReturn(right);

        PrepareAnvilEvent event = mock(PrepareAnvilEvent.class);
        when(event.getInventory()).thenReturn(inventory);
        when(event.getResult()).thenReturn(result);

        listener.onPrepareAnvil(event);

        verify(event, never()).setResult(null);
    }

    private static ItemStack mockItemWithEnchants(Map<Enchantment, Integer> enchantments) {
        ItemStack item = mock(ItemStack.class);
        when(item.getEnchantments()).thenReturn(enchantments);
        return item;
    }

    private static Enchantment mockEnchantment(String namespace, String key) {
        Enchantment enchantment = Mockito.mock(Enchantment.class);
        when(enchantment.getKey()).thenReturn(new NamespacedKey(namespace, key));
        return enchantment;
    }

    private static void resetEnchantConfigState() throws Exception {
        EnchADDConfig.ENCHANTS.clear();
        getIncompatibleMap().clear();
        setInitialized(false);
    }

    @SuppressWarnings("unchecked")
    private static Map<net.kyori.adventure.key.Key, Set<net.kyori.adventure.key.Key>> getIncompatibleMap() throws Exception {
        Field field = EnchADDConfig.class.getDeclaredField("INCOMPATIBLE");
        field.setAccessible(true);
        return (Map<net.kyori.adventure.key.Key, Set<net.kyori.adventure.key.Key>>) field.get(null);
    }

    private static void setInitialized(boolean value) throws Exception {
        Field field = EnchADDConfig.class.getDeclaredField("initialized");
        field.setAccessible(true);
        field.setBoolean(null, value);
    }
}
