package net.enchadd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceOptimizationContractTest {

    private static final Path LISTENERS = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(Path.of("net", "enchadd", "listeners"));
    private static final Path COMMANDS = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(Path.of("net", "enchadd", "commands"));
    private static final Path UTILS = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(Path.of("net", "enchadd", "utils"));
    private static final Path CORE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(Path.of("net", "enchadd"));

    @Test
    void evasionUsesUnifiedPdcCooldownPath() throws IOException {
        String source = read(LISTENERS.resolve("EvasionListener.java"));
        Path legacyCooldownManager = UTILS.resolve("CooldownManager.java");

        assertAll(
                () -> assertTrue(source.contains("PerformanceUtils.isOnCooldown"),
                        "Evasion should check cooldown via PerformanceUtils + PDC"),
                () -> assertTrue(source.contains("PerformanceUtils.setCooldown"),
                        "Evasion should write cooldown via PerformanceUtils + PDC"),
                () -> assertFalse(source.contains("CooldownManager"),
                        "Evasion should not depend on the legacy CooldownManager"),
                () -> assertFalse(Files.exists(legacyCooldownManager),
                        "Legacy CooldownManager should be removed after unification")
        );
    }

    @Test
    void enchantStatsUsesBoundedSingleWriterQueue() throws IOException {
        String source = read(UTILS.resolve("EnchantStats.java"));

        assertAll(
                () -> assertTrue(source.contains("ArrayBlockingQueue"),
                        "EnchantStats should use a bounded queue"),
                () -> assertTrue(source.contains("writerLoop"),
                        "EnchantStats should use a dedicated writer loop"),
                () -> assertTrue(source.contains("writeBatch"),
                        "EnchantStats should batch writes to reduce IO churn"),
                () -> assertFalse(source.contains("CompletableFuture.runAsync"),
                        "EnchantStats should avoid nested async task stacking")
        );
    }

    @Test
    void highFrequencyListenersTrackActiveCandidates() throws IOException {
        String vampirism = read(LISTENERS.resolve("VampirismListener.java"));

        assertAll(
                () -> assertTrue(vampirism.contains("activeCandidates"),
                        "Vampirism should maintain an active candidate set"),
                () -> assertTrue(vampirism.contains("for (UUID id : activeCandidates)"),
                        "Vampirism scheduler should iterate active set instead of full online scan")
        );
    }

    @Test
    void foliaHotPathsUsePlayerSchedulerWithoutGlobalRunTaskBridge() throws IOException {
        String vampirism = read(LISTENERS.resolve("VampirismListener.java"));

        assertAll(
                () -> assertTrue(vampirism.contains("scheduleActiveTick"),
                        "Vampirism should dispatch active ticks through player scheduler"),
                () -> assertTrue(vampirism.contains("scheduleReconcile"),
                        "Vampirism should dispatch reconcile work through player scheduler"),
                () -> assertTrue(vampirism.contains("player.getScheduler().execute"),
                        "Vampirism should execute player work on player scheduler"),
                () -> assertFalse(vampirism.contains("Bukkit.getScheduler().runTask("),
                        "Vampirism should not use Bukkit scheduler runTask bridge")
        );
    }

    @Test
    void telepathyDispatchesOnPlayerSchedulerWithoutBukkitBridge() throws IOException {
        String telepathy = read(LISTENERS.resolve("TelepathyListener.java"));

        assertAll(
                () -> assertTrue(telepathy.contains("player.getScheduler().execute"),
                        "Telepathy should schedule item teleport on player scheduler"),
                () -> assertFalse(telepathy.contains("Bukkit.getScheduler().runTask("),
                        "Telepathy should avoid Bukkit scheduler runTask bridge")
        );
    }

    @Test
    void listenersDoNotUseBukkitRunTaskBridge() throws IOException {
        List<Path> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(LISTENERS)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String source = Files.readString(path, StandardCharsets.UTF_8);
                    if (source.contains("Bukkit.getScheduler().runTask(")) {
                        offenders.add(path.getFileName());
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read listener file: " + path, e);
                }
            });
        }

        assertTrue(offenders.isEmpty(),
                "Listeners should not use Bukkit scheduler runTask bridge in performance-sensitive runtime. Offenders: " + offenders);
    }

    @Test
    void listenerPluginBindingDoesNotDependOnHardcodedPluginName() throws IOException {
        String registrar = read(LISTENERS.resolve("EnchantListenerRegistrar.java"));
        String plan = read(LISTENERS.resolve("ListenerRegistrationPlan.java"));

        assertAll(
                () -> assertTrue(registrar.contains("ListenerRegistrationAssembler.registerAll(plugin);"),
                        "Registrar should delegate to the listener registration assembler"),
                () -> assertTrue(plan.contains("plugin -> new TelepathyListener(plugin)"),
                        "Telepathy listener should receive plugin instance via registration plan"),
                () -> assertTrue(plan.contains("plugin -> new VampirismListener(plugin)"),
                        "Vampirism listener should receive plugin instance via registration plan"),
                () -> assertFalse(plan.contains("getPlugin(\"EnchADD\")"),
                        "Listener registration plan should not resolve plugin by hardcoded plugin name")
        );
    }

    @Test
    void commandCompletionUsesSuggestionCacheHotPath() throws IOException {
        String source = read(COMMANDS.resolve("EnchantListCommand.java"));

        assertAll(
                () -> assertTrue(source.contains("SuggestionCache.filter"),
                        "Enchant command completion should delegate to SuggestionCache.filter"),
                () -> assertTrue(source.contains("handlePerf"),
                        "Enchant command should provide a dedicated perf injection subcommand"),
                () -> assertTrue(source.contains("hasAdminPermission"),
                        "Perf injection path should be permission-gated for admin users"),
                () -> assertTrue(source.contains("out.add(\"perf\")"),
                        "Perf subcommand should be visible in root command suggestions")
        );
    }

    @Test
    void exportCommandUsesMainThreadSnapshotContextBeforeAsyncIo() throws IOException {
        String commandSource = read(COMMANDS.resolve("EnchantListCommand.java"));
        String exportServiceSource = read(COMMANDS.resolve("ExportService.java"));

        assertAll(
                () -> assertTrue(commandSource.contains("ExportService.snapshot("),
                        "Export command should snapshot context on command thread before async execution"),
                () -> assertTrue(exportServiceSource.contains("Function<ExportContext, ExportResult>"),
                        "Export runner should accept immutable export context for async work"),
                () -> assertTrue(commandSource.contains("ExportService.run(plugin, sender, context, exportInProgress, ExportService::exportMarkdown)"),
                        "Markdown export should run with prebuilt context"),
                () -> assertTrue(commandSource.contains("ExportService.run(plugin, sender, context, exportInProgress, ExportService::exportJson)"),
                        "JSON export should run with prebuilt context"),
                () -> assertTrue(commandSource.contains("ExportService.run(plugin, sender, context, exportInProgress, ExportService::exportCsv)"),
                        "CSV export should run with prebuilt context"),
                () -> assertFalse(commandSource.contains("runExportTask(sender, () -> exportMarkdown(snapshotEnchants()))"),
                        "Export command should not build snapshots inside async task lambdas"),
                () -> assertFalse(commandSource.contains("runExportTask(sender, () -> exportJson(snapshotEnchants()))"),
                        "JSON export should not build snapshots inside async task lambdas"),
                () -> assertFalse(commandSource.contains("runExportTask(sender, () -> exportCsv(snapshotEnchants()))"),
                        "CSV export should not build snapshots inside async task lambdas")
        );
    }

    @Test
    void particleQueueExposesAdaptiveAndGuardrailSignals() throws IOException {
        String source = read(UTILS.resolve("ParticleQueue.java"));

        assertAll(
                () -> assertTrue(source.contains("dynamicMaxPerTick"),
                        "ParticleQueue should maintain a dynamic per-tick budget"),
                () -> assertTrue(source.contains("adaptThroughput"),
                        "ParticleQueue should adapt processing throughput"),
                () -> assertTrue(source.contains("warnDropRateThreshold"),
                        "ParticleQueue should expose drop warning threshold"),
                () -> assertTrue(source.contains("getLastWindowDropRate"),
                        "ParticleQueue should expose drop-rate metrics for CI gates")
        );
    }

    @Test
    void lifecycleStopDetachesLangTranslatorSource() throws IOException {
        String source = read(CORE.resolve("EnchADDLifecycleService.java"));

        assertAll(
                () -> assertTrue(source.contains("PlaceholderAPIHook.unregister"),
                        "Lifecycle stop should explicitly detach PlaceholderAPI expansion"),
                () -> assertTrue(source.contains("PluginDisableEvent"),
                        "Lifecycle should observe PlaceholderAPI disable/reload boundaries"),
                () -> assertTrue(source.contains("PlaceholderAPIHook.onPlaceholderApiDisabled"),
                        "Lifecycle should reset PlaceholderAPI hook state when PlaceholderAPI is disabled"),
                () -> assertTrue(source.contains("LangManager.shutdown"),
                        "Lifecycle stop should detach LangManager from GlobalTranslator"),
                () -> assertTrue(source.contains("SuggestionCache.invalidate"),
                        "Lifecycle stop should invalidate suggestion cache to avoid stale reload state"),
                () -> assertTrue(source.contains("EnchantCache.clear"),
                        "Lifecycle stop should clear enchant cache to avoid stale item-level state")
        );
    }

    @Test
    void lifecycleStartFailurePathRollsBackAndDisablesPlugin() throws IOException {
        String source = read(CORE.resolve("EnchADDLifecycleService.java"));

        assertAll(
                () -> assertTrue(source.contains("插件启动阶段异常"),
                        "Lifecycle should emit explicit startup failure logs"),
                () -> assertTrue(source.contains("stop();"),
                        "Lifecycle startup failure should rollback via stop cleanup"),
                () -> assertTrue(source.contains("disablePlugin(plugin)"),
                        "Lifecycle startup failure should disable plugin to avoid partial runtime")
        );
    }

    @Test
    void listenerRegistryGuardsInitializationOrderAndFallbackLogging() throws IOException {
        String source = read(UTILS.resolve("ListenerRegistry.java"));

        assertAll(
                () -> assertTrue(source.contains("Objects.requireNonNull(plugin"),
                        "ListenerRegistry.init should reject null plugin wiring"),
                () -> assertTrue(source.contains("if (plugin == null)"),
                        "ListenerRegistry should guard register path before initialization"),
                () -> assertTrue(source.contains("if (listener == null)"),
                        "ListenerRegistry should guard null listener factories to avoid runtime NPE"),
                () -> assertTrue(source.contains("new ArrayList<>(registeredListeners.keySet())"),
                        "ListenerRegistry.reload should use key snapshots when mutating registration map"),
                () -> assertTrue(source.contains("Bukkit.getLogger().warning(\"[EnchADD]"),
                        "ListenerRegistry should keep fallback warning path when plugin logger is unavailable")
        );
    }

    @Test
    void bootstrapUsesSummaryInfoAndDebugPerEnchantLogs() throws IOException {
        String source = read(CORE.resolve("EnchADDBootstrap.java"));

        assertAll(
                () -> assertTrue(source.contains("Preparing {} active custom enchants and {} legacy compatibility enchants for bootstrap registration"),
                        "Bootstrap should emit a compact summary log instead of per-enchant info spam"),
                () -> assertTrue(source.contains("logger.debug(\"Registering item tag {}\""),
                        "Bootstrap should move per-tag logs to debug level"),
                () -> assertTrue(source.contains("logger.debug(\"Registering enchantment {}\""),
                        "Bootstrap should move per-enchant logs to debug level"),
                () -> assertFalse(source.contains("logger.info(\"Registering item tag {}\""),
                        "Bootstrap should avoid per-tag info logs on normal startup path"),
                () -> assertFalse(source.contains("logger.info(\"Registering enchantment {}\""),
                        "Bootstrap should avoid per-enchant info logs on normal startup path")
        );
    }

    @Test
    void langManagerReloadPathRebuildsTranslationRegistryEntries() throws IOException {
        String source = read(UTILS.resolve("LangManager.java"));

        assertAll(
                () -> assertTrue(source.contains("clearRegistryTranslations();"),
                        "LangManager should clear registered translation keys before init/reload/shutdown rebuild"),
                () -> assertTrue(source.contains("REGISTRY.unregister(key)"),
                        "LangManager should unregister previous translation keys before re-registering"),
                () -> assertTrue(source.contains("REGISTRY.register(key, locale"),
                        "LangManager should re-register current translation values into Adventure registry")
        );
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
