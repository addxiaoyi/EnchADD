package net.enchadd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacySanitizerListenerContractTest {

    @Test
    void listenerDelegatesSanitizationToSupportClass() throws IOException {
        Path listenerPath = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
                Path.of("net", "enchadd", "listeners", "LegacyEnchantSanitizerListener.java"));
        Path supportPath = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
                Path.of("net", "enchadd", "listeners/support", "LegacyEnchantSanitizerSupport.java"));

        String listenerSource = Files.readString(listenerPath, StandardCharsets.UTF_8);
        String supportSource = Files.readString(supportPath, StandardCharsets.UTF_8);

        assertTrue(listenerSource.contains("LegacyEnchantSanitizerSupport"),
                "Legacy sanitizer listener should delegate inventory/item cleanup to a support class");
        assertTrue(listenerSource.contains("sanitizerSupport.sanitizeInventory"),
                "Legacy sanitizer listener should delegate inventory cleanup");
        assertTrue(listenerSource.contains("sanitizerSupport.sanitizeItem"),
                "Legacy sanitizer listener should delegate item cleanup");
        assertTrue(supportSource.contains("LegacyEnchantReport.scanItem"),
                "Support class should retain legacy enchant scan reporting");
        assertTrue(supportSource.contains("LegacyEnchantSanitizer.sanitize"),
                "Support class should retain sanitizer mutation logic");
    }
}
