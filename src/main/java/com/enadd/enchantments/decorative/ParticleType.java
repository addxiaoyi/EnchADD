package com.enadd.enchantments.decorative;

import org.bukkit.Particle;


/**
 * 装饰性附魔使用的粒子类型映射
 */
public enum ParticleType {

    GOLD_SPARK("金色火花", Particle.ELECTRIC_SPARK),
    FLAME("火焰", Particle.FLAME),
    DRAGON_BREATH("虚空之息", Particle.DRAGON_BREATH),
    SNOWFLAKE("雪花", Particle.SNOWFLAKE),
    HEART("爱心", Particle.HEART),
    HAPPY_VILLAGER("快乐村民", Particle.HAPPY_VILLAGER),
    GREEN_SPARK("绿色火花", Particle.COMPOSTER);

    private final String displayName;
    private final Particle bukkitParticle;

    ParticleType(String displayName, Particle bukkitParticle) {
        this.displayName = displayName;
        this.bukkitParticle = bukkitParticle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Particle getBukkitParticle() {
        return bukkitParticle;
    }
}
