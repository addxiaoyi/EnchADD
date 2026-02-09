package com.enadd.core.enchantment.effects;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import com.enadd.core.enchantment.IEnchantmentEffect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;



/**
 * 基础效果类 - 提供通用功能
 * 
 * 修复：
 * - 冷却绕过漏洞：使用 UUID + 附魔ID 作为key
 * - 防止快速切换物品绕过冷却
 */
public abstract class BaseEffect implements IEnchantmentEffect {

    protected final JavaPlugin plugin;
    // 修复：使用 String key (UUID:EnchantmentID) 而不是只用 UUID
    protected final Map<String, Long> cooldowns;

    protected BaseEffect(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
    }

    @Override
    public boolean canApply(EffectContext context) {
        if (context.getPlayer() == null) return false;

        // 检查冷却 - 修复：使用附魔ID作为key的一部分
        long cooldown = getCooldown();
        if (cooldown > 0) {
            String cooldownKey = getCooldownKey(context);
            Long lastUse = cooldowns.get(cooldownKey);

            if (lastUse != null) {
                long elapsed = System.currentTimeMillis() - lastUse;
                if (elapsed < cooldown) {
                    return false;
                }
            }
        }

        // 检查触发概率
        double chance = getTriggerChance(context.getLevel());
        return Math.random() < chance;
    }

    /**
     * 记录冷却 - 修复：使用附魔ID作为key的一部分
     */
    protected void recordCooldown(EffectContext context) {
        if (getCooldown() > 0 && context.getPlayer() != null) {
            String cooldownKey = getCooldownKey(context);
            cooldowns.put(cooldownKey, System.currentTimeMillis());
        }
    }
    
    /**
     * 获取冷却key - 修复：包含附魔ID防止切换物品绕过
     */
    protected String getCooldownKey(EffectContext context) {
        UUID playerId = context.getPlayer().getUniqueId();
        String effectId = getEffectId();
        return playerId.toString() + ":" + effectId;
    }
    
    /**
     * 获取效果ID - 子类必须实现
     */
    protected abstract String getEffectId();
    
    /**
     * 清理玩家的冷却数据（玩家退出时调用）
     */
    public void clearCooldowns(UUID playerId) {
        if (playerId == null) return;
        String prefix = playerId.toString() + ":";
        cooldowns.keySet().removeIf(key -> key.startsWith(prefix));
    }

    /**
     * 获取目标生物实体
     */
    protected LivingEntity getTargetLiving(EffectContext context) {
        if (context.getTarget() instanceof LivingEntity) {
            return (LivingEntity) context.getTarget();
        }
        return null;
    }

    /**
     * 计算伤害
     */
    protected double calculateDamage(int level, double basePerLevel) {
        return level * basePerLevel;
    }

    /**
     * 计算持续时间（ticks）
     */
    protected int calculateDuration(int level, int basePerLevel) {
        return level * basePerLevel;
    }

    /**
     * 计算概率
     */
    protected double calculateChance(int level, double basePerLevel) {
        return Math.min(1.0, level * basePerLevel);
    }
}
