package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.TelepathyEnchant;
import net.enchadd.listeners.support.TelepathyDropSupport;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class TelepathyListener implements Listener {

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment telepathy = registry.get(TelepathyEnchant.KEY);
    private final Plugin plugin;
    private final TelepathyEnchant config;
    private final TelepathyDropSupport dropSupport;

    public TelepathyListener() {
        this(resolvePlugin());
    }

    public TelepathyListener(@Nullable Plugin plugin) {
        this.plugin = plugin;
        EnchADDEnchant enchantObj = EnchADDConfig.ENCHANTS.get(TelepathyEnchant.KEY);
        this.config = enchantObj instanceof TelepathyEnchant ? (TelepathyEnchant) enchantObj : null;
        this.dropSupport = this.config == null ? null : new TelepathyDropSupport(this.config);
    }

    @Nullable
    private static Plugin resolvePlugin() {
        try {
            return JavaPlugin.getProvidingPlugin(TelepathyListener.class);
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTelepathyTool(BlockDropItemEvent event) {
        if (telepathy == null || config == null || dropSupport == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!PerformanceUtils.isPlayerValid(player)) {
            return;
        }
        if (player.getInventory() == null) {
            return;
        }
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (PerformanceUtils.getEnchantLevel(tool, telepathy) <= 0) {
            return;
        }
        var droppedItems = dropSupport.collectDroppedItems(player, event.getItems());
        if (droppedItems.isEmpty()) {
            return;
        }
        Plugin runtimePlugin = plugin;
        if (runtimePlugin == null) {
            return;
        }
        player.getScheduler().execute(runtimePlugin, () -> {
            dropSupport.teleportCollectedItems(player, droppedItems);
        }, null, 1L);
    }
}
