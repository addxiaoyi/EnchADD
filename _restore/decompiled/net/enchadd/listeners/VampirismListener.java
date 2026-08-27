/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.registry.RegistryAccess
 *  io.papermc.paper.registry.RegistryKey
 *  io.papermc.paper.threadedregions.scheduler.ScheduledTask
 *  org.bukkit.Bukkit
 *  org.bukkit.Location
 *  org.bukkit.Registry
 *  org.bukkit.block.Block
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.PlayerItemHeldEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerRespawnEvent
 *  org.bukkit.event.player.PlayerSwapHandItemsEvent
 *  org.bukkit.inventory.EntityEquipment
 *  org.bukkit.plugin.Plugin
 */
package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.enchadd.EnchADD;
import net.enchadd.enchants.VampirismEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.plugin.Plugin;

public class VampirismListener
implements Listener {
    private static final int DIRECT_SUNLIGHT_LEVEL = 15;
    private static final int CLIENT_FIRE_RESET_THRESHOLD = 20;
    private static final int MIN_VISIBLE_FIRE_TICKS = 25;
    private static final int FIRE_REFRESH_INTERVAL_TICKS = 20;
    private static final int RECONCILE_INTERVAL_TICKS = 100;
    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment vampirism = (Enchantment)this.registry.get(VampirismEnchant.KEY);
    private final Set<UUID> activeCandidates = ConcurrentHashMap.newKeySet();
    private final Plugin plugin = Bukkit.getPluginManager().getPlugin("EnchADD");
    private ScheduledTask vampirismTask;
    private ScheduledTask reconcileTask;

    public VampirismListener() {
        if (this.vampirism == null || this.plugin == null) {
            Bukkit.getLogger().warning("[EnchADD] Vampirism enchantment not found, listener disabled");
            return;
        }
        this.vampirismTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, task -> {
            for (UUID id : this.activeCandidates) {
                this.scheduleActiveTick(id);
            }
        }, 1L, 20L);
        this.reconcileTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, task -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.scheduleReconcile(player);
            }
        }, 100L, 100L);
    }

    private void scheduleActiveTick(UUID id) {
        Player player = Bukkit.getPlayer((UUID)id);
        if (player == null || !this.isRuntimeEnabled()) {
            this.activeCandidates.remove(id);
            return;
        }
        player.getScheduler().execute(this.plugin, () -> {
            if (!this.hasVampirismEnchant(player)) {
                this.activeCandidates.remove(id);
                return;
            }
            this.refreshSunBurn(player);
        }, () -> this.activeCandidates.remove(id), 1L);
    }

    private void scheduleReconcile(Player player) {
        if (player == null || !this.isRuntimeEnabled()) {
            return;
        }
        UUID id = player.getUniqueId();
        player.getScheduler().execute(this.plugin, () -> this.reconcileTracking(player), () -> this.activeCandidates.remove(id), 1L);
    }

    private boolean hasVampirismEnchant(Player player) {
        if (!this.isRuntimeEnabled()) {
            return false;
        }
        EntityEquipment equipment = PerformanceUtils.getEquipmentSafe((LivingEntity)player);
        if (equipment == null) {
            return false;
        }
        return EnchADD.getSumOfEnchantLevels(equipment, this.vampirism) > 0;
    }

    private boolean isRuntimeEnabled() {
        return this.plugin != null && this.vampirism != null;
    }

    private void reconcileTracking(Player player) {
        if (!this.isRuntimeEnabled()) {
            return;
        }
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        if (!player.isOnline() || player.isDead()) {
            this.activeCandidates.remove(id);
            return;
        }
        if (this.hasVampirismEnchant(player)) {
            this.activeCandidates.add(id);
            return;
        }
        this.activeCandidates.remove(id);
    }

    private void refreshSunBurn(Player player) {
        if (player == null || !player.isOnline() || player.isDead()) {
            return;
        }
        Location abovePlayer = player.getLocation().add(0.0, player.getEyeHeight() + 0.5, 0.0);
        Block block = player.getWorld().getBlockAt(abovePlayer);
        if (block.getLightFromSky() < 15) {
            return;
        }
        int fireTicks = player.getFireTicks();
        if (fireTicks < 20) {
            fireTicks = 25;
        }
        player.setFireTicks(Math.max(fireTicks, player.getMaxFireTicks()));
    }

    public void cleanup() {
        if (this.vampirismTask != null && !this.vampirismTask.isCancelled()) {
            this.vampirismTask.cancel();
        }
        if (this.reconcileTask != null && !this.reconcileTask.isCancelled()) {
            this.reconcileTask.cancel();
        }
        this.activeCandidates.clear();
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.reconcileTracking(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.activeCandidates.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        this.scheduleReconcile(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        this.reconcileTracking(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerSwapHands(PlayerSwapHandItemsEvent event) {
        this.reconcileTracking(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        this.scheduleReconcile(player);
    }
}
