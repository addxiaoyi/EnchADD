package net.enchadd;

import java.nio.file.Files;
import java.nio.file.Path;

final class PaperTestProjectPaths {

    static final Path REPO_ROOT = locateRepoRoot();
    static final Path PLUGIN_MAIN_JAVA = REPO_ROOT.resolve(Path.of("plugin", "src", "main", "java"));
    static final Path PLUGIN_MAIN_RESOURCES = REPO_ROOT.resolve(Path.of("plugin", "src", "main", "resources"));

    private PaperTestProjectPaths() {
    }

    private static Path locateRepoRoot() {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            Path pluginMain = current.resolve(Path.of("plugin", "src", "main"));
            Path papermcModule = current.resolve(Path.of("tests", "papermc"));
            if (Files.isDirectory(pluginMain) && Files.isDirectory(papermcModule)) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate the repository root from the Paper test module.");
    }
}
