package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuntimeErrorTrackerLogSupportTest {

    @Test
    void sanitizeSourceHandlesMissingAndUnsafeCharacters() {
        assertEquals("unknown", RuntimeErrorTrackerLogSupport.sanitizeSource(null));
        assertEquals("unknown", RuntimeErrorTrackerLogSupport.sanitizeSource("  "));
        assertEquals("alpha.beta:+-__bad", RuntimeErrorTrackerLogSupport.sanitizeSource("alpha.beta:+-_/bad"));
        assertEquals("alpha_beta_gamma", RuntimeErrorTrackerLogSupport.sanitizeSource("alpha beta=gamma"));
    }

    @Test
    void sanitizeSourceTruncatesLongValues() {
        String source = "a".repeat(120);

        assertEquals(96, RuntimeErrorTrackerLogSupport.sanitizeSource(source).length());
    }

    @Test
    void formatIncludesSanitizedSourceAndTotal() {
        assertEquals(
                "[EnchADD] runtime error recorded from alpha_beta total=5",
                RuntimeErrorTrackerLogSupport.format("alpha beta", 5)
        );
    }
}
