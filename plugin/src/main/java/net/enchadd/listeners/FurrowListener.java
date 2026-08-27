package net.enchadd.listeners;

import net.enchadd.listeners.support.FurrowHarvestSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.FurrowEnchant;
import net.enchadd.enchants.ReplantingEnchant;
import net.enchadd.listeners.support.ListenerDispatchGuard;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FurrowListener implements Listener {

    private final Enchantment furrow;
    private final Enchantment replanting;
    private final FurrowHarvestSupport harvestSupport;
    private final ListenerDispatchGuard syntheticBreakGuard = new ListenerDispatchGuard();

    public FurrowListener() {
        this(resolveEnchant(FurrowEnchant.KEY), resolveEnchant(ReplantingEnchant.KEY), resolveConfig());
    }

    public FurrowListener(@Nullable Enchantment furrow,
                          @Nullable Enchantment replanting,
                          @Nullable FurrowEnchant config) {
        this.furrow = furrow;
        this.replanting = replanting;
        this.harvestSupport = config == null ? null : new FurrowHarvestSupport(Tag.CROPS, config);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCropBreak(BlockBreakEvent event) {
        if (syntheticBreakGuard.isActive()) {
            return;
        }
        if (furrow == null || harvestSupport == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!PerformanceUtils.isPlayerValid(player)) {
            return;
        }
        if (harvestSupport.bypassWhenSneaking() && player.isSneaking()) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (PerformanceUtils.getEnchantLevel(tool, furrow) <= 0) {
            return;
        }
        if (!harvestSupport.isHarvestable(event.getBlock())) {
            return;
        }

        List<Block> targets = harvestSupport.collectTargets(event.getBlock());
        if (targets.isEmpty()) {
            return;
        }

        boolean shouldReplant = replanting != null && PerformanceUtils.getEnchantLevel(tool, replanting) > 0;
        for (Block target : targets) {
            BlockBreakEvent syntheticEvent = dispatchSyntheticBreak(player, target);
            if (syntheticEvent.isCancelled()) {
                continue;
            }
            harvestSupport.harvest(target, player, tool, syntheticEvent.isDropItems(), shouldReplant);
        }
    }

    private @NotNull BlockBreakEvent dispatchSyntheticBreak(@NotNull Player player, @NotNull Block target) {
        return syntheticBreakGuard.execute(() -> {
            BlockBreakEvent event = new BlockBreakEvent(target, player);
            Bukkit.getPluginManager().callEvent(event);
            return event;
        });
    }

    private static @Nullable Enchantment resolveEnchant(net.kyori.adventure.key.Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(key);
    }

    private static @Nullable FurrowEnchant resolveConfig() {
        EnchADDEnchant enchant = EnchADDConfig.ENCHANTS.get(FurrowEnchant.KEY);
        return enchant instanceof FurrowEnchant furrowEnchant ? furrowEnchant : null;
    }
}
