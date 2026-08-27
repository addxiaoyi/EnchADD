/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.destroystokyo.paper.event.player.PlayerJumpEvent
 *  io.papermc.paper.registry.RegistryAccess
 *  io.papermc.paper.registry.RegistryKey
 *  io.papermc.paper.threadedregions.scheduler.ScheduledTask
 *  org.bukkit.Bukkit
 *  org.bukkit.Registry
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.HumanEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.player.PlayerInteractEvent
 *  org.bukkit.event.player.PlayerItemHeldEvent
 *  org.bukkit.event.player.PlayerJoinEvent
 *  org.bukkit.event.player.PlayerMoveEvent
 *  org.bukkit.event.player.PlayerQuitEvent
 *  org.bukkit.event.player.PlayerSwapHandItemsEvent
 *  org.bukkit.event.player.PlayerToggleSneakEvent
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package net.enchadd.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.enchadd.EnchADD;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.CloakingEnchant;
import net.enchadd.enchants.EnchADDEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CloakingListener
implements Listener {
    private static final int CLOAK_REFRESH_TICKS = 3;
    private static final int TASK_INTERVAL_TICKS = 1;
    private static final int RECONCILE_INTERVAL_TICKS = 40;
    private final Set<UUID> activeCandidates = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, Long> ticksSinceLastMovement = new ConcurrentHashMap();
    private final Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment cloaking = (Enchantment)this.registry.get(CloakingEnchant.KEY);
    private final CloakingEnchant cloakingEnchant;
    private final PotionEffect cloakingEffect = new PotionEffect(PotionEffectType.INVISIBILITY, 3, 0, false, false, false);
    private final Plugin plugin;
    private ScheduledTask cloakingTask;
    private ScheduledTask reconcileTask;

    public CloakingListener() {
        CloakingEnchant enchant;
        EnchADDEnchant enchantObj = EnchADDConfig.ENCHANTS.get(CloakingEnchant.KEY);
        this.cloakingEnchant = enchantObj instanceof CloakingEnchant ? (enchant = (CloakingEnchant)enchantObj) : null;
        this.plugin = Bukkit.getPluginManager().getPlugin("EnchADD");
        if (this.cloaking == null || this.cloakingEnchant == null || this.plugin == null) {
            Bukkit.getLogger().warning("[EnchADD] Cloaking enchantment not found, listener disabled");
            return;
        }
        this.cloakingTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, task -> {
            for (UUID id : this.activeCandidates) {
                this.scheduleCandidateTick(id);
            }
        }, 1L, 1L);
        this.reconcileTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, task -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                this.scheduleRefreshTracking(player);
            }
        }, 40L, 40L);
    }

    public void cleanup() {
        if (this.cloakingTask != null && !this.cloakingTask.isCancelled()) {
            this.cloakingTask.cancel();
        }
        if (this.reconcileTask != null && !this.reconcileTask.isCancelled()) {
            this.reconcileTask.cancel();
        }
        this.activeCandidates.clear();
        this.ticksSinceLastMovement.clear();
    }

    private void scheduleCandidateTick(UUID id) {
        Player player = Bukkit.getPlayer((UUID)id);
        if (player == null) {
            this.activeCandidates.remove(id);
            this.ticksSinceLastMovement.remove(id);
            return;
        }
        player.getScheduler().execute(this.plugin, () -> this.processActiveCandidateOnPlayerThread(player), () -> {
            this.activeCandidates.remove(id);
            this.ticksSinceLastMovement.remove(id);
        }, 1L);
    }

    private void processActiveCandidateOnPlayerThread(Player player) {
        UUID id = player.getUniqueId();
        if (!this.isEligible(player)) {
            this.activeCandidates.remove(id);
            this.ticksSinceLastMovement.put(id, 0L);
            return;
        }
        this.ticksSinceLastMovement.compute(id, (uuid, ticks) -> ticks == null ? 1L : ticks + 1L);
        if (this.ticksSinceLastMovement.getOrDefault(id, 0L) < (long)this.cloakingEnchant.getTicksToActivate()) {
            return;
        }
        player.addPotionEffect(this.cloakingEffect);
    }

    private boolean isEligible(Player player) {
        if (!this.isRuntimeEnabled()) {
            return false;
        }
        if (!player.isOnline() || !player.isSneaking()) {
            return false;
        }
        return EnchADD.getSumOfEnchantLevels(player.getEquipment(), this.cloaking) > 0;
    }

    private boolean isRuntimeEnabled() {
        return this.plugin != null && this.cloaking != null && this.cloakingEnchant != null;
    }

    private void scheduleRefreshTracking(Player player) {
        if (player == null || !this.isRuntimeEnabled()) {
            return;
        }
        UUID id = player.getUniqueId();
        player.getScheduler().execute(this.plugin, () -> this.refreshTracking(player), () -> {
            this.activeCandidates.remove(id);
            this.ticksSinceLastMovement.remove(id);
        }, 1L);
    }

    private void refreshTracking(Player player) {
        if (!this.isRuntimeEnabled()) {
            return;
        }
        if (player == null) {
            return;
        }
        UUID id = player.getUniqueId();
        if (this.isEligible(player)) {
            this.activeCandidates.add(id);
            this.ticksSinceLastMovement.putIfAbsent(id, 0L);
            return;
        }
        this.activeCandidates.remove(id);
        this.ticksSinceLastMovement.put(id, 0L);
    }

    private void resetProgress(Player player) {
        UUID id = player.getUniqueId();
        this.ticksSinceLastMovement.put(id, 0L);
        if (!player.isSneaking()) {
            this.activeCandidates.remove(id);
        }
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.refreshTracking(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        this.activeCandidates.remove(id);
        this.ticksSinceLastMovement.remove(id);
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) {
            return;
        }
        this.resetProgress(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerJump(PlayerJumpEvent event) {
        this.resetProgress(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        this.resetProgress(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) {
            this.resetProgress(player);
            return;
        }
        this.refreshTracking(player);
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        this.refreshTracking(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onPlayerSwapHands(PlayerSwapHandItemsEvent event) {
        this.refreshTracking(event.getPlayer());
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        this.scheduleRefreshTracking(player);
    }
}
