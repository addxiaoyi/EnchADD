package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.FortitudeEnchant;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.persistence.PersistentDataContainer;

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
        if (fortitude == null || config == null) return;
        if (!(event.getEntity() instanceof LivingEntity entity)) return;

        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(entity);
        if (equipment == null) return;

        ItemStack chestplate = equipment.getChestplate();
        if (chestplate == null) return;

        // 性能优化: 使用 PerformanceUtils 而非直接调用 getEnchantmentLevel
        int level = PerformanceUtils.getEnchantLevel(chestplate, fortitude);
        if (level <= 0) return;

        double remainingHealth = entity.getHealth() - event.getFinalDamage();
        double thresholdHealth = level * config.getHeartsThresholdPerLevel() * 2.0;
        if (remainingHealth > thresholdHealth) return;

        // 性能优化: 使用工具方法安全获取 PDC
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(entity);
        if (pdc == null) return;

        // 性能优化: 使用 PerformanceUtils 的冷却检查 (nanoTime)
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;

        // 性能优化: 使用 PerformanceUtils 设置冷却
        PerformanceUtils.setCooldown(pdc, key);

        // 性能优化: 使用 PerformanceUtils 计算持续时间
        int regenTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getRegenSecondsPerLevel(), level);
        int resistTicks = PerformanceUtils.calculateDurationTicksPerLevel(config.getResistanceSecondsPerLevel(), level);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenTicks, 0, false, false, true));
        entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, resistTicks, 0, false, false, true));
    }
}
