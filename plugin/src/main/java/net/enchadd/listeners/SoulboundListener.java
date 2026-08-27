package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.enchants.SoulboundEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SoulboundListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment soulbound = registry.get(SoulboundEnchant.KEY);

    public SoulboundListener() {
        // 性能优化: 在构造函数中验证附魔是否存在
        if (soulbound == null) {
            Bukkit.getLogger().warning("[EnchADD] Soulbound enchantment not found, listener disabled");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSoulboundEnchantDeath(PlayerDeathEvent event) {
        if (soulbound == null) return;

        ItemStack[] contents = event.getPlayer().getInventory().getContents();
        if (contents == null || contents.length == 0) return;

        List<ItemStack> itemsToKeep = new ArrayList<>(contents.length);
        for (ItemStack itemStack : contents) {
            if (itemStack == null || itemStack.getType().isAir()) continue;
            if (PerformanceUtils.getEnchantLevel(itemStack, soulbound) <= 0) continue;
            itemsToKeep.add(itemStack.clone());
        }

        if (itemsToKeep.isEmpty()) return;

        event.getItemsToKeep().addAll(itemsToKeep);
        // 移除物品掉落，同时从已保留列表中查找以避免重复调用 getEnchantLevel
        event.getDrops().removeIf(drop -> PerformanceUtils.getEnchantLevel(drop, soulbound) > 0);
    }

}
