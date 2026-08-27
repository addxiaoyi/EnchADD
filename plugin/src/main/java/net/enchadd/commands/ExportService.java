package net.enchadd.commands;

import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.utils.RuntimeErrorTracker;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

final class ExportService {

    private ExportService() {
    }

    static ExportContext snapshot(JavaPlugin plugin, Map<net.kyori.adventure.key.Key, EnchADDEnchant> enchants) {
        List<ExportEntry> entries = new ArrayList<>();
        for (EnchADDEnchant enchant : enchants.values()) {
            entries.add(new ExportEntry(
                    enchant.getDescriptionText(),
                    enchant.getKey().asString(),
                    enchant.getMaxLevel(),
                    enchant.getWeight(),
                    enchant.getAnvilCost(),
                    EnchantListFormatSupport.slotsToString(enchant.getActiveSlots())
            ));
        }
        Path outDir = plugin.getDataFolder().toPath().resolve("dist").resolve("reports");
        String version = plugin.getPluginMeta().getVersion();
        return new ExportContext(outDir, version, List.copyOf(entries));
    }

    static void run(JavaPlugin plugin,
                    CommandSender sender,
                    ExportContext context,
                    AtomicBoolean exportInProgress,
                    Function<ExportContext, ExportResult> task) {
        if (!exportInProgress.compareAndSet(false, true)) {
            sender.sendMessage(Component.text("已有导出任务正在执行，请稍后再试。"));
            return;
        }

        try {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                ExportResult result;
                try {
                    result = task.apply(context);
                } catch (RuntimeException ex) {
                    RuntimeErrorTracker.recordError("export.task");
                    plugin.getLogger().warning("EnchADD export task failed unexpectedly: " + ex.getMessage());
                    result = ExportResult.failure(Component.translatable("EnchADD.command.export.failed"));
                }
                ExportResult finalResult = result;
                try {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        try {
                            if (!finalResult.success()) {
                                plugin.getLogger().warning("EnchADD export task finished with a failure response.");
                            }
                            sender.sendMessage(finalResult.message());
                        } finally {
                            exportInProgress.set(false);
                        }
                    });
                } catch (RuntimeException ex) {
                    RuntimeErrorTracker.recordError("export.callback");
                    exportInProgress.set(false);
                    plugin.getLogger().warning("Failed to schedule export completion callback: " + ex.getMessage());
                }
            });
        } catch (RuntimeException ex) {
            RuntimeErrorTracker.recordError("export.schedule");
            exportInProgress.set(false);
            sender.sendMessage(Component.translatable("EnchADD.command.export.failed"));
        }
    }

    static ExportResult exportMarkdown(ExportContext context) {
        try {
            Path outDir = context.outputDir();
            Files.createDirectories(outDir);
            Path out = outDir.resolve("enchants.md");
            Files.write(out, ExportContentComposer.markdown(context), StandardCharsets.UTF_8);
            return ExportResult.success(Component.translatable("EnchADD.command.export.done", Component.text(out.toString())));
        } catch (java.io.IOException e) {
            return ExportResult.failure(Component.translatable("EnchADD.command.export.failed"));
        }
    }

    static ExportResult exportJson(ExportContext context) {
        try {
            Path outDir = context.outputDir();
            Files.createDirectories(outDir);
            Path out = outDir.resolve("enchants.json");
            Path meta = outDir.resolve("enchants-meta.json");
            Files.write(out, ExportContentComposer.json(context).getBytes(StandardCharsets.UTF_8));
            Files.write(meta, ExportContentComposer.metaJson(context).getBytes(StandardCharsets.UTF_8));
            return ExportResult.success(Component.text("已导出: " + out));
        } catch (java.io.IOException e) {
            return ExportResult.failure(Component.text("导出失败"));
        }
    }

    static ExportResult exportCsv(ExportContext context) {
        try {
            Path outDir = context.outputDir();
            Files.createDirectories(outDir);
            Path out = outDir.resolve("enchants.csv");
            Files.write(out, ExportContentComposer.csv(context), StandardCharsets.UTF_8);
            return ExportResult.success(Component.text("已导出: " + out));
        } catch (java.io.IOException e) {
            return ExportResult.failure(Component.text("导出失败"));
        }
    }

    record ExportContext(Path outputDir, String version, List<ExportEntry> entries) {
    }

    record ExportEntry(String name, String key, int maxLevel, int weight, int anvilCost, String slots) {
    }

    static final class ExportResult {
        private final boolean success;
        private final Component message;

        private ExportResult(boolean success, Component message) {
            this.success = success;
            this.message = message;
        }

        static ExportResult success(Component message) {
            return new ExportResult(true, message);
        }

        static ExportResult failure(Component message) {
            return new ExportResult(false, message);
        }

        boolean success() {
            return success;
        }

        Component message() {
            return message;
        }
    }
}
