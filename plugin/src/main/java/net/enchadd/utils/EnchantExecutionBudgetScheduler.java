package net.enchadd.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

final class EnchantExecutionBudgetScheduler {

    private EnchantExecutionBudgetScheduler() {
    }

    static void start(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state,
                      @NotNull JavaPlugin owningPlugin) {
        stop(state, null);
        state.plugin = owningPlugin;
        state.states.clear();
        state.degradedExecutions.set(0L);
        state.skippedExecutions.set(0L);
        state.tickCounter.set(0L);
        state.tickTask = owningPlugin.getServer().getScheduler().runTaskTimer(
                owningPlugin,
                () -> state.tickCounter.incrementAndGet(),
                1L,
                1L
        );
    }

    static void stop(@NotNull EnchantExecutionBudgetManager.EnchantExecutionBudgetState state,
                     ThreadLocal<EnchantExecutionBudgetManager.ExecutionToken> currentExecution) {
        BukkitTask task = state.tickTask;
        state.tickTask = null;
        if (task != null) {
            task.cancel();
        }
        if (currentExecution != null) {
            currentExecution.remove();
        }
        state.states.clear();
        state.tickCounter.set(0L);
        state.degradedExecutions.set(0L);
        state.skippedExecutions.set(0L);
        state.plugin = null;
    }
}
