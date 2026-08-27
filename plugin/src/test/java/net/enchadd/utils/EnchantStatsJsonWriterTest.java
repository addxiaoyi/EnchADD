package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnchantStatsJsonWriterTest {

    @Test
    void escapeJsonEscapesBackslashesAndQuotes() {
        assertEquals("a\\\\b\\\"c", EnchantStatsJsonWriter.escapeJson("a\\b\"c"));
    }

    @Test
    void appendJsonLineFormatsSnapshotDeterministically() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("enchadd:airbag", 5L);
        EnchantStatsSnapshot snapshot = new EnchantStatsSnapshot(
                Map.copyOf(counts),
                OffsetDateTime.parse("2026-05-22T21:00:00+08:00"),
                true
        );

        StringBuilder sb = new StringBuilder();
        EnchantStatsJsonWriter.appendJsonLine(sb, "session01", DateTimeFormatter.ISO_OFFSET_DATE_TIME, snapshot);

        assertEquals(
                "{\"session\":\"session01\",\"time\":\"2026-05-22T21:00:00+08:00\",\"shutdown\":true,\"counts\":{\"enchadd:airbag\":5}}",
                sb.toString()
        );
    }
}
