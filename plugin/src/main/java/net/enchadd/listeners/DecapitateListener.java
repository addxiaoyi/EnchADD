package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.DecapitateEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class DecapitateListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(DecapitateEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final DecapitateEnchant config;

    public DecapitateListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(DecapitateEnchant.KEY);
        this.config = (enchantObj instanceof DecapitateEnchant) ? (DecapitateEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getDamageSource() == null || event.getDamageSource().isIndirect()) return;
        if (enchant == null || config == null) return;
        Entity killer = event.getDamageSource().getCausingEntity();
        if (!(killer instanceof LivingEntity living)) return;

        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(living);
        if (equipment == null) return;

        // 性能优化: 使用 PerformanceUtils 获取最高附魔等级
        int level = PerformanceUtils.getHighestEnchantLevel(equipment, enchant);
        if (level <= 0) return;

        for (ItemStack item : event.getDrops()) {
            if (item != null && item.getType() == Material.PLAYER_HEAD) {
                return;
            }
        }

        double chance = Math.min(0.3, level * config.getChancePerLevel());
        if (!PerformanceUtils.rollChance(chance)) return;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return;
        meta.setOwningPlayer(player);
        head.setItemMeta(meta);
        event.getDrops().add(head);
    }
}
