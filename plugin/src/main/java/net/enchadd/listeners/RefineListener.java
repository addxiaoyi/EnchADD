package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.RefineEnchant;
import net.enchadd.listeners.support.HopperSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class RefineListener implements Listener {

    private static final Set<Material> ORE_DROPS = EnumSet.of(
            Material.RAW_IRON,
            Material.RAW_COPPER,
            Material.RAW_GOLD,
            Material.COAL,
            Material.REDSTONE,
            Material.LAPIS_LAZULI,
            Material.DIAMOND,
            Material.EMERALD,
            Material.NETHERITE_SCRAP
    );

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment enchant = registry.get(RefineEnchant.KEY);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockDrop(BlockDropItemEvent event) {
        if (enchant == null) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) return;
        if (tool.getEnchantmentLevel(Enchantment.SILK_TOUCH) > 0) return;
        int level = net.enchadd.utils.EnchantCache.getLevel(tool, enchant);
        if (level <= 0) return;
        if (!(EnchADDConfig.ENCHANTS.get(RefineEnchant.KEY) instanceof RefineEnchant refineEnchant)) return;
        double chance = refineEnchant.getExtraDropChancePerLevel() * level;
        if (!Double.isFinite(chance) || chance <= 0.0d) return;
        double clampedChance = Math.min(0.5, chance);
        for (Item item : event.getItems()) {
            ItemStack stack = item.getItemStack();
            if (!shouldProcessStack(stack, clampedChance)) {
                continue;
            }
            HopperSupport.atomic_increment(stack);
            item.setItemStack(stack);
        }
    }

    private boolean shouldProcessStack(ItemStack stack, double clampedChance) {
        if (!ORE_DROPS.contains(stack.getType())) return false;
        if (!PerformanceUtils.rollChance(clampedChance)) return false;
        int amount = stack.getAmount();
        return amount > 0;
    }
}
