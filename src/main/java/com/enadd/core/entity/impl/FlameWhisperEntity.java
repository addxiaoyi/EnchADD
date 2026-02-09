package com.enadd.core.entity.impl;
import com.enadd.core.entity.EnchantmentEntity;
import com.enadd.core.entity.EntityState;
import com.enadd.core.entity.EntityType;


public final class FlameWhisperEntity extends EnchantmentEntity {
    private float radius = 3.0f;
    private float particleDensity = 0.1f;
    private long duration = 1500;

    public FlameWhisperEntity() {
        super("flame_whisper", 1, "", EntityType.PARTICLE);
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
            float progress = getProgress() + deltaTime / (duration / 1000f);
            setProgress(progress);

            if (progress >= 1.0f) {
                transitionTo(EntityState.DESTROYING);
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
        this.radius = 3.0f;
        this.particleDensity = 0.1f;
        this.duration = 1500;
    }

    @Override
    public void reset() {
        setProgress(0f);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setParticleDensity(float density) {
        this.particleDensity = density;
    }

    public void setDuration(long millis) {
        this.duration = millis;
    }

    public float getRadius() {
        return radius;
    }

    public float getParticleDensity() {
        return particleDensity;
    }

    public long getDuration() {
        return duration;
    }
}
