package net.enchadd.listeners;

import net.enchadd.listeners.support.IrrigationHydrationSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.IrrigationEnchant;
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

public final class IrrigationListener implements Listener {

    private static final Set<Material> HOES = Set.of(
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE,
            Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE
    );

    private final Enchantment irrigation;
    private final IrrigationHydrationSupport hydrationSupport;
    private final ListenerDispatchGuard syntheticInteractGuard = new ListenerDispatchGuard();

    public IrrigationListener() {
        this(resolveEnchant(IrrigationEnchant.KEY), resolveConfig());
    }

    public IrrigationListener(@Nullable Enchantment irrigation, @Nullable IrrigationEnchant config) {
        this.irrigation = irrigation;
        this.hydrationSupport = config == null ? null : new IrrigationHydrationSupport(config);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onIrrigation(PlayerInteractEvent event) {
        if (syntheticInteractGuard.isActive()) {
            return;
        }
        if (irrigation == null || hydrationSupport == null) {
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

        ItemStack tool = event.getItem();
        if (tool == null || !HOES.contains(tool.getType())) {
            return;
        }
        if (PerformanceUtils.getEnchantLevel(tool, irrigation) <= 0) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        Block center = hydrationSupport.resolveFarmlandTarget(clickedBlock);
        if (center == null) {
            return;
        }

        int radius = hydrationSupport.effectiveRadius(event.getPlayer().isSneaking());

        List<Block> targets = hydrationSupport.collectTargets(center, radius);
        if (targets.isEmpty()) {
            return;
        }

        PluginManager pluginManager = event.getPlayer().getServer().getPluginManager();
        for (Block target : targets) {
            if (!callSyntheticInteract(pluginManager, event, tool, target)) {
                continue;
            }
            hydrationSupport.hydrate(target);
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

    private static @Nullable IrrigationEnchant resolveConfig() {
        EnchADDEnchant enchant = EnchADDConfig.ENCHANTS.get(IrrigationEnchant.KEY);
        return enchant instanceof IrrigationEnchant irrigationEnchant ? irrigationEnchant : null;
    }
}
