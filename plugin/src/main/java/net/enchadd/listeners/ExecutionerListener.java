package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ExecutionerEnchant;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;


public class ExecutionerListener implements Listener {

    private final Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment executioner = registry.get(ExecutionerEnchant.KEY);

    // 性能优化: 缓存配置对象
    private final ExecutionerEnchant config;

    public ExecutionerListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(ExecutionerEnchant.KEY);
        this.config = (enchantObj instanceof ExecutionerEnchant) ? (ExecutionerEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExecutionerDamage(EntityDamageByEntityEvent event) {
        if (executioner == null || config == null) return;

        // 性能优化: 添加 DamageSource null 检查
        if (event.getDamageSource() == null) return;

        Entity damager = event.getDamageSource().getCausingEntity();
        if (damager == null) return;
        if (!damager.equals(event.getDamageSource().getDirectEntity())) return;
        if (!(damager instanceof LivingEntity damagerEntity)) return;

        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment damagerEquipment = net.enchadd.utils.PerformanceUtils.getEquipmentSafe(damagerEntity);
        if (damagerEquipment == null) return;

        // 性能优化: 使用 PerformanceUtils 获取附魔等级总和
        int level = net.enchadd.utils.PerformanceUtils.getSumOfEnchantLevels(damagerEquipment, executioner);
        if (level == 0) return;

        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity livingEntity)) return;

        AttributeInstance maxHealthAttribute = livingEntity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) return;
        double targetMaxHealth = maxHealthAttribute.getValue();

        // 性能优化: 使用 PerformanceUtils 安全除法，防止除以零
        double targetHealthPercentage = net.enchadd.utils.PerformanceUtils.safeDivide(
            livingEntity.getHealth(), targetMaxHealth, 1.0);

        if (targetHealthPercentage < config.getMaxDamageHpThreshold()) {
            double damageMultiplier = 1 + (config.getDamageMultiplierPerLevel() * level);
            event.setDamage(event.getDamage() * damageMultiplier);
        }
    }

}

