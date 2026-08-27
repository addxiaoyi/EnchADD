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

class CompatibilityBoundaryContractTest {

    private static final Path PLUGIN_MAIN_JAVA = TestProjectPaths.PLUGIN_MAIN_JAVA;
    private static final Path PAPER_PLUGIN_YML = TestProjectPaths.PLUGIN_MAIN_RESOURCES.resolve("paper-plugin.yml");
    private static final List<String> FORBIDDEN_IMPORT_PREFIXES = List.of(
            "import net.minecraft.",
            "import org.bukkit.craftbukkit."
    );

    @Test
    void pluginSourceDoesNotDependOnNmsOrCraftBukkitInternals() throws IOException {
        try (Stream<Path> files = Files.walk(PLUGIN_MAIN_JAVA)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(this::assertNoForbiddenImports);
        }
    }

    @Test
    void pluginSourceDoesNotUseLegacyDamageModifierBlockingApi() throws IOException {
        List<Path> offenders = filesContaining("DamageModifier.");
        assertTrue(offenders.isEmpty(),
                "Legacy DamageModifier API should not re-enter plugin sources. Offenders: " + offenders);
    }

    @Test
    void placeholderApiTypeReferenceStaysIsolatedToHookFile() throws IOException {
        List<Path> holders = filesContaining("me.clip.placeholderapi");
        assertAll(
                () -> assertTrue(!holders.isEmpty(),
                        "PlaceholderAPI bridge should keep explicit placeholder type reference in hook layer"),
                () -> assertTrue(holders.stream().allMatch(path -> path.getFileName().toString().equals("PlaceholderAPIHook.java")),
                        "PlaceholderAPI type references should stay isolated to PlaceholderAPIHook.java. Found: " + holders)
        );
    }

    @Test
    void paperPluginMetadataKeepsOptionalPlaceholderApiDependencyContract() throws IOException {
        String source = Files.readString(PAPER_PLUGIN_YML, StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(source.contains("dependencies:"),
                        "paper-plugin.yml should declare Paper dependencies"),
                () -> assertTrue(source.contains("PlaceholderAPI:"),
                        "PlaceholderAPI compatibility should stay explicit in paper-plugin.yml"),
                () -> assertTrue(source.contains("load: BEFORE"),
                        "Optional PlaceholderAPI bridge should request loading before this plugin"),
                () -> assertTrue(source.contains("required: false"),
                        "PlaceholderAPI dependency must remain optional"),
                () -> assertTrue(source.contains("join-classpath: true"),
                        "Optional PlaceholderAPI dependency should join classpath for expansion linkage")
        );
    }

    @Test
    void legacyRootRegistrarsDoNotReappear() {
        Path legacyRegistrar = PLUGIN_MAIN_JAVA.resolve(Path.of("net", "enchadd", "EnchADDListenerRegistrar.java"));
        Path legacyCommandRegistrar = PLUGIN_MAIN_JAVA.resolve(Path.of("net", "enchadd", "EnchADDCommandRegistrar.java"));
        assertAll(
                () -> assertFalse(Files.exists(legacyRegistrar),
                        "Legacy root listener registrar should stay removed to avoid split registration paths"),
                () -> assertFalse(Files.exists(legacyCommandRegistrar),
                        "Legacy root command registrar should stay removed to avoid split command wiring paths")
        );
    }

    private void assertNoForbiddenImports(Path file) {
        try {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            for (String forbidden : FORBIDDEN_IMPORT_PREFIXES) {
                assertFalse(
                        source.contains(forbidden),
                        () -> "Forbidden internal dependency '" + forbidden + "' detected in " + file
                );
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read source file: " + file, e);
        }
    }

    private List<Path> filesContaining(String needle) throws IOException {
        List<Path> hits = new ArrayList<>();
        try (Stream<Path> files = Files.walk(PLUGIN_MAIN_JAVA)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String source = Files.readString(path, StandardCharsets.UTF_8);
                    if (source.contains(needle)) {
                        hits.add(path.getFileName());
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read source file: " + path, e);
                }
            });
        }
        return hits;
    }
}
