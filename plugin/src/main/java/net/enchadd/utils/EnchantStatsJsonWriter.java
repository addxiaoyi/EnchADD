package net.enchadd.utils;

import java.time.format.DateTimeFormatter;
import java.util.Map;

final class EnchantStatsJsonWriter {

    private EnchantStatsJsonWriter() {
    }

    static void appendJsonLine(StringBuilder sb, String sessionId, DateTimeFormatter iso, EnchantStatsSnapshot snapshot) {
        sb.append("{");
        sb.append("\"session\":\"").append(sessionId).append("\",");
        sb.append("\"time\":\"").append(snapshot.time().format(iso)).append("\",");
        if (snapshot.shutdown()) {
            sb.append("\"shutdown\":true,");
        }
        sb.append("\"counts\":{");
        boolean first = true;
        for (Map.Entry<String, Long> entry : snapshot.counts().entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
        }
        sb.append("}}");
    }

    static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
