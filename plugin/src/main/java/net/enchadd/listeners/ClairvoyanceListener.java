package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ClairvoyanceEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
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

@SuppressWarnings("UnstableApiUsage")
public class ClairvoyanceListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(ClairvoyanceEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(ClairvoyanceEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final ClairvoyanceEnchant config;

    public ClairvoyanceListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(ClairvoyanceEnchant.KEY);
        this.config = (enchantObj instanceof ClairvoyanceEnchant) ? (ClairvoyanceEnchant) enchantObj : null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (enchant == null || config == null) return;
        
        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(victim);
        if (equipment == null) return;
        
        // 性能优化: 使用 PerformanceUtils 而非直接调用 getEnchantmentLevel
        int level = PerformanceUtils.getEnchantLevel(equipment.getHelmet(), enchant);
        if (level <= 0) return;
        
        if (!attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
        
        // 性能优化: 使用工具方法安全获取 PDC
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(victim);
        if (pdc == null) return;
        
        // 性能优化: 使用 PerformanceUtils 的冷却检查 (nanoTime)
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;
        
        double chance = Math.min(0.85, config.getTriggerChance() * level);
        // 性能优化: 使用 PerformanceUtils 的概率检查
        if (!PerformanceUtils.rollChance(chance)) return;
        
        // 性能优化: 使用工具方法计算持续时间
        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getRevealSecondsPerLevel(), level);
        PotionEffect effect = new PotionEffect(PotionEffectType.GLOWING, durationTicks, 0, false, false, true);
        attacker.addPotionEffect(effect);
        
        // 性能优化: 使用 PerformanceUtils 设置冷却
        PerformanceUtils.setCooldown(pdc, key);
    }
}
