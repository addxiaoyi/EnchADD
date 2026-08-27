package net.enchadd.utils;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;

final class PerformanceKeySupport {

    private PerformanceKeySupport() {
    }

    static NamespacedKey namespacedKey(Key key) {
        return new NamespacedKey(key.namespace(), key.value());
    }

    static NamespacedKey namespacedKeyWithSuffix(Key key, String suffix) {
        return new NamespacedKey(key.namespace(), key.value() + suffix);
    }

    static NamespacedKey enchaddKey(String value) {
        return new NamespacedKey("enchadd", value);
    }
}
