package com.enadd.core.entity.impl;
import com.enadd.core.entity.EnchantmentEntity;
import com.enadd.core.entity.EntityState;
import com.enadd.core.entity.EntityType;


public final class ChainLightningEntity extends EnchantmentEntity {
    private int maxTargets = 5;
    private float damage = 10.0f;
    private float chainRange = 10.0f;

    public ChainLightningEntity() {
        super("chain_lightning", 1, "", EntityType.PROJECTILE);
    }

    @Override
    protected void onActivate() {
        super.onActivate();
        setProgress(0f);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);

        if (getState() == EntityState.RUNNING) {
            float progress = getProgress() + deltaTime * 2;
            setProgress(progress);

            if (progress >= 1.0f) {
                returnToPool();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanup();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.maxTargets = 5;
        this.damage = 10.0f;
        this.chainRange = 10.0f;
    }

    @Override
    public void reset() {
        setProgress(0f);
    }

    public void setMaxTargets(int targets) {
        this.maxTargets = targets;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setChainRange(float range) {
        this.chainRange = range;
    }

    public int getMaxTargets() {
        return maxTargets;
    }

    public float getDamage() {
        return damage;
    }

    public float getChainRange() {
        return chainRange;
    }
}
