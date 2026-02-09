package com.enadd.core.enchantment.effects.combat;

import com.enadd.config.EnchantmentConfig;
import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import com.enadd.core.enchantment.EnchantmentEffectManager.EffectTrigger;
import com.enadd.core.enchantment.effects.BaseEffect;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * 流血效果 - 造成持续伤害
 *
 * <p>当玩家攻击敌人时，有几率使敌人进入流血状态，造成持续伤害。</p>
 *
 * <p><strong>效果机制：</strong></p>
 * <ul>
 *   <li>触发几率：基础30% + 每级10%</li>
 *   <li>伤害：每级0.5点伤害/秒</li>
 *   <li>持续时间：每级60 ticks（3秒），最大300 ticks（15秒）</li>
 *   <li>冷却时间：3秒</li>
 * </ul>
 *
 * <p><strong>视觉效果：</strong></p>
 * <ul>
 *   <li>深红色粒子效果</li>
 *   <li>受伤音效</li>
 * </ul>
 *
 * @author ADDXIAOYI2048
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BleedingEffect extends BaseEffect {

    /** 粒子颜色 - 深红色 */
    private static final Color BLEEDING_PARTICLE_COLOR = Color.fromRGB(139, 0, 0);
    /** 粒子大小 */
    private static final float PARTICLE_SIZE = 1.0f;
    /** 粒子数量 */
    private static final int PARTICLE_COUNT = 10;
    /** 粒子扩散范围 */
    private static final double PARTICLE_SPREAD = 0.3;
    /** 音效音量 */
    private static final float SOUND_VOLUME = 0.3f;
    /** 音效音调 */
    private static final float SOUND_PITCH = 0.8f;
    /** 每秒tick数 */
    private static final int TICKS_PER_SECOND = 20;

    public BleedingEffect(JavaPlugin plugin) {
        super(plugin);
    }
    
    @Override
    protected String getEffectId() {
        return "bleeding";
    }

    @Override
    public void apply(EffectContext context) {
        if (context.getTrigger() != EffectTrigger.ATTACK) return;

        LivingEntity target = getTargetLiving(context);
        if (target == null || target.isDead()) return;

        int level = context.getLevel();

        // 使用配置常量计算伤害和持续时间
        double damagePerSecond = level * EnchantmentConfig.BleedingConfig.DAMAGE_PER_LEVEL;
        int duration = Math.min(
            level * EnchantmentConfig.BleedingConfig.DURATION_TICKS_PER_LEVEL,
            EnchantmentConfig.BleedingConfig.MAX_DURATION_TICKS
        );

        applyBleedingEffect(context, target, damagePerSecond, duration);
        recordCooldown(context);
    }

    /**
     * 应用流血效果
     *
     * @param context 效果上下文
     * @param target 目标实体
     * @param damagePerSecond 每秒伤害
     * @param duration 持续时间（ticks）
     */
    private void applyBleedingEffect(EffectContext context, LivingEntity target,
                                     double damagePerSecond, int duration) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (shouldCancelEffect(target, ticks, duration)) {
                    cancel();
                    return;
                }

                // 每秒造成一次伤害
                if (ticks % TICKS_PER_SECOND == 0) {
                    applyDamageAndEffects(context, target, damagePerSecond);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    /**
     * 检查是否应该取消效果
     *
     * @param target 目标实体
     * @param currentTicks 当前tick数
     * @param maxDuration 最大持续时间
     * @return 是否应该取消
     */
    private boolean shouldCancelEffect(LivingEntity target, int currentTicks, int maxDuration) {
        return currentTicks >= maxDuration || target.isDead() || !target.isValid();
    }

    /**
     * 应用伤害和视觉效果
     *
     * @param context 效果上下文
     * @param target 目标实体
     * @param damage 伤害值
     */
    private void applyDamageAndEffects(EffectContext context, LivingEntity target, double damage) {
        // 造成伤害
        target.damage(damage, context.getPlayer());

        // 播放粒子效果
        spawnBleedingParticles(target);

        // 播放音效
        playBleedingSound(target);
    }

    /**
     * 生成流血粒子效果
     *
     * @param target 目标实体
     */
    private void spawnBleedingParticles(LivingEntity target) {
        target.getWorld().spawnParticle(
            Particle.DUST,
            target.getLocation().add(0, target.getHeight() / 2, 0),
            PARTICLE_COUNT,
            PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD,
            new Particle.DustOptions(BLEEDING_PARTICLE_COLOR, PARTICLE_SIZE)
        );
    }

    /**
     * 播放流血音效
     *
     * @param target 目标实体
     */
    private void playBleedingSound(LivingEntity target) {
        target.getWorld().playSound(
            target.getLocation(),
            Sound.ENTITY_PLAYER_HURT,
            SOUND_VOLUME,
            SOUND_PITCH
        );
    }

    @Override
    public long getCooldown() {
        return 3000; // 3秒冷却
    }

    @Override
    public double getTriggerChance(int level) {
        return EnchantmentConfig.BleedingConfig.TRIGGER_CHANCE_BASE +
               (level * EnchantmentConfig.BleedingConfig.TRIGGER_CHANCE_PER_LEVEL);
    }
}

