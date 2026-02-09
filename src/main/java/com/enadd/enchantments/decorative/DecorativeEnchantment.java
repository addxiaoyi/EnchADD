package com.enadd.enchantments.decorative;

import java.util.UUID;


/**
 * 代表一个具体的装饰性附魔实例
 */
public final class DecorativeEnchantment {

    private final UUID id;
    private final DecorativeEnchantmentType type;
    private final int level;
    private long lastTriggerTime;
    private int triggerCount;

    public DecorativeEnchantment(DecorativeEnchantmentType type, int level) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.level = level;
        this.lastTriggerTime = 0;
        this.triggerCount = 0;
    }

    public UUID getId() {
        return id;
    }

    public DecorativeEnchantmentType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return type.getDisplayName();
    }

    public boolean shouldTrigger() {
        // 简单的触发频率限制 (例如 500ms)
        return System.currentTimeMillis() - lastTriggerTime > 500;
    }

    public void recordTrigger() {
        this.lastTriggerTime = System.currentTimeMillis();
        this.triggerCount++;
    }

    public int getTriggerCount() {
        return triggerCount;
    }
}
