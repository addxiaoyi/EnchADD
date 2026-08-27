package net.enchadd.utils;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerformanceKeySupportTest {

    @Test
    void namespacedKeyKeepsNamespaceAndValue() {
        NamespacedKey key = PerformanceKeySupport.namespacedKey(Key.key("enchadd", "alpha"));

        assertEquals("enchadd", key.getNamespace());
        assertEquals("alpha", key.getKey());
    }

    @Test
    void namespacedKeyWithSuffixAppendsSuffixToValue() {
        NamespacedKey key = PerformanceKeySupport.namespacedKeyWithSuffix(Key.key("enchadd", "alpha"), "_cooldown");

        assertEquals("enchadd", key.getNamespace());
        assertEquals("alpha_cooldown", key.getKey());
    }

    @Test
    void enchaddKeyUsesPluginNamespace() {
        NamespacedKey key = PerformanceKeySupport.enchaddKey("probe");

        assertEquals("enchadd", key.getNamespace());
        assertEquals("probe", key.getKey());
    }
}
