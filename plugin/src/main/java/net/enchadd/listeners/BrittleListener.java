package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.BrittleEnchant;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class BrittleListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(BrittleEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final BrittleEnchant config;

    public BrittleListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(BrittleEnchant.KEY);
        this.config = (enchantObj instanceof BrittleEnchant) ? (BrittleEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFallDamage(@NotNull EntityDamageEvent event) {
        if (enchant == null || config == null) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        
        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;
        
        ItemStack leggings = equipment.getLeggings();
        if (leggings == null) return;
        
        // 性能优化: 使用 PerformanceUtils 而非直接调用 EnchantCache
        int level = PerformanceUtils.getEnchantLevel(leggings, enchant);
        if (level <= 0) return;
        
        double base = event.getDamage();
        if (base <= 0.0) return;
        
        double perLevel = config.getExtraMultiplierPerLevel();
        if (perLevel <= 0.0) return;
        
        double bonus = perLevel * level;
        double maxBonus = config.getMaxExtraMultiplier();
        if (maxBonus > 0.0 && bonus > maxBonus) {
            bonus = maxBonus;
        }
        if (bonus <= 0.0) return;
        
        double scale = 1.0 + bonus;
        if (scale <= 1.0) return;
        
        event.setDamage(base * scale);
    }
}

