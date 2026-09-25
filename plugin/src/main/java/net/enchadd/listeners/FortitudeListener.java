package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.FortitudeEnchant;
import net.enchadd.listeners.support.SurvivalHealthSupport;
import net.enchadd.listeners.support.SurvivalBuffSupport;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class FortitudeListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment fortitude = registry.get(FortitudeEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(FortitudeEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final FortitudeEnchant config;

    public FortitudeListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(FortitudeEnchant.KEY);
        this.config = (enchantObj instanceof FortitudeEnchant) ? (FortitudeEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onLowHealth(EntityDamageEvent event) {
        if (event.isCancelled() || fortitude == null || config == null) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(entity);
        if (equipment == null) return;

        ItemStack chestplate = equipment.getChestplate();
        if (chestplate == null) return;

        // 性能优化: 使用 PerformanceUtils 而非直接调用 getEnchantmentLevel
        int level = PerformanceUtils.getEnchantLevel(chestplate, fortitude);
        level = Math.min(level, config.getMaxLevel());
        if (level <= 0) return;

        double rawThreshold = (double) level * config.getHeartsThresholdPerLevel() * 2.0d;
        if (!PerformanceUtils.isEntityValid(entity)) return;
        if (!SurvivalHealthSupport.survivesBelowThreshold(entity.getHealth(), event.getFinalDamage(),
                entity.getMaxHealth(), rawThreshold)) return;

        // 性能优化: 使用工具方法安全获取 PDC
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(entity);
        if (pdc == null) return;

        // 性能优化: 使用 PerformanceUtils 的冷却检查 (nanoTime)
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;

        // Reserve the cooldown before potion events can re-enter this listener.
        Long previousCooldown = pdc.get(key, PersistentDataType.LONG);
        PerformanceUtils.setCooldown(pdc, key);
        if (!SurvivalBuffSupport.applyFortitude(entity, level, config)) {
            if (previousCooldown == null) {
                pdc.remove(key);
            } else {
                pdc.set(key, PersistentDataType.LONG, previousCooldown);
            }
        }
    }
}
