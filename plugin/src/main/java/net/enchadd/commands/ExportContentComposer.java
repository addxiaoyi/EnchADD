package net.enchadd.commands;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ExportContentComposer {

    private static Clock clock = Clock.systemDefaultZone();

    private ExportContentComposer() {
    }

    static List<String> markdown(ExportService.ExportContext context) {
        List<String> names = new ArrayList<>();
        for (ExportService.ExportEntry entry : context.entries()) {
            names.add(entry.name());
        }
        Collections.sort(names);

        List<String> md = new ArrayList<>();
        md.add("Enchantment List");
        for (String name : names) {
            md.add("- " + name);
        }
        return md;
    }

    static String json(ExportService.ExportContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (ExportService.ExportEntry entry : context.entries()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("{");
            sb.append("\"name\":\"").append(escapeJson(entry.name())).append("\",");
            sb.append("\"key\":\"").append(escapeJson(entry.key())).append("\",");
            sb.append("\"maxLevel\":").append(entry.maxLevel()).append(",");
            sb.append("\"weight\":").append(entry.weight()).append(",");
            sb.append("\"anvilCost\":").append(entry.anvilCost()).append(",");
            sb.append("\"slots\":\"").append(escapeJson(entry.slots())).append("\"");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    static String metaJson(ExportService.ExportContext context) {
        String generatedAt = OffsetDateTime.now(clock).toString();
        return "{\"generatedAt\":\"" + escapeJson(generatedAt) + "\",\"version\":\"" + escapeJson(context.version()) + "\"}";
    }

    static List<String> csv(ExportService.ExportContext context) {
        List<String> rows = new ArrayList<>();
        rows.add("# generatedAt=" + OffsetDateTime.now(clock));
        rows.add("# version=" + context.version());
        rows.add("name,key,maxLevel,weight,anvilCost,slots");
        for (ExportService.ExportEntry entry : context.entries()) {
            rows.add(escapeCsv(entry.name()) + ","
                    + escapeCsv(entry.key()) + ","
                    + entry.maxLevel() + ","
                    + entry.weight() + ","
                    + entry.anvilCost() + ","
                    + escapeCsv(entry.slots()));
        }
        return rows;
    }

    static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    static String escapeCsv(String s) {
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        String v = s.replace("\"", "\"\"");
        if (needQuotes) {
            return "\"" + v + "\"";
        }
        return v;
    }

    static void useClockForTesting(Clock testingClock) {
        clock = testingClock;
    }

    static void resetClockForTesting() {
        clock = Clock.systemDefaultZone();
    }
}
