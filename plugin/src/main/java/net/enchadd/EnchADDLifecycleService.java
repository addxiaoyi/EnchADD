package net.enchadd;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.enchadd.commands.EnchantListCommand;
import net.enchadd.hooks.PlaceholderAPIHook;
import net.enchadd.listeners.*;
import net.enchadd.utils.EnchantCache;
import net.enchadd.utils.EnchantStats;
import net.enchadd.utils.LangManager;
import net.enchadd.utils.ListenerRegistry;
import net.enchadd.utils.ParticleQueue;
import net.enchadd.utils.RuntimeErrorTracker;
import net.enchadd.utils.RuntimeHealthMonitor;
import net.enchadd.utils.SafetyModeManager;
import net.enchadd.utils.SuggestionCache;
import net.enchadd.utils.EnchantExecutionBudgetManager;
import net.enchadd.utils.GitHubReleaseChecker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class EnchADDLifecycleService {

    private static final String CMD_ENCHADD = "enchadd";

    private final @NotNull JavaPlugin plugin;
    private final GitHubReleaseChecker updateChecker = new GitHubReleaseChecker();

    public EnchADDLifecycleService(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean start() {
        if (!initConfiguration()) {
            return false;
        }
        try {
            initManagers();
            registerLifecycleHandlers();
            registerCommands();
            registerListeners();
            return true;
        } catch (RuntimeException ex) {
            plugin.getLogger().severe("插件启动阶段异常，正在执行回滚清理: " + ex.getMessage());
            stop();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }

    public void stop() {
        runCleanup("RuntimeHealthMonitor.stop", RuntimeHealthMonitor::stop);
        runCleanup("GitHubReleaseChecker.stop", updateChecker::stop);
        runCleanup("EnchantExecutionBudgetManager.stop", EnchantExecutionBudgetManager::stop);
        runCleanup("SafetyModeManager.shutdown", SafetyModeManager::shutdown);
        runCleanup("ParticleQueue.stop", ParticleQueue::stop);
        runCleanup("ListenerRegistry.unregisterAll", ListenerRegistry::unregisterAll);
        runCleanup("EnchantStats.flushSync", EnchantStats::flushSync);
        runCleanup("PlaceholderAPIHook.unregister", () -> PlaceholderAPIHook.unregister(plugin));
        runCleanup("LangManager.shutdown", LangManager::shutdown);
        runCleanup("SuggestionCache.invalidate", SuggestionCache::invalidate);
        runCleanup("EnchantCache.clear", EnchantCache::clear);
        RuntimeErrorTracker.reset();
    }

    private boolean initConfiguration() {
        try {
            EnchADDConfig.init(plugin.getDataFolder().toPath());
            LangManager.init(plugin.getDataFolder().toPath(), EnchADDConfig.getLanguage());
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("配置初始化失败: " + e.getMessage());
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }

    private void initManagers() {
        plugin.getLogger().info("[EnchADD] 组件缓存已就绪。");
        SuggestionCache.build();
        SafetyModeManager.initialize(plugin);
        EnchantExecutionBudgetManager.start(plugin);
        int maxParticlesPerTick = plugin.getConfig().getInt("particle-queue.max-per-tick", 100);
        int minParticlesPerTick = plugin.getConfig().getInt(
                "particle-queue.min-per-tick",
                Math.max(1, maxParticlesPerTick / 2)
        );
        int maxDynamicParticlesPerTick = plugin.getConfig().getInt(
                "particle-queue.max-dynamic-per-tick",
                Math.max(maxParticlesPerTick, maxParticlesPerTick * 4)
        );
        double warnDropRate = plugin.getConfig().getDouble("particle-queue.warn-drop-rate", 0.05d);
        ParticleQueue.start(
                plugin,
                maxParticlesPerTick,
                minParticlesPerTick,
                maxDynamicParticlesPerTick,
                warnDropRate
        );
        ListenerRegistry.init(plugin);
        EnchantStats.init(plugin);
        RuntimeErrorTracker.reset();
        RuntimeHealthMonitor.start(plugin);
        updateChecker.start(plugin);
    }

    private void registerLifecycleHandlers() {
        plugin.getServer().getPluginManager().registerEvents(new LifecycleListener(), plugin);
        PlaceholderAPIHook.tryRegister(plugin);
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPluginEnable(PluginEnableEvent event) {
                if ("PlaceholderAPI".equalsIgnoreCase(event.getPlugin().getName())) {
                    PlaceholderAPIHook.tryRegister(plugin);
                }
            }

            @EventHandler
            public void onPluginDisable(PluginDisableEvent event) {
                if ("PlaceholderAPI".equalsIgnoreCase(event.getPlugin().getName())) {
                    PlaceholderAPIHook.onPlaceholderApiDisabled(plugin);
                }
            }
        }, plugin);
    }

    private void registerCommands() {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            final Commands registrar = event.registrar();
            registrar.register(CMD_ENCHADD, List.of("enchants"), new EnchantListCommand(plugin));
        }));
    }

    private void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new CurseConflictListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new LegacyEnchantSanitizerListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ServerExceptionMonitorListener(plugin), plugin);
        EnchantListenerRegistrar.registerAll(plugin);
    }

    private void runCleanup(String step, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            RuntimeErrorTracker.recordError("cleanup." + step);
            plugin.getLogger().warning("清理步骤失败 [" + step + "]: " + ex.getMessage());
        }
    }
}
