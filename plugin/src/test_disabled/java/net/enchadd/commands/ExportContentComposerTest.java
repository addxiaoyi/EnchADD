package net.enchadd.commands;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportContentComposerTest {

    @AfterEach
    void resetClock() {
        ExportContentComposer.resetClockForTesting();
    }

    @Test
    void markdownSortsNamesForStableOutput() {
        ExportService.ExportContext context = context(
                new ExportService.ExportEntry("Beta", "enchadd:beta", 1, 10, 2, "mainhand"),
                new ExportService.ExportEntry("Alpha", "enchadd:alpha", 2, 5, 3, "armor")
        );

        assertEquals(List.of("Enchantment List", "- Alpha", "- Beta"),
                ExportContentComposer.markdown(context));
    }

    @Test
    void jsonAndCsvEscapeSpecialCharacters() {
        ExportService.ExportContext context = context(
                new ExportService.ExportEntry("A\"B", "enchadd:a\\b", 1, 2, 3, "main,off")
        );

        assertEquals("[{\"name\":\"A\\\"B\",\"key\":\"enchadd:a\\\\b\",\"maxLevel\":1,\"weight\":2,\"anvilCost\":3,\"slots\":\"main,off\"}]",
                ExportContentComposer.json(context));
        assertEquals("\"x,y\"", ExportContentComposer.escapeCsv("x,y"));
        assertEquals("\"x\"\"y\"", ExportContentComposer.escapeCsv("x\"y"));
    }

    @Test
    void metadataUsesVersionAndStableGeneratedAt() {
        ExportContentComposer.useClockForTesting(Clock.fixed(Instant.parse("2026-05-27T12:00:00Z"), ZoneOffset.UTC));

        assertEquals("{\"generatedAt\":\"2026-05-27T12:00Z\",\"version\":\"2.0.1\"}",
                ExportContentComposer.metaJson(context()));
    }

    @Test
    void csvIncludesMetadataHeaderAndEscapedRows() {
        ExportContentComposer.useClockForTesting(Clock.fixed(Instant.parse("2026-05-27T12:00:00Z"), ZoneOffset.UTC));
        ExportService.ExportContext context = context(
                new ExportService.ExportEntry("A,B", "enchadd:a", 2, 5, 7, "main\noff")
        );

        assertEquals(List.of(
                "# generatedAt=2026-05-27T12:00Z",
                "# version=2.0.1",
                "name,key,maxLevel,weight,anvilCost,slots",
                "\"A,B\",enchadd:a,2,5,7,\"main\noff\""
        ), ExportContentComposer.csv(context));
    }

    @Test
    void escapeJsonEscapesControlCharactersAndQuotes() {
        assertEquals("a\\\\b\\\"c\\nd\\r", ExportContentComposer.escapeJson("a\\b\"c\nd\r"));
    }

    private static ExportService.ExportContext context(ExportService.ExportEntry... entries) {
        return new ExportService.ExportContext(Path.of("reports"), "2.0.1", List.of(entries));
    }
}
