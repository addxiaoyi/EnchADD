package com.enadd.core.enchantment;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import com.enadd.config.ConfigManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;




/**
 * 附魔效果管理器 - 统一管理所有附魔效果
 * 使用Holder模式实现线程安全的单例
 */
public final class EnchantmentEffectManager {

    private static final class Holder {
        private static final EnchantmentEffectManager INSTANCE = new EnchantmentEffectManager();
    }

    private final Map<String, IEnchantmentEffect> effects;

    private EnchantmentEffectManager() {
        this.effects = new ConcurrentHashMap<>(256);
    }

    public static EnchantmentEffectManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 注册附魔效果
     */
    public void registerEffect(String enchantmentId, IEnchantmentEffect effect) {
        effects.put(enchantmentId.toLowerCase(), effect);
    }

    /**
     * 应用附魔效果
     * 考虑配置的强度调整
     */
    public boolean applyEffect(String enchantmentId, EffectContext context) {
        if (enchantmentId == null || enchantmentId.trim().isEmpty()) {
            return false;
        }

        if (context == null) {
            return false;
        }

        IEnchantmentEffect effect = effects.get(enchantmentId.toLowerCase());
        if (effect != null && effect.canApply(context)) {
            try {
                // 获取附魔强度配置
                double intensity = ConfigManager.getEnchantmentIntensity(enchantmentId);
                double globalIntensity = ConfigManager.getGlobalIntensity();

                // 计算最终触发概率
                double finalIntensity = intensity * globalIntensity;

                // 根据强度决定是否触发
                if (finalIntensity >= 1.0 || Math.random() < finalIntensity) {
                    effect.apply(context);
                    return true;
                }
                return false;
            } catch (Exception e) {
                // 如果效果应用失败，记录错误但不崩溃
                if (context.getPlayer() != null) {
                    Bukkit.getLogger().log(Level.WARNING,
                        "Failed to apply enchantment effect " + enchantmentId + ": " + e.getMessage(), e
                    );
                }
                return false;
            }
        }
        return false;
    }

    /**
     * 检查是否可以应用效果
     */
    public boolean canApply(String enchantmentId, EffectContext context) {
        IEnchantmentEffect effect = effects.get(enchantmentId.toLowerCase());
        return effect != null && effect.canApply(context);
    }

    /**
     * 获取已注册的效果数量
     */
    public int getRegisteredEffectCount() {
        return effects.size();
    }

    /**
     * 清理所有效果
     */
    public void clearAll() {
        effects.clear();
    }

    /**
     * 效果上下文 - 包含所有必要的信息
     */
    public static class EffectContext {
        private final Player player;
        private final Entity target;
        private final ItemStack item;
        private final int level;
        private final Event event;
        private final EffectTrigger trigger;

        public EffectContext(Player player, Entity target, ItemStack item, int level, Event event, EffectTrigger trigger) {
            this.player = player;
            this.target = target;
            this.item = item;
            this.level = level;
            this.event = event;
            this.trigger = trigger;
        }

        public Player getPlayer() { return player; }
        public Entity getTarget() { return target; }
        public ItemStack getItem() { return item; }
        public int getLevel() { return level; }
        public Event getEvent() { return event; }
        public EffectTrigger getTrigger() { return trigger; }
    }

    /**
     * 效果触发类型
     */
    public enum EffectTrigger {
        ATTACK,           // 攻击时
        DEFEND,           // 防御时
        MINE,             // 挖掘时
        SHOOT,            // 射击时
        HIT,              // 命中时
        KILL,             //击杀时
        HURT,             // 受伤时
        MOVE,             // 移动时
        JUMP,             // 跳跃时
        SNEAK,            // 潜行时
        INTERACT,         // 交互时
        BREAK_BLOCK,      // 破坏方块时
        PLACE_BLOCK,      // 放置方块时
        CONSUME,          // 消耗时
        EQUIP,            // 装备时
        UNEQUIP,          // 卸下时
        TICK,             // 每tick
        PASSIVE           // 被动效果
    }
}
