package net.enchadd.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuntimeErrorTrackerTest {

    @AfterEach
    void reset() {
        RuntimeErrorTracker.reset();
    }

    @Test
    void recordErrorIncrementsAndResetClearsCounter() {
        RuntimeErrorTracker.reset();

        assertEquals(1L, RuntimeErrorTracker.recordError("unit.test"));
        assertEquals(2L, RuntimeErrorTracker.recordError("unit.test"));
        assertEquals(2L, RuntimeErrorTracker.getTotalErrors());

        RuntimeErrorTracker.reset();

        assertEquals(0L, RuntimeErrorTracker.getTotalErrors());
    }

    @Test
    void logSourceSanitizationIsStable() {
        assertEquals("unknown", RuntimeErrorTrackerLogSupport.sanitizeSource(null));
        assertEquals("unknown", RuntimeErrorTrackerLogSupport.sanitizeSource("   "));
        assertEquals("bad_source_name", RuntimeErrorTrackerLogSupport.sanitizeSource("bad source/name"));
        assertEquals("[EnchADD] runtime error recorded from export.task total=7",
                RuntimeErrorTrackerLogSupport.format("export.task", 7L));
    }

    @Test
    void logSourceIsCapped() {
        String source = "a".repeat(120);

        assertEquals(96, RuntimeErrorTrackerLogSupport.sanitizeSource(source).length());
    }
}
