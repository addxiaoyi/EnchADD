package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ImmolateEnchant;
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
import org.bukkit.persistence.PersistentDataContainer;

@SuppressWarnings("UnstableApiUsage")
public class ImmolateListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(ImmolateEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(ImmolateEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final ImmolateEnchant config;

    public ImmolateListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(ImmolateEnchant.KEY);
        this.config = (enchantObj instanceof ImmolateEnchant) ? (ImmolateEnchant) enchantObj : null;
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

        double chance = config.getTriggerChance() * level;
        if (!Double.isFinite(chance)) return;
        chance = Math.min(0.6d, Math.max(0.0d, chance));
        if (!PerformanceUtils.rollChance(chance)) return;

        int durationTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getFireSecondsPerLevel(), level);
        if (durationTicks <= 0) return;
        int appliedTicks = Math.max(victim.getFireTicks(), durationTicks);
        victim.setFireTicks(appliedTicks);

        PerformanceUtils.setCooldown(pdc, key);
    }
}
