package com.enadd.core.entity.impl;
import com.enadd.core.entity.EnchantmentEntity;
import com.enadd.core.entity.EntityType;


public final class AreaMiningEntity extends EnchantmentEntity {
    private int radius = 3;
    private int depth = 1;
    private boolean autoSmelt = false;

    public AreaMiningEntity() {
        super("area_mining", 1, "", EntityType.AREA_EFFECT);
    }

    @Override
    protected void onActivate() {
        super.onActivate();
        setProgress(0f);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);

        float progress = getProgress() + deltaTime * 5;
        setProgress(progress);

        if (progress >= 1.0f) {
            returnToPool();
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.radius = 3;
        this.depth = 1;
        this.autoSmelt = false;
    }

    @Override
    public void reset() {
        setProgress(0f);
    }

    public void setAutoSmelt(boolean autoSmelt) {
        this.autoSmelt = autoSmelt;
    }

    public boolean isAutoSmelt() {
        return autoSmelt;
    }

    public int getRadius() {
        return radius;
    }

    public int getDepth() {
        return depth;
    }
}
