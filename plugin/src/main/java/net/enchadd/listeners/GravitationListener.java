package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.GravitationEnchant;
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

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class GravitationListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(GravitationEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final GravitationEnchant config;

    public GravitationListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(GravitationEnchant.KEY);
        this.config = (enchantObj instanceof GravitationEnchant) ? (GravitationEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFallDamage(EntityDamageEvent event) {
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.FALL) return;
        
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (enchant == null || config == null) return;
        
        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;
        
        ItemStack boots = equipment.getBoots();
        if (boots == null) return;
        
        // 性能优化: 使用 PerformanceUtils 而非直接调用 EnchantCache
        int level = PerformanceUtils.getEnchantLevel(boots, enchant);
        if (level <= 0) return;
        
        double base = event.getDamage();
        if (base <= 0.0) return;
        
        double perLevel = config.getExtraFallDamagePerLevel();
        if (perLevel <= 0.0) return;
        
        double bonus = perLevel * level;
        double maxBonus = config.getMaxFallDamageMultiplier();
        if (maxBonus > 0.0 && bonus > maxBonus) {
            bonus = maxBonus;
        }
        if (bonus <= 0.0) return;
        
        double scale = 1.0 + bonus;
        if (scale <= 1.0) return;
        
        event.setDamage(base * Math.min(3.0, Math.max(0.0, scale)));
    }
}

