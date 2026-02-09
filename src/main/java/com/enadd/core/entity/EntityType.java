package com.enadd.core.entity;

public enum EntityType {
    PARTICLE("粒子效果", 0),
    PROJECTILE("投射物", 1),
    AREA_EFFECT("区域效果", 2),
    SUMMONED("召唤物", 3),
    TEMPORARY("临时实体", 4),
    DAMAGE("伤害实体", 5),
    UTILITY("utility实体", 6);

    private final String displayName;
    private final int priority;

    EntityType(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isPersistent() {
        return this == SUMMONED;
    }

    public boolean isTemporary() {
        return this == PARTICLE || this == TEMPORARY || this == DAMAGE;
    }
}
