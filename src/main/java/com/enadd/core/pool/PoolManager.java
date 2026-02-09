package com.enadd.core.pool;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;



/**
 * 对象池管理器 - 统一管理所有对象池
 * 使用Holder模式实现线程安全的单例
 */
public final class PoolManager {

    private static final class Holder {
        private static final PoolManager INSTANCE = new PoolManager();
    }

    // 常用集合对象池
    private final ObjectPool<ArrayList<?>> arrayListPool;
    private final ObjectPool<HashMap<?, ?>> hashMapPool;
    private final ObjectPool<HashSet<?>> hashSetPool;
    private final ObjectPool<ConcurrentHashMap<?, ?>> concurrentMapPool;

    // ItemStack对象池 - 按Material分类
    private final ConcurrentHashMap<Material, ObjectPool<ItemStack>> itemStackPools;

    private PoolManager() {
        // 初始化集合对象池
        arrayListPool = new ObjectPool<>(ArrayList::new, 50, 200);
        hashMapPool = new ObjectPool<>(HashMap::new, 30, 100);
        hashSetPool = new ObjectPool<>(HashSet::new, 30, 100);
        concurrentMapPool = new ObjectPool<>(ConcurrentHashMap::new, 20, 80);

        // 初始化ItemStack池容器
        itemStackPools = new ConcurrentHashMap<>(32);
    }

    public static PoolManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 获取ArrayList对象池
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectPool<ArrayList<T>> getArrayListPool() {
        return (ObjectPool<ArrayList<T>>) (Object) arrayListPool;
    }

    /**
     * 获取HashMap对象池
     */
    @SuppressWarnings("unchecked")
    public <K, V> ObjectPool<HashMap<K, V>> getHashMapPool() {
        return (ObjectPool<HashMap<K, V>>) (Object) hashMapPool;
    }

    /**
     * 获取HashSet对象池
     */
    @SuppressWarnings("unchecked")
    public <T> ObjectPool<HashSet<T>> getHashSetPool() {
        return (ObjectPool<HashSet<T>>) (Object) hashSetPool;
    }

    /**
     * 获取ConcurrentHashMap对象池
     */
    @SuppressWarnings("unchecked")
    public <K, V> ObjectPool<ConcurrentHashMap<K, V>> getConcurrentMapPool() {
        return (ObjectPool<ConcurrentHashMap<K, V>>) (Object) concurrentMapPool;
    }

    /**
     * 获取ItemStack对象池
     * 按Material分类，避免混用
     */
    public ObjectPool<ItemStack> getItemStackPool(Material material) {
        return itemStackPools.computeIfAbsent(material,
            m -> new ObjectPool<>(() -> new ItemStack(m), 10, 50));
    }

    /**
     * 便捷方法：获取ArrayList
     */
    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> acquireArrayList() {
        ArrayList<T> list = (ArrayList<T>) getArrayListPool().acquire();
        list.clear(); // 确保清空
        return list;
    }

    /**
     * 便捷方法：归还ArrayList
     */
    @SuppressWarnings("unchecked")
    public <T> void releaseArrayList(ArrayList<T> list) {
        if (list != null) {
            list.clear();
            ((ObjectPool<ArrayList<Object>>) (Object) getArrayListPool()).release((ArrayList<Object>) (Object) list);
        }
    }

    /**
     * 便捷方法：获取HashMap
     */
    @SuppressWarnings("unchecked")
    public <K, V> HashMap<K, V> acquireHashMap() {
        HashMap<K, V> map = (HashMap<K, V>) getHashMapPool().acquire();
        map.clear(); // 确保清空
        return map;
    }

    /**
     * 便捷方法：归还HashMap
     */
    @SuppressWarnings("unchecked")
    public <K, V> void releaseHashMap(HashMap<K, V> map) {
        if (map != null) {
            map.clear();
            ((ObjectPool<HashMap<Object, Object>>) (Object) getHashMapPool()).release((HashMap<Object, Object>) (Object) map);
        }
    }

    /**
     * 便捷方法：获取HashSet
     */
    @SuppressWarnings("unchecked")
    public <T> HashSet<T> acquireHashSet() {
        HashSet<T> set = (HashSet<T>) getHashSetPool().acquire();
        set.clear(); // 确保清空
        return set;
    }

    /**
     * 便捷方法：归还HashSet
     */
    @SuppressWarnings("unchecked")
    public <T> void releaseHashSet(HashSet<T> set) {
        if (set != null) {
            set.clear();
            ((ObjectPool<HashSet<Object>>) (Object) getHashSetPool()).release((HashSet<Object>) (Object) set);
        }
    }

    /**
     * 便捷方法：获取ItemStack
     */
    public ItemStack acquireItemStack(Material material) {
        return getItemStackPool(material).acquire();
    }

    /**
     * 便捷方法：归还ItemStack
     */
    public void releaseItemStack(ItemStack item) {
        if (item != null) {
            Material material = item.getType();
            getItemStackPool(material).release(item);
        }
    }

    /**
     * 清空所有对象池
     */
    public void clearAll() {
        arrayListPool.clear();
        hashMapPool.clear();
        hashSetPool.clear();
        concurrentMapPool.clear();
        itemStackPools.values().forEach(ObjectPool::clear);
        itemStackPools.clear();
    }

    /**
     * 获取对象池统计信息
     */
    public PoolStats getStats() {
        int totalPools = 4 + itemStackPools.size();
        int totalObjects = arrayListPool.size() + hashMapPool.size() +
                          hashSetPool.size() + concurrentMapPool.size();

        for (ObjectPool<ItemStack> pool : itemStackPools.values()) {
            totalObjects += pool.size();
        }

        return new PoolStats(totalPools, totalObjects);
    }

    /**
     * 对象池统计信息
     */
    public static final class PoolStats {
        private final int totalPools;
        private final int totalObjects;

        public PoolStats(int totalPools, int totalObjects) {
            this.totalPools = totalPools;
            this.totalObjects = totalObjects;
        }

        public int getTotalPools() {
            return totalPools;
        }

        public int getTotalObjects() {
            return totalObjects;
        }

        @Override
        public String toString() {
            return String.format("Pools: %d, Objects: %d", totalPools, totalObjects);
        }
    }
}
