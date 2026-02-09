package com.enadd.core.entity.pool;

import com.enadd.core.entity.EntityState;
import com.enadd.core.entity.ManagedEntity;


public abstract class PooledEntity extends ManagedEntity {
    private EntityPool<?> pool;
    private boolean isPooled = false;
    private int reuseCount = 0;
    private volatile long lastBorrowTime;
    private volatile long totalActiveTime;

    protected PooledEntity() {
        super();
    }

    @Override
    protected void onStateChanged(EntityState oldState, EntityState newState) {
        if (newState == EntityState.ACTIVE) {
            lastBorrowTime = System.currentTimeMillis();
        } else if (oldState == EntityState.ACTIVE && newState == EntityState.DESTROYED) {
            totalActiveTime += System.currentTimeMillis() - lastBorrowTime;
        }
    }

    public final void setPool(EntityPool<?> pool) {
        this.pool = pool;
    }

    public final EntityPool<?> getPool() {
        return pool;
    }

    public final void setPooled(boolean pooled) {
        isPooled = pooled;
    }

    public final boolean isPooled() {
        return isPooled;
    }

    @SuppressWarnings("unchecked")
    public final void returnToPool() {
        if (pool != null) {
            ((EntityPool<PooledEntity>) pool).returnEntity(this);
        }
    }

    protected void onBorrow() {
        reuseCount++;
    }

    protected void onReturn() {}

    public final int getReuseCount() {
        return reuseCount;
    }

    public final long getLastBorrowTime() {
        return lastBorrowTime;
    }

    public final long getTotalActiveTime() {
        return totalActiveTime;
    }

    public final float getAverageActiveTime() {
        return reuseCount > 0 ? (float) totalActiveTime / reuseCount : 0;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.pool = null;
        this.isPooled = false;
    }

    public abstract void reset();
}
