package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import com.enadd.core.enchantment.EnchantmentEffectManager.EffectTrigger;
import com.enadd.core.enchantment.effects.BaseEffect;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


/**
 * 吸血鬼效果 - 攻击时恢复生命值并获得吸血buff
 *
 * <p>当玩家攻击敌人时，恢复生命值并获得短暂的力量提升。</p>
 *
 * <p><strong>效果机制：</strong></p>
 * <ul>
 *   <li>生命恢复：造成伤害的15%</li>
 *   <li>力量提升：攻击后获得2秒力量I效果</li>
 *   <li>触发条件：攻击造成伤害时</li>
 * </ul>
 *
 * <p><strong>视觉效果：</strong></p>
 * <ul>
 *   <li>红色粒子效果</li>
 *   <li>吸血鬼嘶吼音效</li>
 * </ul>
 *
 * @author ADDXIAOYI2048
 * @version 1.0.0
 * @since 1.0.0
 */
public class VampirismEffect extends BaseEffect {

    /** 粒子数量 */
    private static final int PARTICLE_COUNT = 12;
    /** 粒子扩散范围 */
    private static final double PARTICLE_SPREAD = 0.4;
    /** 音效音量 */
    private static final float SOUND_VOLUME = 0.5f;
    /** 音效音调 */
    private static final float SOUND_PITCH = 0.8f;
    /** 生命恢复比例 */
    private static final double HEAL_PERCENTAGE = 0.15;
    /** 力量效果持续时间（tick） */
    private static final int STRENGTH_DURATION = 40;
    /** 力量效果等级 */
    private static final int STRENGTH_AMPLIFIER = 0;

    public VampirismEffect(JavaPlugin plugin) {
        super(plugin);
    }
    
    @Override
    protected String getEffectId() {
        return "vampirism";
    }

    @Override
    public void apply(EffectContext context) {
        if (context.getTrigger() != EffectTrigger.ATTACK) {
            return;
        }

        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();

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

        // 计算恢复量
        double damage = event.getFinalDamage();
        double healAmount = damage * HEAL_PERCENTAGE * (1 + (level - 1) * 0.1);

        // 应用生命恢复
        applyVampirism(player, healAmount, level);
    }

    /**
     * 应用吸血鬼效果
     *
     * @param player 玩家
     * @param healAmount 恢复量
     * @param level 附魔等级
     */
    private void applyVampirism(Player player, double healAmount, int level) {
        // 恢复生命值
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        if (player.getHealth() < maxHealth) {
            double newHealth = Math.min(player.getHealth() + healAmount, maxHealth);
            player.setHealth(newHealth);
        }

        // 添加力量效果
        int duration = STRENGTH_DURATION + (level - 1) * 20;
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.STRENGTH,
            duration,
            STRENGTH_AMPLIFIER,
            false,
            true,
            true
        ));

        // 播放粒子效果
        spawnVampirismParticles(player);

        // 播放音效
        playVampirismSound(player);
    }

    /**
     * 生成吸血鬼粒子效果
     *
     * @param player 玩家
     */
    private void spawnVampirismParticles(Player player) {
        player.getWorld().spawnParticle(
            Particle.DUST,
            player.getLocation().add(0, player.getHeight() / 2, 0),
            PARTICLE_COUNT,
            PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD,
            0.1,
            new Particle.DustOptions(Color.RED, 1.0f)
        );
    }

    /**
     * 播放吸血鬼音效
     *
     * @param player 玩家
     */
    private void playVampirismSound(Player player) {
        player.getWorld().playSound(
            player.getLocation(),
            Sound.ENTITY_BAT_AMBIENT,
            SOUND_VOLUME,
            SOUND_PITCH
        );
    }

    @Override
    public boolean canApply(EffectContext context) {
        if (context.getTrigger() != EffectTrigger.ATTACK) {
            return false;
        }

        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) {
            return false;
        }

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
        return event.getDamager() instanceof Player;
    }

    @Override
    public long getCooldown() {
        return 0;
    }
}

