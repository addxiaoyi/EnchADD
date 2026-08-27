package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.PanicEnchant;
import net.enchadd.events.PlayerPanicEvent;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class PanicListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment panic = registry.get(PanicEnchant.KEY);
    
    // 性能优化: 缓存配置对象
    private final PanicEnchant config;

    public PanicListener() {
        // 性能优化: 在构造时缓存配置，避免每次事件都查询
        Object enchantObj = EnchADDConfig.ENCHANTS.get(PanicEnchant.KEY);
        this.config = (enchantObj instanceof PanicEnchant) ? (PanicEnchant) enchantObj : null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerPanic(EntityDamageEvent event) {
        if (panic == null || config == null) return;

        if (!(event.getEntity() instanceof Player player)) return;

        // 性能优化: 使用工具方法安全获取装备
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe(player);
        if (equipment == null) return;

        // 性能优化: 使用 PerformanceUtils 获取最高附魔等级
        int level = PerformanceUtils.getHighestEnchantLevel(equipment, panic);
        if (level == 0) return;
        
        double chance = level * config.getPanicChancePerLevel();

        // 性能优化: 使用 PerformanceUtils 的概率检查
        if (!PerformanceUtils.rollChance(chance)) return;

        PlayerInventory inventory = player.getInventory();
        // 性能优化: 添加 inventory null 检查
        if (inventory == null) return;
        
        // 性能优化: 直接操作数组，避免创建中间集合 (Stream + subList + ArrayList)
        ItemStack[] contents = inventory.getContents();
        if (contents == null || contents.length < 9) return;

        // 使用预分配的 ArrayList
        List<ItemStack> hotbarItems = PerformanceUtils.newArrayListWithCapacity(9);
        for (int i = 0; i < 9; i++) {
            hotbarItems.add(contents[i]);
        }

        // 性能优化: 使用 Fisher-Yates 洗牌算法，避免 Collections.shuffle
        for (int i = hotbarItems.size() - 1; i > 0; i--) {
            int j = PerformanceUtils.getRandom().nextInt(i + 1);
            ItemStack temp = hotbarItems.get(i);
            hotbarItems.set(i, hotbarItems.get(j));
            hotbarItems.set(j, temp);
        }

        PlayerPanicEvent playerPanicEvent = new PlayerPanicEvent(player, hotbarItems);
        Bukkit.getPluginManager().callEvent(playerPanicEvent);
        if (playerPanicEvent.isCancelled()) return;

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, playerPanicEvent.getScrambledItems().get(i));
        }
    }

}
