package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ShadowstrikeEnchant;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;

@SuppressWarnings("UnstableApiUsage")
public class ShadowstrikeListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(ShadowstrikeEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(ShadowstrikeEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final ShadowstrikeEnchant config;

    public ShadowstrikeListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(ShadowstrikeEnchant.KEY);
        this.config = (enchantObj instanceof ShadowstrikeEnchant) ? (ShadowstrikeEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMeleeBackstab(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;
        
        // 性能优化: 使用 PerformanceUtils 而非直接调用 getEnchantmentLevel
        int level = PerformanceUtils.getEnchantLevel(attacker.getInventory().getItemInMainHand(), enchant);
        if (level <= 0) return;
        
        // 性能优化: 使用工具方法安全获取 PDC
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(attacker);
        if (pdc == null) return;
        
        // 性能优化: 使用 PerformanceUtils 的冷却检查 (nanoTime)
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;
        
        Vector victimFacing = victim.getLocation().getDirection().normalize();
        Vector toAttacker = attacker.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();
        double dot = victimFacing.dot(toAttacker);
        if (dot <= 0.7) return;
        
        // 性能优化: 使用 PerformanceUtils 设置冷却
        PerformanceUtils.setCooldown(pdc, key);
        
        double bonus = level * config.getBonusDamagePerLevel();
        event.setDamage(event.getDamage() + bonus);
    }
}
