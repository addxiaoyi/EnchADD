package net.enchadd.listeners;

import net.enchadd.utils.EnchantCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

/**
 * 优化2：缓存一致性保障（Cache Invalidation）
 *
 * 统一监听器处理两类生命周期事件：
 *
 * 1. 物品修改后的缓存失效（InventoryClickEvent、PrepareAnvilEvent、PlayerItemDamageEvent）
 *    当玩家修改背包物品、使用铁砧时，淘汰该物品在 EnchantCache 中的条目，
 *    保证下次读取的等级数据是准确的。
 *
 */
public class LifecycleListener implements Listener {

    // ─── 缓存失效 ────────────────────────────────────────────────────────────────

    /**
     * 玩家在背包内移动/交换物品时，使相关物品的附魔缓存失效。
     * 此操作覆盖：堆叠、分拆、拖拽、shift+点击等所有操作。
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if (current != null && !current.getType().isAir()) {
            EnchantCache.invalidate(current);
        }
        if (cursor != null && !cursor.getType().isAir()) {
            EnchantCache.invalidate(cursor);
        }
    }

    /**
     * 铁砧合并/附魔书操作时，使结果物品的缓存失效。
     * 铁砧可以传递/覆盖附魔等级，必须强制刷新。
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && !result.getType().isAir()) {
            EnchantCache.invalidate(result);
        }
        // 同时失效输入格的两个物品，防止引用相同 ItemStack 实例
        ItemStack left = event.getInventory().getFirstItem();
        ItemStack right = event.getInventory().getSecondItem();
        if (left != null) EnchantCache.invalidate(left);
        if (right != null) EnchantCache.invalidate(right);
    }

    /**
     * 物品耐久损耗时，也应淘汰缓存。
     * （等级本身不变，但 ItemStack 内部状态改变后 hashCode 可能变）
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemDamage(org.bukkit.event.player.PlayerItemDamageEvent event) {
        EnchantCache.invalidate(event.getItem());
    }

}
