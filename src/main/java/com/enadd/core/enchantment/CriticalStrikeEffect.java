package com.enadd.core.enchantment;

import com.enadd.config.EnchantmentConfig;
import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import com.enadd.core.enchantment.EnchantmentEffectManager.EffectTrigger;
import com.enadd.core.enchantment.effects.BaseEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;



/**
 * 暴击效果 - 增加暴击几率和暴击伤害
 *
 * <p>当玩家攻击时，有几率触发暴击，造成额外伤害。</p>
 *
 * <p><strong>效果机制：</strong></p>
 * <ul>
 *   <li>暴击几率：基础5% + 每级3%，最大50%</li>
 *   <li>暴击伤害：基础1.5倍 + 每级0.25倍</li>
 *   <li>触发条件：攻击时</li>
 *   <li>冷却时间：无</li>
 * </ul>
 *
 * <p><strong>视觉效果：</strong></p>
 * <ul>
 *   <li>暴击粒子效果</li>
 *   <li>暴击音效</li>
 * </ul>
 *
 * @author ADDXIAOYI2048
 * @version 1.0.0
 * @since 1.0.0
 */
public class CriticalStrikeEffect extends BaseEffect {

    private final Random random;

    /** 粒子数量 */
    private static final int CRITICAL_PARTICLE_COUNT = 20;
    /** 粒子扩散范围 */
    private static final double PARTICLE_SPREAD = 0.5;
    /** 音效音量 */
    private static final float SOUND_VOLUME = 0.5f;
    /** 音效音调 */
    private static final float SOUND_PITCH = 1.2f;

    public CriticalStrikeEffect(JavaPlugin plugin) {
        super(plugin);
        this.random = ThreadLocalRandom.current();
    }
    
    @Override
    protected String getEffectId() {
        return "critical_strike";
    }

    @Override
    public void apply(EffectContext context) {
        // 只在攻击时触发
        if (context.getTrigger() != EffectTrigger.ATTACK) {
            return;
        }

        // 检查目标
        if (!(context.getTarget() instanceof LivingEntity)) {
            return;
        }

        LivingEntity target = (LivingEntity) context.getTarget();
        if (target.isDead() || !target.isValid()) {
            return;
        }

        int level = context.getLevel();

        // 计算暴击几率
        double criticalChance = calculateCriticalChance(level);

        // 判断是否触发暴击
        if (random.nextDouble() > criticalChance) {
            return;
        }

        // 计算暴击伤害倍数
        double damageMultiplier = calculateDamageMultiplier(level);

        // 应用暴击效果
        applyCriticalHit(context, target, damageMultiplier);
    }

    /**
     * 计算暴击几率
     *
     * @param level 附魔等级
     * @return 暴击几率（0.0-1.0）
     */
    private double calculateCriticalChance(int level) {
        double chance = EnchantmentConfig.CriticalStrikeConfig.CHANCE_BASE +
                (level * EnchantmentConfig.CriticalStrikeConfig.CHANCE_PER_LEVEL);
        return Math.min(chance, EnchantmentConfig.CriticalStrikeConfig.MAX_CHANCE);
    }

    /**
     * 计算暴击伤害倍数
     *
     * @param level 附魔等级
     * @return 伤害倍数
     */
    private double calculateDamageMultiplier(int level) {
        return EnchantmentConfig.CriticalStrikeConfig.DAMAGE_MULTIPLIER_BASE +
                (level * EnchantmentConfig.CriticalStrikeConfig.DAMAGE_MULTIPLIER_PER_LEVEL);
    }

    /**
     * 应用暴击效果
     *
     * @param context 效果上下文
     * @param target 目标实体
     * @param damageMultiplier 伤害倍数
     */
    private void applyCriticalHit(EffectContext context, LivingEntity target, double damageMultiplier) {
        // 获取原始事件
        if (context.getEvent() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();

            // 计算额外伤害
            double originalDamage = event.getDamage();
            double bonusDamage = originalDamage * (damageMultiplier - 1.0);

            // 增加伤害
            event.setDamage(originalDamage + bonusDamage);

            // 播放视觉效果
            spawnCriticalParticles(target);

            // 播放音效
            playCriticalSound(target);

            // 记录日志（DEBUG级别）
            if (plugin != null) {
                plugin.getLogger().fine(String.format(
                    "暴击触发！玩家 %s 对 %s 造成 %.1f 倍暴击伤害（额外 %.1f 伤害）",
                    context.getPlayer().getName(),
                    target.getName(),
                    damageMultiplier,
                    bonusDamage
                ));
            }
        }
    }

    /**
     * 生成暴击粒子效果
     *
     * @param target 目标实体
     */
    private void spawnCriticalParticles(LivingEntity target) {
        target.getWorld().spawnParticle(
            Particle.CRIT,
            target.getLocation().add(0, target.getHeight() / 2, 0),
            CRITICAL_PARTICLE_COUNT,
            PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD,
            0.1
        );

        // 额外添加火花效果
        target.getWorld().spawnParticle(
            Particle.FIREWORK,
            target.getLocation().add(0, target.getHeight() / 2, 0),
            10,
            PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD,
            0.05
        );
    }

    /**
     * 播放暴击音效
     *
     * @param target 目标实体
     */
    private void playCriticalSound(LivingEntity target) {
        target.getWorld().playSound(
            target.getLocation(),
            Sound.ENTITY_PLAYER_ATTACK_CRIT,
            SOUND_VOLUME,
            SOUND_PITCH
        );

        // 额外播放一次强化的音效
        target.getWorld().playSound(
            target.getLocation(),
            Sound.ENTITY_IRON_GOLEM_ATTACK,
            SOUND_VOLUME * 0.5f,
            SOUND_PITCH
        );
    }

    @Override
    public boolean canApply(EffectContext context) {
        // 检查基本触发条件
        if (context.getTrigger() != EffectTrigger.ATTACK) {
            return false;
        }

        // 检查目标是否有效
        if (!(context.getTarget() instanceof LivingEntity)) {
            return false;
        }

        LivingEntity target = (LivingEntity) context.getTarget();
        return !target.isDead() && target.isValid();
    }

    @Override
    public long getCooldown() {
        return 0; // 暴击无冷却，完全基于几率
    }
}

