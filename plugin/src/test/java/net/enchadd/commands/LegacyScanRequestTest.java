package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyScanRequestTest {

    @Test
    void fromDefaultsToWildcardAndFullOutput() {
        LegacyScanRequest request = LegacyScanRequest.from(new String[] {"legacyscan"});

        assertEquals("", request.filter());
        assertEquals("*", request.displayFilter());
        assertFalse(request.summaryOnly());
    }

    @Test
    void fromNormalizesFilterAndDetectsSummaryMode() {
        LegacyScanRequest request = LegacyScanRequest.from(new String[] {"legacyscan", "Alice", "SUMMARY"});

        assertEquals("alice", request.filter());
        assertEquals("alice", request.displayFilter());
        assertTrue(request.summaryOnly());
    }
}
