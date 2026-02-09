package com.enadd.core.entity;

import java.util.concurrent.atomic.AtomicReference;


public abstract class ManagedEntity {
    private final AtomicReference<EntityState> state = new AtomicReference<>(EntityState.INIT);
    private volatile String entityId;
    private volatile EntityLifecycleManager lifecycleManager;
    private volatile long creationTime;
    private volatile long lastActivityTime;
    private volatile boolean isPersistent = false;

    protected ManagedEntity() {
        this.creationTime = System.currentTimeMillis();
        this.lastActivityTime = creationTime;
    }

    public final void transitionTo(EntityState newState) {
        EntityState current = state.get();
        if (current == newState || current == EntityState.DESTROYED) {
            return;
        }

        if (!validateTransition(current, newState)) {
            throw new IllegalStateException(String.format("Invalid state transition from %s to %s for entity %s",
                    current, newState, entityId));
        }

        state.set(newState);
        lastActivityTime = System.currentTimeMillis();

        onStateChanged(current, newState);
    }

    protected boolean validateTransition(EntityState from, EntityState to) {
        return switch (from) {
            case INIT -> to == EntityState.ACTIVATING;
            case ACTIVATING -> to == EntityState.ACTIVE || to == EntityState.DESTROYED;
            case ACTIVE -> to == EntityState.RUNNING || to == EntityState.PAUSED || to == EntityState.DESTROYING;
            case RUNNING -> to == EntityState.ACTIVE || to == EntityState.PAUSED || to == EntityState.DESTROYING;
            case PAUSED -> to == EntityState.ACTIVE || to == EntityState.DESTROYING;
            case DESTROYING -> to == EntityState.DESTROYED;
            default -> false;
        };
    }

    protected void onStateChanged(EntityState oldState, EntityState newState) {}

    public void activate() {
        transitionTo(EntityState.ACTIVATING);
        onActivate();
        transitionTo(EntityState.ACTIVE);
    }

    public void start() {
        transitionTo(EntityState.RUNNING);
        onStart();
    }

    public void update(float deltaTime) {
        if (state.get() == EntityState.RUNNING || state.get() == EntityState.ACTIVE) {
            onUpdate(deltaTime);
            lastActivityTime = System.currentTimeMillis();
        }
    }

    public void pause() {
        transitionTo(EntityState.PAUSED);
        onPause();
    }

    public void resume() {
        if (state.get() == EntityState.PAUSED) {
            onResume();
            transitionTo(EntityState.ACTIVE);
        }
    }

    public void destroy() {
        if (lifecycleManager != null) {
            lifecycleManager.destroyEntity(entityId);
        }
    }

    protected void onActivate() {}
    protected void onStart() {}
    protected void onUpdate(float deltaTime) {}
    protected void onPause() {}
    protected void onResume() {}
    protected void onDestroy() {}
    protected void cleanup() {}

    public final void setEntityId(String id) {
        this.entityId = id;
    }

    public final void setLifecycleManager(EntityLifecycleManager manager) {
        this.lifecycleManager = manager;
    }

    public final String getEntityId() {
        return entityId;
    }

    public final EntityState getState() {
        return state.get();
    }

    public final long getCreationTime() {
        return creationTime;
    }

    public final long getLastActivityTime() {
        return lastActivityTime;
    }

    public final long getUptime() {
        return System.currentTimeMillis() - creationTime;
    }

    public final long getIdleTime() {
        return System.currentTimeMillis() - lastActivityTime;
    }

    public final boolean isAlive() {
        return state.get().isAlive();
    }

    public final boolean isValid() {
        return state.get().isValid();
    }

    public void setPersistent(boolean persistent) {
        isPersistent = persistent;
    }

    public boolean isPersistent() {
        return isPersistent;
    }

    protected EntityLifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }
}
