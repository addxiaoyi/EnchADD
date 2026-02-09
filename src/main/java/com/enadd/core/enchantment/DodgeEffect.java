package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import com.enadd.core.enchantment.EnchantmentEffectManager.EffectTrigger;
import com.enadd.core.enchantment.effects.BaseEffect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.ThreadLocalRandom;


/**
 * 闪避效果 - 有几率完全躲避攻击
 *
 * <p>当玩家受到攻击时，有一定几率完全免疫该次伤害。</p>
 *
 * <p><strong>效果机制：</strong></p>
 * <ul>
 *   <li>闪避几率：基础5% + 每级3%，最大35%</li>
 *   <li>触发条件：受到伤害时</li>
 *   <li>冷却时间：500毫秒</li>
 * </ul>
 *
 * <p><strong>视觉效果：</strong></p>
 * <ul>
 *   <li>烟雾粒子效果</li>
 *   <li>闪避音效</li>
 * </ul>
 *
 * @author ADDXIAOYI2048
 * @version 1.0.0
 * @since 1.0.0
 */
public class DodgeEffect extends BaseEffect {

    /** 基础闪避几率 */
    private static final double DODGE_CHANCE_BASE = 0.05;
    /** 每级增加的闪避几率 */
    private static final double DODGE_CHANCE_PER_LEVEL = 0.03;
    /** 最大闪避几率 */
    private static final double MAX_DODGE_CHANCE = 0.35;
    /** 粒子数量 */
    private static final int PARTICLE_COUNT = 15;
    /** 粒子扩散范围 */
    private static final double PARTICLE_SPREAD = 0.5;
    /** 音效音量 */
    private static final float SOUND_VOLUME = 0.6f;
    /** 音效音调 */
    private static final float SOUND_PITCH = 1.8f;
    /** 冷却时间（毫秒） */
    private static final long COOLDOWN_MS = 500;

    public DodgeEffect(JavaPlugin plugin) {
        super(plugin);
    }
    
    @Override
    protected String getEffectId() {
        return "dodge";
    }

    @Override
    public long getCooldown() {
        return COOLDOWN_MS;
    }

    @Override
    public void apply(EffectContext context) {
        // 只在受伤时触发
        if (context.getTrigger() != EffectTrigger.DEFEND) {
            return;
        }

        // 检查事件类型
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) {
            return;
        }

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();

        // 检查受害者是否为玩家
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        int level = context.getLevel();

        // 计算闪避几率
        double dodgeChance = calculateDodgeChance(level);

        // 随机判定是否闪避
        if (ThreadLocalRandom.current().nextDouble() < dodgeChance) {
            // 闪避成功
            performDodge(player, event);
        }
    }

    /**
     * 计算闪避几率
     *
     * @param level 附魔等级
     * @return 闪避几率（0.0-1.0）
     */
    private double calculateDodgeChance(int level) {
        double chance = DODGE_CHANCE_BASE + (level - 1) * DODGE_CHANCE_PER_LEVEL;
        return Math.min(chance, MAX_DODGE_CHANCE);
    }

    /**
     * 执行闪避
     *
     * @param player 玩家
     * @param event 伤害事件
     */
    private void performDodge(Player player, EntityDamageByEntityEvent event) {
        // 取消伤害
        event.setCancelled(true);

        // 播放粒子效果
        spawnDodgeParticles(player);

        // 播放音效
        playDodgeSound(player);

        // 调试日志
        if (plugin != null) {
            plugin.getLogger().fine(String.format(
                "闪避: %s 成功闪避了一次攻击",
                player.getName()
            ));
        }
    }

    /**
     * 生成闪避粒子效果
     *
     * @param player 玩家
     */
    private void spawnDodgeParticles(Player player) {
        player.getWorld().spawnParticle(
            Particle.LARGE_SMOKE,
            player.getLocation().add(0, player.getHeight() / 2, 0),
            PARTICLE_COUNT,
            PARTICLE_SPREAD, PARTICLE_SPREAD, PARTICLE_SPREAD,
            0.1
        );
    }

    /**
     * 播放闪避音效
     *
     * @param player 玩家
     */
    private void playDodgeSound(Player player) {
        player.getWorld().playSound(
            player.getLocation(),
            Sound.ENTITY_BAT_TAKEOFF,
            SOUND_VOLUME,
            SOUND_PITCH
        );
    }

    @Override
    public boolean canApply(EffectContext context) {
        // 只在受伤时触发
        if (context.getTrigger() != EffectTrigger.DEFEND) {
            return false;
        }

        // 检查受害者是否为玩家
        if (!(context.getEvent() instanceof EntityDamageByEntityEvent)) {
            return false;
        }

        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) context.getEvent();
        return event.getEntity() instanceof Player;
    }

}

