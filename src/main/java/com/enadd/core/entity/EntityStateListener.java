package com.enadd.core.entity;

public interface EntityStateListener {
    void onEntityCreated(ManagedEntity entity);
    void onEntityDestroyed(ManagedEntity entity);
    void onStateChanged(ManagedEntity entity, EntityState oldState, EntityState newState);
    void onError(ManagedEntity entity, Exception error);
}
