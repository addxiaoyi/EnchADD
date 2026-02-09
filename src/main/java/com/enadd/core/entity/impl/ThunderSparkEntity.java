package com.enadd.core.entity.impl;
import com.enadd.core.entity.EnchantmentEntity;
import com.enadd.core.entity.EntityType;


public final class ThunderSparkEntity extends EnchantmentEntity {
    private float radius = 5.0f;
    private float intensity = 0.15f;
    private long duration = 1200;

    public ThunderSparkEntity() {
        super("thunder_spark", 1, "", EntityType.PARTICLE);
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
        this.radius = 5.0f;
        this.intensity = 0.15f;
        this.duration = 1200;
    }

    @Override
    public void reset() {
        setProgress(0f);
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public float getIntensity() {
        return intensity;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
