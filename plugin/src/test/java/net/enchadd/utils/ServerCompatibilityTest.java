package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerCompatibilityTest {

    @Test
    void detectsLegacyPaperFromMinecraftVersion() {
        ServerCompatibility.Detection detection = ServerCompatibility.detect(
                "1.21.11", "1.21.11-R0.1-SNAPSHOT", "git-Paper-123 (MC: 1.21.11)"
        );

        assertEquals(ServerCompatibility.Status.SUPPORTED, detection.status());
        assertEquals("1.21.11", detection.displayVersion());
        assertEquals("paper-1.21+", detection.mode());
        assertTrue(detection.shouldStart());
    }

    @Test
    void detectsModernPaper26AndFutureBuilds() {
        ServerCompatibility.Detection detection = ServerCompatibility.detect(
                "26.3", "26.3.build.142-beta", "Paper 26.3"
        );

        assertEquals(ServerCompatibility.Status.SUPPORTED, detection.status());
        assertEquals("26.3.0", detection.displayVersion());
        assertEquals("paper-modern-26+", detection.mode());
    }

    @Test
    void rejectsKnownVersionsBelowMinimum() {
        ServerCompatibility.Detection detection = ServerCompatibility.detect(
                "1.21.10", "1.21.10-R0.1-SNAPSHOT", "git-Paper (MC: 1.21.10)"
        );

        assertEquals(ServerCompatibility.Status.UNSUPPORTED, detection.status());
        assertFalse(detection.shouldStart());
    }

    @Test
    void fallsBackToOtherVersionFieldsWhenPrimaryFieldIsMalformed() {
        ServerCompatibility.Detection detection = ServerCompatibility.detect(
                "not-a-version", "git-Paper-123 (MC: 1.21.11)", "Paper"
        );

        assertEquals(ServerCompatibility.Status.SUPPORTED, detection.status());
        assertEquals("1.21.11", detection.displayVersion());
    }

    @Test
    void unknownVersionUsesSafeFallback() {
        ServerCompatibility.Detection detection = ServerCompatibility.detect("unknown", "unknown", "unknown");

        assertEquals(ServerCompatibility.Status.UNKNOWN, detection.status());
        assertNull(detection.version());
        assertEquals("unknown-safe-fallback", detection.mode());
        assertTrue(detection.shouldStart());
    }

    @Test
    void parsesVersionTokensWithBuildSuffixes() {
        ServerCompatibility.ServerVersion version = ServerCompatibility.parse("git-Paper-999 (MC: 1.21.11)");

        assertNotNull(version);
        assertEquals(new ServerCompatibility.ServerVersion(1, 21, 11), version);
    }
}