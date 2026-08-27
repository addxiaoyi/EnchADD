package net.enchadd;

import net.enchadd.enchants.LegacyEnchantCompat;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyEnchantCompatContractTest {

    @Test
    void legacyCompatibilityRegistryKeepsExpectedAliasesAndRemovedKeys() {
        Set<Key> legacyKeys = LegacyEnchantCompat.legacyKeys();

        assertTrue(legacyKeys.contains(Key.key("enchadd:cloaking")));
        assertTrue(legacyKeys.contains(Key.key("enchadd:sonar")));
        assertTrue(legacyKeys.contains(Key.key("enchadd:arrow_refund")));
        assertTrue(legacyKeys.contains(Key.key("enchadd:panic")));
        assertTrue(legacyKeys.contains(Key.key("enchadd:vampirism")));

        assertEquals(Key.key("enchadd:panic_curse"), LegacyEnchantCompat.migrationTarget(Key.key("enchadd:panic")));
        assertEquals(Key.key("enchadd:vampirism_curse"), LegacyEnchantCompat.migrationTarget(Key.key("enchadd:vampirism")));
        assertNull(LegacyEnchantCompat.migrationTarget(Key.key("enchadd:cloaking")));
        assertNull(LegacyEnchantCompat.migrationTarget(Key.key("enchadd:sonar")));
    }
}
