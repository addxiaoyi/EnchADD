package com.enadd.core.entity;

import com.enadd.core.entity.pool.PooledEntity;


public abstract class EnchantmentEntity extends PooledEntity {
    protected final String enchantmentId;
    protected final int enchantmentLevel;
    protected final String sourcePlayerId;
    protected final EntityType entityType;
    protected volatile float progress = 0f;

    protected EnchantmentEntity(String enchantmentId, int level, String playerId, EntityType type) {
        this.enchantmentId = enchantmentId;
        this.enchantmentLevel = level;
        this.sourcePlayerId = playerId;
        this.entityType = type;
    }

    @Override
    protected void onActivate() {
        super.onActivate();
        onEnchantmentActivate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        onEnchantmentStart();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);
        updateProgress(deltaTime);
        onEnchantmentUpdate(deltaTime);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onEnchantmentDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onEnchantmentPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onEnchantmentResume();
    }

    protected void updateProgress(float deltaTime) {}

    protected void onEnchantmentActivate() {}

    protected void onEnchantmentStart() {}

    protected void onEnchantmentUpdate(float deltaTime) {}

    protected void onEnchantmentDestroy() {}

    protected void onEnchantmentPause() {}

    protected void onEnchantmentResume() {}

    public final String getEnchantmentId() {
        return enchantmentId;
    }

    public final int getEnchantmentLevel() {
        return enchantmentLevel;
    }

    public final String getSourcePlayerId() {
        return sourcePlayerId;
    }

    public final EntityType getEntityType() {
        return entityType;
    }

    public final void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(1, progress));
    }

    public final float getProgress() {
        return progress;
    }

    @Override
    public void setPersistent(boolean persistent) {
        super.setPersistent(persistent);
    }
}
