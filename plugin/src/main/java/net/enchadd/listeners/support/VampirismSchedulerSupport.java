package net.enchadd.listeners.support;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class VampirismSchedulerSupport {

    private final Plugin plugin;
    private final int refreshIntervalTicks;

    public VampirismSchedulerSupport(@NotNull Plugin plugin, int refreshIntervalTicks) {
        this.plugin = plugin;
        this.refreshIntervalTicks = refreshIntervalTicks;
    }

    public ScheduledTask startRefreshTask(@NotNull Set<UUID> activeCandidates,
                                   @NotNull Consumer<UUID> activeTickScheduler) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> {
            for (UUID id : activeCandidates) {
                activeTickScheduler.accept(id);
            }
        }, 1L, refreshIntervalTicks);
    }

    public void scheduleOnlinePlayerBootstrap(@NotNull Consumer<Player> reconcileScheduler) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            reconcileScheduler.accept(online);
        }
    }

    public void schedulePlayerWork(@Nullable Player player,
                            @NotNull Runnable activeTask,
                            @NotNull Runnable retiredTask) {
        if (player == null) {
            retiredTask.run();
            return;
        }
        player.getScheduler().execute(plugin, activeTask, retiredTask, 1L);
    }
}
