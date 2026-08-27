package net.enchadd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyScanCommandContractTest {

    @Test
    void commandAndScriptSurfaceLegacyScanEntryPoints() throws IOException {
        String commandSource = Files.readString(
                TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(java.nio.file.Path.of("net", "enchadd", "commands", "EnchantListCommand.java")),
                StandardCharsets.UTF_8
        );
        String pluginYml = Files.readString(
                TestProjectPaths.PLUGIN_MAIN_RESOURCES.resolve("plugin.yml"),
                StandardCharsets.UTF_8
        );
        String script = Files.readString(
                TestProjectPaths.REPO_ROOT.resolve(java.nio.file.Path.of("scripts", "scan-legacy-enchants.ps1")),
                StandardCharsets.UTF_8
        );

        assertAll(
                () -> assertTrue(commandSource.contains("SUB_LEGACYSCAN = \"legacyscan\""),
                        "EnchantListCommand should define the legacyscan subcommand"),
                () -> assertTrue(commandSource.contains("/\" + label + \" legacyscan"),
                        "Help output should document the legacyscan subcommand"),
                () -> assertTrue(commandSource.contains("handleLegacyScan"),
                        "EnchantListCommand should implement legacy scan handling"),
                () -> assertTrue(pluginYml.contains("usage: /enchadd help"),
                        "plugin.yml should still expose the main enchadd command usage"),
                () -> assertTrue(script.contains("[ENCHADD-LEGACYSCAN-OFFLINE]"),
                        "Offline legacy scan script should emit stable legacy scan markers"),
                () -> assertTrue(script.contains("legacy-enchants.json"),
                        "Offline legacy scan script should source legacy enchant keys from the shared definition resource")
        );
    }
}
