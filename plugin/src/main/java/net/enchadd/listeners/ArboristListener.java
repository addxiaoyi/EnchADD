package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.ArboristEnchant;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.listeners.support.ArboristStripSupport;
import net.enchadd.listeners.support.ListenerDispatchGuard;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.Block;
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

public final class ArboristListener implements Listener {

    private static final Set<Material> AXES = Set.of(
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE,
            Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE
    );

    private final Enchantment arborist;
    private final ArboristStripSupport stripSupport;
    private final ListenerDispatchGuard syntheticInteractGuard = new ListenerDispatchGuard();

    public ArboristListener() {
        this(resolveEnchant(ArboristEnchant.KEY), resolveConfig());
    }

    public ArboristListener(@Nullable Enchantment arborist, @Nullable ArboristEnchant config) {
        this.arborist = arborist;
        this.stripSupport = config == null ? null : new ArboristStripSupport(config);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onArborist(PlayerInteractEvent event) {
        if (syntheticInteractGuard.isActive()) {
            return;
        }
        if (arborist == null || stripSupport == null) {
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
        if (clickedBlock == null) {
            return;
        }

        ItemStack tool = event.getItem();
        if (tool == null || !AXES.contains(tool.getType())) {
            return;
        }
        if (PerformanceUtils.getEnchantLevel(tool, arborist) <= 0) {
            return;
        }
        if (stripSupport.bypassWhenSneaking() && event.getPlayer().isSneaking()) {
            return;
        }

        Material clickedType = clickedBlock.getType();
        Material strippedType = stripSupport.resolveStrippedType(clickedType);
        if (strippedType == null) {
            return;
        }

        int extraBlocks = stripSupport.extraBlocks();
        if (extraBlocks <= 0) {
            return;
        }

        List<Block> targets = stripSupport.collectTargets(clickedBlock, clickedType);
        if (targets.isEmpty()) {
            return;
        }

        PluginManager pluginManager = event.getPlayer().getServer().getPluginManager();
        for (Block target : targets) {
            if (!callSyntheticInteract(pluginManager, event, tool, target)) {
                continue;
            }
            stripSupport.applyStrippedMaterial(target, strippedType);
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
                    originalEvent.getBlockFace(),
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

    private static @Nullable ArboristEnchant resolveConfig() {
        EnchADDEnchant enchant = EnchADDConfig.ENCHANTS.get(ArboristEnchant.KEY);
        return enchant instanceof ArboristEnchant arboristEnchant ? arboristEnchant : null;
    }
}
