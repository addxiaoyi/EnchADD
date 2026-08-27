package net.enchadd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandSecurityContractTest {

    private static final Path COMMAND_SOURCE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "commands", "EnchantListCommand.java")
    );
    private static final Path EXPORT_SERVICE_SOURCE = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "commands", "ExportService.java")
    );

    @Test
    void exportSubcommandsRequireExplicitExportPermission() throws IOException {
        String source = Files.readString(COMMAND_SOURCE, StandardCharsets.UTF_8);
        int permissionGuards = source.split("if \\(!hasExportPermission\\(sender\\)\\)").length - 1;

        assertAll(
                () -> assertTrue(source.contains("PERM_EXPORT = \"enchadd.export\""),
                        "Export permission constant should use the dedicated export permission node"),
                () -> assertTrue(permissionGuards >= 3,
                        "All export subcommands should guard with hasExportPermission")
        );
    }

    @Test
    void exportExecutionUsesBukkitSchedulerAndHasRateLimitGuard() throws IOException {
        String commandSource = Files.readString(COMMAND_SOURCE, StandardCharsets.UTF_8);
        String serviceSource = Files.readString(EXPORT_SERVICE_SOURCE, StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(commandSource.contains("ExportService.run("),
                        "Export subcommands should delegate to ExportService"),
                () -> assertTrue(serviceSource.contains("runTaskAsynchronously(plugin"),
                        "Export should run on Bukkit async scheduler"),
                () -> assertTrue(serviceSource.contains("runTask(plugin"),
                        "Export completion callback should run on Bukkit main thread"),
                () -> assertTrue(serviceSource.contains("exportInProgress.compareAndSet(false, true)"),
                        "Export should reject concurrent requests"),
                () -> assertFalse(serviceSource.contains("CompletableFuture.runAsync"),
                        "Export should avoid the common ForkJoin pool")
        );
    }

    @Test
    void infoSubcommandRequiresDedicatedInfoPermission() throws IOException {
        String source = Files.readString(COMMAND_SOURCE, StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(source.contains("PERM_INFO = \"enchadd.info\""),
                        "Info permission constant should use the dedicated info permission node"),
                () -> assertTrue(source.contains("if (!hasInfoPermission(sender))"),
                        "Info subcommand should enforce hasInfoPermission before execution")
        );
    }

    @Test
    void listPaginationKeepsTotalPagesAtLeastOneForEmptyResultSets() throws IOException {
        String source = Files.readString(COMMAND_SOURCE, StandardCharsets.UTF_8);

        assertTrue(source.contains("Math.max(searched.size(), 1)"),
                "List pagination should avoid rendering page counts like 1/0 for empty results");
    }
}
