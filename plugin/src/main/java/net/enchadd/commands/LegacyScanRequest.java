package net.enchadd.commands;

import java.util.Locale;

record LegacyScanRequest(String filter, boolean summaryOnly) {

    static LegacyScanRequest from(String[] args) {
        String filter = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "";
        boolean summaryOnly = args.length >= 3 && "summary".equalsIgnoreCase(args[2]);
        return new LegacyScanRequest(filter, summaryOnly);
    }

    String displayFilter() {
        return filter.isBlank() ? "*" : filter;
    }
}
