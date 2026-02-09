package com.enadd.core.entity;

public enum EntityState {
    INIT("初始化", 0),
    ACTIVATING("激活中", 1),
    ACTIVE("活跃", 2),
    RUNNING("运行中", 3),
    PAUSED("已暂停", 4),
    DESTROYING("销毁中", 5),
    DESTROYED("已销毁", 6),
    ORPHAN("孤立", 7);

    private final String displayName;
    private final int priority;

    EntityState(String displayName, int priority) {
        this.displayName = displayName;
        this.priority = priority;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isAlive() {
        return this == ACTIVE || this == RUNNING || this == ACTIVATING;
    }

    public boolean isTransitioning() {
        return this == ACTIVATING || this == DESTROYING || this == PAUSED;
    }

    public boolean isValid() {
        return this != DESTROYED && this != ORPHAN;
    }
}
