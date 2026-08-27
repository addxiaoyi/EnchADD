package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.enchants.ReplantingEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ReplantingListener implements Listener {

    private final Registry<Enchantment> enchantmentRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Tag<Material> cropTag = Tag.CROPS;
    private final Enchantment replanting = enchantmentRegistry.get(ReplantingEnchant.KEY);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onReplantingTool(BlockDropItemEvent event) {
        if (replanting == null) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (PerformanceUtils.getEnchantLevel(tool, replanting) <= 0) return;

        BlockState blockState = event.getBlockState();
        if (!cropTag.isTagged(blockState.getType())) return;

        Material placementMaterial = blockState.getBlockData().getPlacementMaterial();
        if (placementMaterial == null || placementMaterial.isAir()) return;

        boolean shouldReplant = player.getGameMode() == GameMode.CREATIVE;
        if (!shouldReplant) {
            shouldReplant = consumeSeedFromInventory(player.getInventory(), placementMaterial);
        }
        if (!shouldReplant) {
            shouldReplant = consumeSeedFromDrops(event, placementMaterial);
        }
        if (!shouldReplant) return;

        Block targetBlock = event.getBlock();
        targetBlock.setType(blockState.getType(), false);
    }

    private boolean consumeSeedFromInventory(PlayerInventory inventory, Material placementMaterial) {
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() != placementMaterial) continue;
            decrementStack(item);
            return true;
        }
        return false;
    }

    private boolean consumeSeedFromDrops(BlockDropItemEvent event, Material placementMaterial) {
        for (Item item : event.getItems()) {
            if (item == null) continue;
            ItemStack itemStack = item.getItemStack();
            if (itemStack == null || itemStack.getType() != placementMaterial) continue;
            decrementStack(itemStack);
            return true;
        }
        return false;
    }

    private void decrementStack(ItemStack itemStack) {
        int amount = itemStack.getAmount();
        if (amount <= 1) {
            itemStack.setAmount(0);
            return;
        }
        itemStack.setAmount(amount - 1);
    }
}
