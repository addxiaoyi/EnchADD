package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.MomentumEnchant;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class MomentumListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment momentum = registry.get(MomentumEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final MomentumEnchant config;

    public MomentumListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(MomentumEnchant.KEY);
        this.config = (enchantObj instanceof MomentumEnchant) ? (MomentumEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onKillGainSpeed(EntityDeathEvent event) {
        if (momentum == null || config == null) return;
        if (event.getDamageSource() == null || event.getDamageSource().isIndirect()) return;

        Entity killer = event.getDamageSource().getCausingEntity();
        if (!(killer instanceof LivingEntity living)) return;

        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(living);
        if (equipment == null) return;

        // 性能优化: 使用 PerformanceUtils 获取最高附魔等级
        int level = PerformanceUtils.getHighestEnchantLevel(equipment, momentum);
        if (level <= 0) return;

        // 性能优化: 使用 PerformanceUtils 计算持续时间
        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getSpeedSecondsPerLevel(), level);
        int amplifier = Math.max(0, config.getSpeedAmplifier());
        PotionEffect effect = new PotionEffect(PotionEffectType.SPEED, durationTicks, amplifier, false, false, true);
        living.addPotionEffect(effect);
    }
}
