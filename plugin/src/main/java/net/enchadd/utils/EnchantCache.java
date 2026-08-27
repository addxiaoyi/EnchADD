package net.enchadd.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * LRU cache for enchantment levels with per-item invalidation indexing.
 * Optimized with ReadWriteLock to reduce contention on read path.
 */
public class EnchantCache {

    private static final int MAX_CACHE_SIZE = 10000;

    // Use ReadWriteLock for better read concurrency
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // LRU cache keyed by item identity + enchantment.
    private static final Map<CacheKey, Integer> cache = new LinkedHashMap<CacheKey, Integer>(
            MAX_CACHE_SIZE + 1, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<CacheKey, Integer> eldest) {
            boolean evict = size() > MAX_CACHE_SIZE;
            if (evict) {
                unindex(eldest.getKey());
            }
            return evict;
        }
    };

    // Reverse index to invalidate all entries for a mutated item without scanning the whole cache.
    private static final Map<Integer, java.util.LinkedHashSet<CacheKey>> index = new java.util.HashMap<>();

    private EnchantCache() {}

    public static int getLevel(@Nullable ItemStack item, @NotNull Enchantment enchantment) {
        if (item == null || item.getType().isAir()) return 0;

        CacheKey key = new CacheKey(item, enchantment);

        // Use read lock for better concurrency on read path
        rwLock.readLock().lock();
        try {
            Integer cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
        } finally {
            rwLock.readLock().unlock();
        }

        // Cache miss - need to write
        int level = item.getEnchantmentLevel(enchantment);

        rwLock.writeLock().lock();
        try {
            // Double-check after acquiring write lock
            Integer cached = cache.get(key);
            if (cached != null) {
                return cached;
            }
            cache.put(key, level);
            index.computeIfAbsent(key.itemHashCode, ignored -> new java.util.LinkedHashSet<>()).add(key);
            return level;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Call this when an item is modified to invalidate its cache.
     */
    public static void invalidate(@Nullable ItemStack item) {
        if (item == null) return;

        rwLock.writeLock().lock();
        try {
            java.util.LinkedHashSet<CacheKey> keys = index.remove(System.identityHashCode(item));
            if (keys == null || keys.isEmpty()) {
                return;
            }
            for (CacheKey key : keys) {
                cache.remove(key);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public static void clear() {
        rwLock.writeLock().lock();
        try {
            cache.clear();
            index.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private static void unindex(CacheKey key) {
        java.util.LinkedHashSet<CacheKey> keys = index.get(key.itemHashCode);
        if (keys == null) {
            return;
        }
        keys.remove(key);
        if (keys.isEmpty()) {
            index.remove(key.itemHashCode);
        }
    }
    
    /**
     * Immutable cache key based on item identity and enchantment.
     */
    private static class CacheKey {
        private final int itemHashCode;
        private final Enchantment enchantment;
        private final int hashCode;
        
        CacheKey(ItemStack item, Enchantment enchantment) {
            this.itemHashCode = System.identityHashCode(item);
            this.enchantment = enchantment;
            this.hashCode = 31 * itemHashCode + enchantment.hashCode();
        }
        
        boolean matchesItem(ItemStack item) {
            return this.itemHashCode == System.identityHashCode(item);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof CacheKey)) return false;
            CacheKey other = (CacheKey) obj;
            return this.itemHashCode == other.itemHashCode && 
                   this.enchantment.equals(other.enchantment);
        }
        
        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
