package net.enchadd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyDefinitionResourceContractTest {

    @Test
    void legacyDefinitionResourceExistsAndListsKnownRemovedKeys() throws IOException {
        String source = Files.readString(
                TestProjectPaths.PLUGIN_MAIN_RESOURCES.resolve("legacy-enchants.json"),
                StandardCharsets.UTF_8
        );

        assertAll(
                () -> assertTrue(source.contains("\"entries\""), "legacy-enchants.json should expose entries array"),
                () -> assertTrue(source.contains("\"enchadd:cloaking\""), "legacy-enchants.json should keep cloaking"),
                () -> assertTrue(source.contains("\"enchadd:sonar\""), "legacy-enchants.json should keep sonar"),
                () -> assertTrue(source.contains("\"enchadd:arrow_refund\""), "legacy-enchants.json should keep arrow_refund"),
                () -> assertTrue(source.contains("\"enchadd:panic_curse\""), "legacy-enchants.json should include migration targets where applicable")
        );
    }
}
