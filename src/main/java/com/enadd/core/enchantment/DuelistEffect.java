package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import org.bukkit.plugin.java.JavaPlugin;


public class DuelistEffect implements IEnchantmentEffect {

    public DuelistEffect(JavaPlugin plugin) {
        // Constructor accepts plugin parameter for interface compatibility
    }

    @Override
    public void apply(EffectContext context) {
        // Implementation for duelist effect
        // This would increase damage when fighting alone
    }

    @Override
    public boolean canApply(EffectContext context) {
        // Check if the effect can be applied
        return true;
    }
}
