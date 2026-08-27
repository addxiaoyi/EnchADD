package net.enchadd.listeners;

import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.enchants.InsomniaEnchant;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EntityEquipment;

import net.enchadd.utils.PerformanceUtils;

public class InsomniaListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment insomnia = registry.get(InsomniaEnchant.KEY);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerSufferingFromInsomnia(PlayerDeepSleepEvent event) {
        if (insomnia == null) return;
        
        Player player = event.getPlayer();
        
        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment damagerEquipment = PerformanceUtils.getEquipmentSafe(player);
        if (damagerEquipment == null) return;
        
        // 性能优化: 使用 PerformanceUtils 获取附魔等级总和
        int level = PerformanceUtils.getSumOfEnchantLevels(damagerEquipment, insomnia);
        if (level == 0) return;
        
        event.setCancelled(true);
    }

}
