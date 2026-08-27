package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CiStatusLegacySupportTest {

    @Test
    void summarizeLegacyKeysReturnsNoneForEmptyCounts() {
        assertEquals("none", CiStatusLegacySupport.summarizeLegacyKeys(Map.of()));
    }

    @Test
    void summarizeLegacyKeysPreservesOrderAndLimitsToFiveEntries() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("one", 1L);
        counts.put("two", 2L);
        counts.put("three", 3L);
        counts.put("four", 4L);
        counts.put("five", 5L);
        counts.put("six", 6L);

        assertEquals("one:1,two:2,three:3,four:4,five:5", CiStatusLegacySupport.summarizeLegacyKeys(counts));
    }
}
