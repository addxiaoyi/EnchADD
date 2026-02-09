package com.enadd.core.pool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


/**
 * 高性能对象池 - 减少GC压力
 * 使用无锁队列实现，支持并发访问
 *
 * @param <T> 池化对象类型
 */
public final class ObjectPool<T> {

    private final ConcurrentLinkedQueue<T> pool;
    private final Supplier<T> factory;
    private final int maxSize;
    private final AtomicInteger currentSize;

    /**
     * 创建对象池
     *
     * @param factory 对象工厂
     * @param initialSize 初始大小
     * @param maxSize 最大大小
     */
    public ObjectPool(Supplier<T> factory, int initialSize, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.pool = new ConcurrentLinkedQueue<>();
        this.currentSize = new AtomicInteger(0);

        // 预创建对象
        for (int i = 0; i < initialSize; i++) {
            pool.offer(factory.get());
            currentSize.incrementAndGet();
        }
    }

    /**
     * 从池中获取对象
     * 如果池为空，创建新对象
     */
    public T acquire() {
        T obj = pool.poll();
        if (obj == null) {
            obj = factory.get();
        }
        return obj;
    }

    /**
     * 归还对象到池中
     * 如果池已满，丢弃对象
     */
    public void release(T obj) {
        if (obj == null) {
            return;
        }

        if (currentSize.get() < maxSize) {
            pool.offer(obj);
            currentSize.incrementAndGet();
        }
    }

    /**
     * 清空对象池
     */
    public void clear() {
        pool.clear();
        currentSize.set(0);
    }

    /**
     * 获取当前池大小
     */
    public int size() {
        return currentSize.get();
    }

    /**
     * 获取最大池大小
     */
    public int getMaxSize() {
        return maxSize;
    }
}
