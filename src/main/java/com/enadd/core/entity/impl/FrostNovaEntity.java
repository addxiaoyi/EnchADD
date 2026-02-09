package com.enadd.core.entity.impl;
import com.enadd.core.entity.EnchantmentEntity;
import com.enadd.core.entity.EntityType;


public final class FrostNovaEntity extends EnchantmentEntity {
    private float radius = 8.0f;
    private float slowEffect = 0.5f;
    private long duration = 3000;

    public FrostNovaEntity() {
        super("frost_nova", 1, "", EntityType.AREA_EFFECT);
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
        this.radius = 8.0f;
        this.slowEffect = 0.5f;
        this.duration = 3000;
    }

    @Override
    public void reset() {
        setProgress(0f);
    }

    public void setSlowEffect(float effect) {
        this.slowEffect = effect;
    }

    public float getSlowEffect() {
        return slowEffect;
    }

    public float getRadius() {
        return radius;
    }
}
