package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import com.enadd.core.enchantment.EnchantmentEffectManager.EffectTrigger;
import com.enadd.core.enchantment.effects.BaseEffect;
import com.enadd.config.EnchantmentConfig;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * 生命偷取效果 - 攻击时恢复生命值
 *
 * <p>当玩家攻击敌人时，根据造成的伤害恢复一定比例的生命值。</p>
 *
 * <p><strong>效果机制：</strong></p>
 * <ul>
 *   <li>恢复比例：基础10% + 每级5%，最大50%</li>
 *   <li>触发条件：攻击造成伤害时</li>
 *   <li>冷却时间：无</li>
 * </ul>
 *
 * <p><strong>视觉效果：</strong></p>
 * <ul>
 *   <li>心形粒子效果</li>
 *   <li>生命恢复音效</li>
 * </ul>
 *
 * @author ADDXIAOYI2048
 * @version 1.0.0
 * @since 1.0.0
 */
public class LifeStealEffect extends BaseEffect {

    /** 粒子数量 */
    private static final int HEAL_PARTICLE_COUNT = 8;
    /** 粒子扩散范围 */
    private static final double PARTICLE_SPREAD = 0.3;
    /** 音效音量 */
    private static final float SOUND_VOLUME = 0.4f;
    /** 音效音调 */
    private static final float SOUND_PITCH = 1.5f;

    public LifeStealEffect(JavaPlugin plugin) {
        super(plugin);
    }
    
    @Override
    protected String getEffectId() {
        return "life_steal";
    }

    @Override
    public void apply(EffectContext context) {
        // 只在攻击时触发
        if (context.getTrigger() != EffectTrigger.ATTACK) {
            return;
        }

        // 检查事件类型
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();

        // 检查攻击者是否为玩家
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        org.bukkit.entity.Entity target = context.getTarget();

        if (!(target instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingTarget = (LivingEntity) target;
        if (livingTarget.isDead() || !livingTarget.isValid()) {
            return;
        }

        int level = context.getLevel();

        // 计算生命偷取比例
        double healPercentage = calculateHealPercentage(level);

        // 计算实际伤害（用于计算恢复量）
        double damage = event.getFinalDamage();
        double healAmount = damage * healPercentage;

        // 应用生命恢复
        applyLifeSteal(player, healAmount);
    }

    /**
     * 计算生命偷取比例
     *
     * @param level 附魔等级
     * @return 恢复比例（0.0-1.0）
     */
    private double calculateHealPercentage(int level) {
        double percentage = EnchantmentConfig.LifeStealConfig.HEAL_PERCENTAGE_BASE +
                (level * EnchantmentConfig.LifeStealConfig.HEAL_PERCENTAGE_PER_LEVEL);
        return Math.min(percentage, EnchantmentConfig.LifeStealConfig.MAX_HEAL_PERCENTAGE);
    }

    /**
     * 应用生命偷取效果
     *
     * @param player 玩家
     * @param healAmount 恢复量
     */
    private void applyLifeSteal(Player player, double healAmount) {
        // 检查玩家是否需要治疗
        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        if (player.getHealth() >= maxHealth) {
            return;
        }

        // 恢复生命值
        double newHealth = Math.min(player.getHealth() + healAmount, maxHealth);
        player.setHealth(newHealth);

        // 播放粒子效果
        spawnHealParticles(player);

        // 播放音效
        playHealSound(player);

        // 调试日志
        if (plugin != null) {
            plugin.getLogger().fine(String.format(
                "生命偷取: %s 恢复了 %.1f 生命值",
                player.getName(),
                healAmount
            ));
        }
    }

    /**
     * 生成治疗粒子效果
     *
     * @param player 玩家
     */
    private void spawnHealParticles(Player player) {
        player.getWorld().spawnParticle(
            Particle.HEART,
            player.getLocation().add(0, player.getHeight() / 2, 0),
            HEAL_PARTICLE_COUNT,
            PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD,
            0.1
        );
    }

    /**
     * 播放治疗音效
     *
     * @param player 玩家
     */
    private void playHealSound(Player player) {
        player.getWorld().playSound(
            player.getLocation(),
            Sound.ENTITY_PLAYER_LEVELUP,
            SOUND_VOLUME,
            SOUND_PITCH
        );
    }

    @Override
    public boolean canApply(EffectContext context) {
        // 只在攻击时触发
        if (context.getTrigger() != EffectTrigger.ATTACK) {
            return false;
        }

        // 检查攻击者是否为玩家
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) {
            return false;
        }

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
        return event.getDamager() instanceof Player;
    }

    @Override
    public long getCooldown() {
        return 0; // 生命偷取无冷却
    }
}

