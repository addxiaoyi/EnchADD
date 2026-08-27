package net.enchadd.commands;

import java.util.Map;

final class CiStatusLegacySupport {

    private CiStatusLegacySupport() {
    }

    static String summarizeLegacyKeys(Map<String, Long> counts) {
        if (counts.isEmpty()) {
            return "none";
        }
        StringBuilder out = new StringBuilder();
        int emitted = 0;
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            if (emitted >= 5) {
                break;
            }
            if (!out.isEmpty()) {
                out.append(',');
            }
            out.append(entry.getKey()).append(':').append(entry.getValue());
            emitted++;
        }
        return out.toString();
    }
}
