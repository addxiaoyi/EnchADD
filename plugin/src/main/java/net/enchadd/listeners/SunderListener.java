package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.SunderEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

@SuppressWarnings("UnstableApiUsage")
public class SunderListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(SunderEnchant.KEY);
    private final NamespacedKey key = PerformanceUtils.namespacedKey(SunderEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final SunderEnchant config;

    public SunderListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(SunderEnchant.KEY);
        this.config = (enchantObj instanceof SunderEnchant) ? (SunderEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (enchant == null || config == null) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        
        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;
        
        ItemStack mainHand = equipment.getItemInMainHand();
        // 性能优化: 使用 PerformanceUtils 而非直接调用 EnchantCache
        int level = PerformanceUtils.getEnchantLevel(mainHand, enchant);
        if (level <= 0) return;
        
        if (!PerformanceUtils.isEntityValid(target)) return;
        AttributeInstance armorAttr = target.getAttribute(Attribute.ARMOR);
        if (armorAttr == null) return;
        double armor = armorAttr.getValue();
        if (armor <= 0) return;
        
        // 性能优化: 使用工具方法安全获取 PDC
        PersistentDataContainer pdc = PerformanceUtils.getPDCSafe(player);
        if (pdc == null) return;
        
        // 性能优化: 使用 PerformanceUtils 的冷却检查 (nanoTime)
        if (PerformanceUtils.isOnCooldown(pdc, key, config.getCooldownTicks())) return;
        
        double chance = Math.min(config.getMaxTriggerChance(), config.getTriggerChance() * level);
        // 性能优化: 使用 PerformanceUtils 的概率检查
        if (!PerformanceUtils.rollChance(chance)) return;
        
        // 性能优化: 使用 PerformanceUtils.clamp 限制值范围
        double bonus = armor * config.getBonusPerArmorPointPerLevel() * level;
        bonus = PerformanceUtils.clamp(bonus, 0.0, config.getMaxBonusMultiplier());
        if (bonus <= 0) return;
        
        double damage = event.getDamage();
        double newDamage = damage * (1.0 + bonus);
        event.setDamage(newDamage);
        
        // 性能优化: 使用 PerformanceUtils 设置冷却
        PerformanceUtils.setCooldown(pdc, key);
    }
}
