package net.enchadd.utils;

import java.util.ArrayList;
import java.util.HashMap;

final class PerformanceCollectionSupport {

    private PerformanceCollectionSupport() {
    }

    static <T> ArrayList<T> newArrayListWithCapacity(int capacity) {
        return new ArrayList<>(Math.max(10, capacity));
    }

    static <K, V> HashMap<K, V> newHashMapWithCapacity(int capacity) {
        int initialCapacity = (int) Math.ceil(capacity / 0.75) + 1;
        return new HashMap<>(initialCapacity);
    }
}
