package net.enchadd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShieldBlockingContractTest {

    private static final Path LISTENERS = TestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "listeners")
    );

    @Test
    void shieldDamageListenersUseSuccessfulShieldBlockGate() throws IOException {
        for (String fileName : List.of(
                "BarrierListener.java",
                "BreakguardListener.java",
                "BulwarkListener.java",
                "ParryListener.java",
                "PivotListener.java",
                "RiposteListener.java",
                "WardListener.java"
        )) {
            String source = read(fileName);
            assertTrue(source.contains("PerformanceUtils.isSuccessfulShieldBlock("),
                    fileName + " should gate effects with PerformanceUtils.isSuccessfulShieldBlock");
            assertFalse(source.contains(".isBlocking("),
                    fileName + " should not call player.isBlocking() directly");
        }
    }

    @Test
    void braceUsesFacingAwareShieldGateInsteadOfDirectBlockingCheck() throws IOException {
        String source = read("BraceListener.java");

        assertTrue(source.contains("PerformanceUtils.isLikelyShieldFacingBlock("),
                "Brace should use the facing-aware shield gate for knockback-only events");
        assertTrue(source.contains("EntityKnockbackEvent.Cause.SHIELD_BLOCK"),
                "Brace should only react to shield-block knockback causes");
        assertFalse(source.contains(".isBlocking("),
                "Brace should not call player.isBlocking() directly");
    }

    @Test
    void listenerLayerAvoidsDirectBlockingChecksOutsidePerformanceUtils() throws IOException {
        List<String> offenders;
        try (var stream = Files.list(LISTENERS)) {
            offenders = stream
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path -> {
                        try {
                            return Files.readString(path, StandardCharsets.UTF_8).contains(".isBlocking(");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
        }
        assertTrue(offenders.isEmpty(),
                "Listener layer should delegate blocking checks to PerformanceUtils. Offenders: " + offenders);
    }

    private static String read(String fileName) throws IOException {
        return Files.readString(LISTENERS.resolve(fileName), StandardCharsets.UTF_8);
    }
}
