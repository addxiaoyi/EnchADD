package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.enchants.SmeltingEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SmeltingListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment smelting = registry.get(SmeltingEnchant.KEY);
    // 仅以掉落物类型作为缓存键，减少 ItemStack 对象抖动开销
    private final Map<Material, ItemStack> smeltingCache = new ConcurrentHashMap<>();
    // 负缓存：记录不可熔炼类型，避免重复全量遍历配方
    private final Set<Material> nonSmeltableCache = ConcurrentHashMap.newKeySet();
    private static final int MAX_CACHE_SIZE = 1000;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSmeltingEnchantSmelt(BlockDropItemEvent event) {
        if (smelting == null) return;
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (net.enchadd.utils.EnchantCache.getLevel(tool, smelting) <= 0) return;

        BlockState block = event.getBlockState();
        if (block instanceof BlockInventoryHolder) return;

        for (Item item : event.getItems()) {
            if (item == null) continue;
            int amount = item.getItemStack().getAmount();
            ItemStack smeltedItem = getSmeltedItem(item.getItemStack());
            if (smeltedItem == null) continue;
            item.setItemStack(smeltedItem.asQuantity(amount));
        }
    }

    /**
     * Gets the smelted item from the given item stack. If item stack is not smeltable, returns the item stack itself.
     */
    private ItemStack getSmeltedItem(@NotNull ItemStack itemStack) {
        Material type = itemStack.getType();
        ItemStack cached = smeltingCache.get(type);
        if (cached != null) return cached;
        if (nonSmeltableCache.contains(type)) return null;

        // 修复: 添加缓存大小限制
        if (smeltingCache.size() > MAX_CACHE_SIZE) {
            smeltingCache.clear();
            nonSmeltableCache.clear();
        }

        ItemStack singleItem = itemStack.asOne();
        for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            Recipe recipe = it.next();
            if (!(recipe instanceof FurnaceRecipe furnaceRecipe)) continue;
            if (!furnaceRecipe.getInputChoice().test(singleItem)) continue;
            ItemStack result = furnaceRecipe.getResult().asOne();
            smeltingCache.put(type, result);
            return result;
        }
        nonSmeltableCache.add(type);
        return null;
    }

}
