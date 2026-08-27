package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.TremorEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public class TremorListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(TremorEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(TremorEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final TremorEnchant config;

    public TremorListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(TremorEnchant.KEY);
        this.config = (enchantObj instanceof TremorEnchant) ? (TremorEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        if (enchant == null || config == null) return;
        
        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;
        
        // 性能优化: 使用 PerformanceUtils 而非直接调用 getEnchantmentLevel
        int level = PerformanceUtils.getEnchantLevel(equipment.getItemInMainHand(), enchant);
        if (level <= 0) return;
        
        // 性能优化: 使用工具方法安全获取 PDC
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) return;
        
        // 性能优化: 使用 PerformanceUtils 的冷却检查 (nanoTime)
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;
        
        double chance = Math.min(0.5, config.getTriggerChance() * level);
        // 性能优化: 使用 PerformanceUtils 的概率检查
        if (!PerformanceUtils.rollChance(chance)) return;
        
        // 性能优化: 使用工具方法计算持续时间
        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getSlowSecondsPerLevel(), level);
        double r = Math.max(0.5, config.getRadiusPerLevel() * level);
        
        // 性能优化: 避免 Stream API，直接遍历集合
        Collection<Entity> nearbyEntities = victim.getNearbyEntities(r, r, r);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (target.equals(victim) || target.equals(player)) continue;
            
            PotionEffect effect = new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 0, false, false, true);
            target.addPotionEffect(effect);
        }
        
        // 性能优化: 使用 PerformanceUtils 设置冷却
        PerformanceUtils.setCooldown(pdc, key);
    }
}
