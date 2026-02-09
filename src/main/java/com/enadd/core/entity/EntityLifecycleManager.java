package com.enadd.core.entity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;


public final class EntityLifecycleManager {
    // 使用Holder模式实现线程安全的单例，避免同步开销
    private static final class Holder {
        private static final EntityLifecycleManager INSTANCE = new EntityLifecycleManager();
    }

    // 预分配容量以减少扩容开销
    private final ConcurrentHashMap<String, ManagedEntity> entities = new ConcurrentHashMap<>(256);
    private final CopyOnWriteArrayList<EntityStateListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger activeCount = new AtomicInteger(0);
    private final AtomicInteger totalCreated = new AtomicInteger(0);
    private final AtomicInteger totalDestroyed = new AtomicInteger(0);
    private final AtomicInteger batchOperationThreshold = new AtomicInteger(100);

    private volatile boolean isPaused = false;
    private volatile long lastUpdateTime = System.currentTimeMillis();

    private EntityLifecycleManager() {}

    public static EntityLifecycleManager getInstance() {
        return Holder.INSTANCE;
    }

    public String registerEntity(ManagedEntity entity) {
        String id = generateEntityId(entity.getClass().getSimpleName());
        entity.setEntityId(id);
        entity.setLifecycleManager(this);
        entities.put(id, entity);
        totalCreated.incrementAndGet();
        activeCount.incrementAndGet();

        notifyListeners(e -> e.onEntityCreated(entity));
        entity.transitionTo(EntityState.INIT);

        return id;
    }

    public boolean destroyEntity(String entityId) {
        ManagedEntity entity = entities.get(entityId);
        if (entity != null && entity.getState() != EntityState.DESTROYED) {
            entity.transitionTo(EntityState.DESTROYING);
            entity.onDestroy();

            entities.remove(entityId);
            if (entity.getState() == EntityState.ACTIVE || entity.getState() == EntityState.RUNNING) {
                activeCount.decrementAndGet();
            }
            totalDestroyed.incrementAndGet();

            entity.transitionTo(EntityState.DESTROYED);
            notifyListeners(e -> e.onEntityDestroyed(entity));

            entity.cleanup();
            return true;
        }
        return false;
    }

    public void pauseAllEntities() {
        isPaused = true;
        entities.values().forEach(entity -> {
            if (entity.getState().isAlive()) {
                entity.transitionTo(EntityState.PAUSED);
                entity.onPause();
            }
        });
    }

    public void resumeAllEntities() {
        isPaused = false;
        entities.values().forEach(entity -> {
            if (entity.getState() == EntityState.PAUSED) {
                entity.onResume();
                entity.transitionTo(EntityState.ACTIVE);
            }
        });
    }

    public void updateAllEntities() {
        if (isPaused) return;

        long currentTime = System.currentTimeMillis();
        float rawDeltaTime = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        final float deltaTime = Math.min(rawDeltaTime, 1f);

        int entityCount = entities.size();
        if (entityCount > batchOperationThreshold.get()) {
            batchUpdateEntities(deltaTime);
        } else {
            entities.values().forEach(entity -> {
                EntityState state = entity.getState();
                if (state == EntityState.ACTIVE || state == EntityState.RUNNING) {
                    entity.update(deltaTime);
                }
            });
        }
    }

    private void batchUpdateEntities(float deltaTime) {
        entities.values().parallelStream().forEach(entity -> {
            EntityState state = entity.getState();
            if (state == EntityState.ACTIVE || state == EntityState.RUNNING) {
                entity.update(deltaTime);
            }
        });
    }

    public void updateEntitiesByFilter(Predicate<ManagedEntity> filter, float deltaTime) {
        entities.values().stream()
                .filter(filter)
                .filter(e -> e.getState() == EntityState.ACTIVE || e.getState() == EntityState.RUNNING)
                .forEach(e -> e.update(deltaTime));
    }

    public int destroyEntitiesByFilter(Predicate<ManagedEntity> filter) {
        return entities.values().stream()
                .filter(filter)
                .filter(e -> e.getState() != EntityState.DESTROYED)
                .mapToInt(e -> destroyEntity(e.getEntityId()) ? 1 : 0)
                .sum();
    }

    public void forceCleanupAll() {
        entities.values().stream()
                .filter(e -> e.getState() != EntityState.DESTROYED)
                .forEach(e -> destroyEntity(e.getEntityId()));
    }

    public void addListener(EntityStateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(EntityStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(Consumer<EntityStateListener> action) {
        listeners.forEach(action);
    }

    private String generateEntityId(String prefix) {
        return String.format("%s_%d_%d", prefix, System.currentTimeMillis(), totalCreated.get());
    }

    public ManagedEntity getEntity(String entityId) {
        return entities.get(entityId);
    }

    public int getActiveEntityCount() {
        return activeCount.get();
    }

    public int getTotalEntityCount() {
        return entities.size();
    }

    public int getTotalCreated() {
        return totalCreated.get();
    }

    public int getTotalDestroyed() {
        return totalDestroyed.get();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public EntityStateStats getStats() {
        return new EntityStateStats(
                totalCreated.get(),
                totalDestroyed.get(),
                activeCount.get(),
                entities.size(),
                isPaused
        );
    }

    public static final class EntityStateStats {
        private final int totalCreated;
        private final int totalDestroyed;
        private final int activeCount;
        private final int totalCount;
        private final boolean paused;

        public EntityStateStats(int totalCreated, int totalDestroyed, int activeCount, int totalCount, boolean paused) {
            this.totalCreated = totalCreated;
            this.totalDestroyed = totalDestroyed;
            this.activeCount = activeCount;
            this.totalCount = totalCount;
            this.paused = paused;
        }

        public int getTotalCreated() { return totalCreated; }
        public int getTotalDestroyed() { return totalDestroyed; }
        public int getActiveCount() { return activeCount; }
        public int getTotalCount() { return totalCount; }
        public boolean isPaused() { return paused; }
        public float getSurvivalRate() {
            int active = totalCreated - totalDestroyed;
            return totalCreated > 0 ? (float) active / totalCreated * 100 : 0;
        }
    }
}
