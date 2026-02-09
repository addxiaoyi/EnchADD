package com.enadd.enchantments.decorative;

import java.util.EnumSet;
import java.util.Set;


/**
 * 定义所有装饰性附魔类型及其属性
 */
public enum DecorativeEnchantmentType {

    SPARKLE("sparkle", "§e闪耀", "物品周围散发出金色的火花粒子效果", 2000, 1.5, ParticleType.GOLD_SPARK),
    FLAME_AURA("flame_aura", "§6火焰光环", "被炽热的火焰粒子包围", 3000, 2.0, ParticleType.FLAME),
    VOID_ECHO("void_echo", "§5虚空回响", "散发出虚空的紫色烟雾效果", 2500, 1.8, ParticleType.DRAGON_BREATH),
    FROST_BREATH("frost_breath", "§b寒冰之息", "周期性产生寒冷的雪花和云雾", 2000, 1.2, ParticleType.SNOWFLAKE),
    HEART_BEAT("heart_beat", "§c心跳", "规律地散发出爱心粒子效果", 1500, 1.0, ParticleType.HEART),
    NATURE_SPIRIT("nature_spirit", "§2自然之灵", "被绿色的树叶和快乐的村民粒子环绕", 3500, 2.5, ParticleType.HAPPY_VILLAGER, ParticleType.GREEN_SPARK);

    private final String id;
    private final String displayName;
    private final String description;
    private final int duration;
    private final double effectRadius;
    private final Set<ParticleType> particleTypes;

    DecorativeEnchantmentType(String id, String displayName, String description, int duration, double effectRadius, ParticleType... particleTypes) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.duration = duration;
        this.effectRadius = effectRadius;
        this.particleTypes = EnumSet.of(particleTypes[0], particleTypes);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getDuration() {
        return duration;
    }

    public double getEffectRadius() {
        return effectRadius;
    }

    public Set<ParticleType> getParticleTypes() {
        return particleTypes;
    }

    public static DecorativeEnchantmentType fromId(String id) {
        for (DecorativeEnchantmentType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }
}
