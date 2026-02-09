package com.enadd.core.enchantment;

import com.enadd.core.enchantment.EnchantmentEffectManager.EffectContext;
import org.bukkit.plugin.java.JavaPlugin;


public class ThunderBladeEffect implements IEnchantmentEffect {

    public ThunderBladeEffect(JavaPlugin plugin) {
        // Constructor accepts plugin parameter for interface compatibility
    }

    @Override
    public void apply(EffectContext context) {
        // Implementation for thunder blade effect
        // This would summon lightning
    }

    @Override
    public boolean canApply(EffectContext context) {
        // Check if the effect can be applied
        return true;
    }
}
