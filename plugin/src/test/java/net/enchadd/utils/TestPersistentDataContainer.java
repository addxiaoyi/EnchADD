package net.enchadd.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
final class TestPersistentDataContainer implements PersistentDataContainer {

    private final Map<NamespacedKey, Object> values = new HashMap<>();

    @Override
    public <P, C> void set(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type, @NotNull C value) {
        values.put(key, value);
    }

    @Override
    public <P, C> boolean has(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type) {
        return values.containsKey(key);
    }

    @Override
    public boolean has(@NotNull NamespacedKey key) {
        return values.containsKey(key);
    }

    @Override
    public <P, C> @Nullable C get(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type) {
        return (C) values.get(key);
    }

    @Override
    public <P, C> @NotNull C getOrDefault(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type, @NotNull C defaultValue) {
        C value = get(key, type);
        return value == null ? defaultValue : value;
    }

    @Override
    public @NotNull Set<NamespacedKey> getKeys() {
        return Set.copyOf(values.keySet());
    }

    public int getSize() {
        return values.size();
    }

    @Override
    public void remove(@NotNull NamespacedKey key) {
        values.remove(key);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public @NotNull PersistentDataAdapterContext getAdapterContext() {
        throw new UnsupportedOperationException("Adapter context is not needed in tests");
    }

    @Override
    public void copyTo(@NotNull PersistentDataContainer other, boolean replace) {
        if (other instanceof TestPersistentDataContainer target) {
            for (Map.Entry<NamespacedKey, Object> entry : values.entrySet()) {
                if (replace || !target.values.containsKey(entry.getKey())) {
                    target.values.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public byte @NotNull [] serializeToBytes() {
        return new byte[0];
    }

    @Override
    public void readFromBytes(byte @NotNull [] bytes, boolean clear) {
        if (clear) {
            values.clear();
        }
    }
}
