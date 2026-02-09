package com.enadd.core.entity.pool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;


public final class EntityPool<T extends PooledEntity> {

    private final ConcurrentLinkedQueue<T> availableEntities = new ConcurrentLinkedQueue<>();
    private final AtomicInteger poolSize = new AtomicInteger(0);
    private final AtomicInteger activeCount = new AtomicInteger(0);
    private final AtomicInteger borrowedCount = new AtomicInteger(0);
    private final AtomicInteger returnedCount = new AtomicInteger(0);

    private final Supplier<T> entityFactory;
    private final int maxPoolSize;
    private final int initialSize;

    private volatile boolean isShutdown = false;

    private EntityPool(Supplier<T> factory, int initial, int max) {
        this.entityFactory = factory;
        this.initialSize = initial;
        this.maxPoolSize = max;
        initializePool();
    }

    public static <T extends PooledEntity> EntityPool<T> create(Supplier<T> factory, int initial, int max) {
        return new EntityPool<>(factory, initial, max);
    }

    private void initializePool() {
        for (int i = 0; i < initialSize; i++) {
            T entity = entityFactory.get();
            entity.setPool(this);
            entity.setPooled(true);
            availableEntities.offer(entity);
            poolSize.incrementAndGet();
        }
    }

    public T borrowEntity() {
        if (isShutdown) return null;

        T entity = availableEntities.poll();
        if (entity == null && poolSize.get() < maxPoolSize) {
            entity = entityFactory.get();
            entity.setPool(this);
            entity.setPooled(true);
            poolSize.incrementAndGet();
        }

        if (entity != null) {
            entity.setPooled(false);
            entity.onBorrow();
            activeCount.incrementAndGet();
            borrowedCount.incrementAndGet();
            entity.activate();
        }

        return entity;
    }

    public boolean returnEntity(T entity) {
        if (entity == null || isShutdown) return false;

        if (entity.isValid()) {
            entity.onReturn();
            entity.reset();
            entity.transitionTo(com.enadd.core.entity.EntityState.INIT);
            entity.setPooled(true);

            if (poolSize.get() <= maxPoolSize) {
                availableEntities.offer(entity);
                activeCount.decrementAndGet();
                returnedCount.incrementAndGet();
                return true;
            } else {
                entity.destroy();
                entity.cleanup();
            }
        }
        return false;
    }

    public boolean returnEntity(String entityId) {
        return availableEntities.stream()
                .filter(e -> entityId.equals(e.getEntityId()))
                .findFirst()
                .map(this::returnEntity)
                .orElse(false);
    }

    public void shutdown() {
        isShutdown = true;
        availableEntities.clear();
        poolSize.set(0);
        activeCount.set(0);
    }

    public void clearPool() {
        availableEntities.clear();
    }

    public int getAvailableCount() {
        return availableEntities.size();
    }

    public int getActiveCount() {
        return activeCount.get();
    }

    public int getPoolSize() {
        return poolSize.get();
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getBorrowedCount() {
        return borrowedCount.get();
    }

    public int getReturnedCount() {
        return returnedCount.get();
    }

    public float getUtilizationRate() {
        int total = poolSize.get();
        return total > 0 ? (float) activeCount.get() / total * 100 : 0;
    }

    public boolean isHealthy() {
        return borrowedCount.get() >= returnedCount.get() - 10;
    }
}
