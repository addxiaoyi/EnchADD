package net.enchadd.listeners;

import net.enchadd.listeners.support.TrailblazerPathSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.TrailblazerEnchant;
import net.enchadd.listeners.support.ListenerDispatchGuard;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public final class TrailblazerListener implements Listener {

    private static final Set<Material> SHOVELS = Set.of(
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL
    );

    private final Enchantment trailblazer;
    private final TrailblazerPathSupport pathSupport;
    private final ListenerDispatchGuard syntheticInteractGuard = new ListenerDispatchGuard();

    public TrailblazerListener() {
        this(resolveEnchant(TrailblazerEnchant.KEY), resolveConfig());
    }

    public TrailblazerListener(@Nullable Enchantment trailblazer, @Nullable TrailblazerEnchant config) {
        this.trailblazer = trailblazer;
        this.pathSupport = config == null ? null : new TrailblazerPathSupport(config);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTrailblazer(PlayerInteractEvent event) {
        if (syntheticInteractGuard.isActive()) {
            return;
        }
        if (trailblazer == null || pathSupport == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || event.getBlockFace() != BlockFace.UP) {
            return;
        }
        if (!pathSupport.canFlatten(clickedBlock)) {
            return;
        }

        ItemStack tool = event.getItem();
        if (tool == null || !SHOVELS.contains(tool.getType())) {
            return;
        }
        if (PerformanceUtils.getEnchantLevel(tool, trailblazer) <= 0) {
            return;
        }
        if (pathSupport.bypassWhenSneaking() && event.getPlayer().isSneaking()) {
            return;
        }

        int radius = pathSupport.radius();
        if (radius <= 0) {
            return;
        }

        PluginManager pluginManager = event.getPlayer().getServer().getPluginManager();
        List<Block> targets = pathSupport.collectTargets(clickedBlock);
        for (Block target : targets) {
            if (!callSyntheticInteract(pluginManager, event, tool, target)) {
                continue;
            }
            pathSupport.flatten(target);
        }
    }
    
    private boolean callSyntheticInteract(PluginManager pluginManager,
                                          PlayerInteractEvent originalEvent,
                                          ItemStack tool,
                                          Block target) {
        PlayerInteractEvent syntheticEvent = syntheticInteractGuard.execute(() -> {
            PlayerInteractEvent dispatched = new PlayerInteractEvent(
                    originalEvent.getPlayer(),
                    Action.RIGHT_CLICK_BLOCK,
                    tool,
                    target,
                    BlockFace.UP,
                    EquipmentSlot.HAND
            );
            pluginManager.callEvent(dispatched);
            return dispatched;
        });
        return !syntheticEvent.isCancelled()
                && syntheticEvent.useInteractedBlock() != Event.Result.DENY
                && syntheticEvent.useItemInHand() != Event.Result.DENY;
    }

    private static @Nullable Enchantment resolveEnchant(net.kyori.adventure.key.Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(key);
    }

    private static @Nullable TrailblazerEnchant resolveConfig() {
        EnchADDEnchant enchant = EnchADDConfig.ENCHANTS.get(TrailblazerEnchant.KEY);
        return enchant instanceof TrailblazerEnchant trailblazerEnchant ? trailblazerEnchant : null;
    }
}
