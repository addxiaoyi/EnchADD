package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.InsightEnchant;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import net.enchadd.utils.PerformanceUtils;

@SuppressWarnings("UnstableApiUsage")
public class InsightListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(InsightEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final InsightEnchant config;

    public InsightListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(InsightEnchant.KEY);
        this.config = (enchantObj instanceof InsightEnchant) ? (InsightEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (enchant == null || config == null) return;
        
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) return;
        
        // 性能优化: 使用 PerformanceUtils 而非直接调用 EnchantCache
        int level = PerformanceUtils.getEnchantLevel(tool, enchant);
        if (level <= 0) return;
        
        int exp = event.getExpToDrop();
        if (exp <= 0) return;
        
        double bonusMultiplier = config.getXpBonusPerLevel() * level;
        if (bonusMultiplier <= 0) return;
        
        int bonus = (int) Math.round(exp * bonusMultiplier);
        if (bonus <= 0) return;
        
        event.setExpToDrop(exp + bonus);
    }
}
