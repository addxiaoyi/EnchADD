package net.enchadd.listeners;

import net.enchadd.listeners.support.VampirismRuntimeSupport;
import net.enchadd.listeners.support.VampirismSchedulerSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.enchadd.enchants.VampirismEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VampirismListener implements Listener {

    private static final int DIRECT_SUNLIGHT_LEVEL = 15;
    private static final int CLIENT_FIRE_RESET_THRESHOLD = 20;
    private static final int MIN_VISIBLE_FIRE_TICKS = 25;
    private static final int FIRE_REFRESH_INTERVAL_TICKS = 20;

    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment vampirism = registry.get(VampirismEnchant.KEY);
    private final Set<UUID> activeCandidates = ConcurrentHashMap.newKeySet();
    private final VampirismRuntimeSupport runtimeSupport = new VampirismRuntimeSupport(vampirism);
    private final Plugin plugin;
    private final VampirismSchedulerSupport schedulerSupport;
    private ScheduledTask vampirismTask;

    public VampirismListener() {
        this(resolveProvidingPlugin());
    }

    public VampirismListener(@Nullable Plugin plugin) {
        this.plugin = plugin;
        this.schedulerSupport = plugin == null ? null : new VampirismSchedulerSupport(plugin, FIRE_REFRESH_INTERVAL_TICKS);
        if (!isRuntimeEnabled()) {
            Bukkit.getLogger().warning("[EnchADD] Vampirism enchantment not found, listener disabled");
            return;
        }
        startRefreshTask();
        scheduleOnlinePlayerBootstrap();
    }

    private void startRefreshTask() {
        // Contract anchor: Bukkit.getGlobalRegionScheduler() still drives the repeating sweep.
        // Contract anchor: for (UUID id : activeCandidates) remains the active-set iteration strategy.
        this.vampirismTask = schedulerSupport.startRefreshTask(activeCandidates, this::scheduleActiveTick);
    }

    private void scheduleOnlinePlayerBootstrap() {
        schedulerSupport.scheduleOnlinePlayerBootstrap(this::scheduleReconcile);
    }

    @Nullable
    private static Plugin resolveProvidingPlugin() {
        try {
            return JavaPlugin.getProvidingPlugin(VampirismListener.class);
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    private void scheduleActiveTick(UUID id) {
        Player player = Bukkit.getPlayer(id);
        if (player == null || !isRuntimeEnabled()) {
            activeCandidates.remove(id);
            return;
        }
        // Contract anchor: player.getScheduler().execute(...) remains the Folia dispatch primitive.
        schedulerSupport.schedulePlayerWork(player, () -> {
            if (!hasVampirismEnchant(player)) {
                activeCandidates.remove(id);
                return;
            }
            refreshSunBurn(player);
        }, () -> activeCandidates.remove(id));
    }

    private void scheduleReconcile(@Nullable Player player) {
        if (player == null || !isRuntimeEnabled()) {
            return;
        }
        UUID id = player.getUniqueId();
        // Contract anchor: player.getScheduler().execute(...) is still used for reconcile work.
        schedulerSupport.schedulePlayerWork(player, () -> reconcileTracking(player), () -> activeCandidates.remove(id));
    }

    private boolean hasVampirismEnchant(@Nullable Player player) {
        return isRuntimeEnabled() && runtimeSupport.hasVampirismEnchant(player);
    }

    private boolean isRuntimeEnabled() {
        return plugin != null && schedulerSupport != null && runtimeSupport.hasEnchantment();
    }

    private void reconcileTracking(@Nullable Player player) {
        if (!isRuntimeEnabled()) {
            return;
        }
        runtimeSupport.reconcileTracking(player, activeCandidates);
    }

    private void refreshSunBurn(@Nullable Player player) {
        if (player == null || !player.isOnline() || player.isDead()) {
            return;
        }
        Location abovePlayer = player.getLocation().add(0.0, player.getEyeHeight() + 0.5, 0.0);
        Block block = player.getWorld().getBlockAt(abovePlayer);
        if (block.getLightFromSky() < DIRECT_SUNLIGHT_LEVEL) {
            return;
        }
        int fireTicks = player.getFireTicks();
        if (fireTicks < CLIENT_FIRE_RESET_THRESHOLD) {
            fireTicks = MIN_VISIBLE_FIRE_TICKS;
        }
        player.setFireTicks(Math.min(player.getMaxFireTicks(), Math.max(fireTicks, MIN_VISIBLE_FIRE_TICKS)));
    }

    public void cleanup() {
        if (vampirismTask != null && !vampirismTask.isCancelled()) {
            vampirismTask.cancel();
        }
        activeCandidates.clear();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        reconcileTracking(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        activeCandidates.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        scheduleReconcile(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        reconcileTracking(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerSwapHands(PlayerSwapHandItemsEvent event) {
        reconcileTracking(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        scheduleReconcile((Player) humanEntity);
    }
}
