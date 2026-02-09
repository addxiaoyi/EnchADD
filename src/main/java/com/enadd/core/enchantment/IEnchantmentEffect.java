package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;


/**
 * 附魔效果接口 - 所有附魔效果必须实现此接口
 */
public interface IEnchantmentEffect {

    /**
     * 应用效果
     * @param context 效果上下文
     */
    void apply(EffectContext context);

    /**
     * 检查是否可以应用效果
     * @param context 效果上下文
     * @return 是否可以应用
     */
    boolean canApply(EffectContext context);

    /**
     * 获取冷却时间（毫秒）
     * @return 冷却时间，0表示无冷却
     */
    default long getCooldown() {
        return 0;
    }

    /**
     * 获取触发概率（0.0-1.0）
     * @param level 附魔等级
     * @return 触发概率
     */
    default double getTriggerChance(int level) {
        return 1.0;
    }
}
