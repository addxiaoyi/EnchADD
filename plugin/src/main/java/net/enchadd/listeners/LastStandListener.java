package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.LastStandEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.persistence.PersistentDataContainer;

@SuppressWarnings("UnstableApiUsage")
public class LastStandListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(LastStandEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(LastStandEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final LastStandEnchant config;

    public LastStandListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(LastStandEnchant.KEY);
        this.config = (enchantObj instanceof LastStandEnchant) ? (LastStandEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(entity);
        if (equipment == null) return;
        if (equipment.getChestplate() == null) return;

        // 性能优化: 使用 PerformanceUtils 而非直接调用 getEnchantmentLevel
        int level = PerformanceUtils.getEnchantLevel(equipment.getChestplate(), enchant);
        if (level <= 0) return;

        double health = entity.getHealth();
        double finalDamage = event.getFinalDamage();
        if (finalDamage < health) return;
        if (!PerformanceUtils.isEntityValid(entity)) return;

        // 性能优化: 使用工具方法安全获取 PDC
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(entity);
        if (pdc == null) return;

        // 性能优化: 使用 PerformanceUtils 的冷却检查 (nanoTime)
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;

        // 性能优化: 使用 PerformanceUtils 的概率检查
        double chance = Math.min(config.getMaxTriggerChance(), config.getTriggerChance() * level);
        if (!PerformanceUtils.rollChance(chance)) return;

        double newHealth = Math.min(entity.getMaxHealth(), 2.0);
        if (newHealth < 1.0) {
            newHealth = 1.0;
        }
        event.setCancelled(true);
        entity.setHealth(newHealth);

        // 性能优化: 使用 PerformanceUtils 设置冷却
        PerformanceUtils.setCooldown(pdc, key);
    }
}
