package com.enadd.achievements;

/**
 * Represents a single achievement with metadata
 */
public final class Achievement {

    private final String id;
    private final String title;
    private final String description;
    private final AchievementType type;
    private final int targetValue;

    public Achievement(String id, String title, String description, AchievementType type, int targetValue) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.targetValue = targetValue;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public AchievementType getType() {
        return type;
    }

    public int getTargetValue() {
        return targetValue;
    }

    public int getRequirement() {
        return targetValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Achievement that = (Achievement) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Achievement{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", targetValue=" + targetValue +
                '}';
    }
}
