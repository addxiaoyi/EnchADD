package com.enadd.core.entity.impl;
import com.enadd.core.entity.EnchantmentEntity;
import com.enadd.core.entity.EntityType;


public final class FrostAuraEntity extends EnchantmentEntity {
    private float radius = 4.0f;
    private float speed = 0.03f;
    private long duration = 2000;

    public FrostAuraEntity() {
        super("frost_aura", 1, "", EntityType.PARTICLE);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);

        float progress = getProgress() + deltaTime / (duration / 1000f);
        setProgress(progress);

        if (progress >= 1.0f) {
            returnToPool();
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.radius = 4.0f;
        this.speed = 0.03f;
        this.duration = 2000;
    }

    @Override
    public void reset() {
        setProgress(0f);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getRadius() {
        return radius;
    }

    public float getSpeed() {
        return speed;
    }
}
