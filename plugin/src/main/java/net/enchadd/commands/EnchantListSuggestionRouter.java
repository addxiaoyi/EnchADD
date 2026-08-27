package net.enchadd.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class EnchantListSuggestionRouter {

    private final EnchantListCommandSupport support;

    EnchantListSuggestionRouter(EnchantListCommandSupport support) {
        this.support = support;
    }

    Collection<String> suggest(String[] args) {
        if (args.length == 0) {
            return new ArrayList<>();
        }
        if (args.length == 1) {
            return support.suggestRoot();
        }

        String sub = EnchantListSubcommands.normalize(args[0]);
        if ("list".equals(sub)) {
            return support.completeList(args);
        }
        if (EnchantListSubcommands.isExport(sub)) {
            return support.suggestLang();
        }
        if ("info".equals(sub)) {
            return support.completeInfo(args.length >= 2 ? args[1] : "");
        }
        if ("find".equals(sub)) {
            return args.length == 2 ? support.completeInfo(args[1]) : support.suggestLang();
        }
        if (EnchantListCommand.SUB_PERF.equals(sub)) {
            return support.suggestPerf(args);
        }
        if (EnchantListCommand.SUB_SAFEMODE.equals(sub)) {
            return support.suggestSafeMode(args);
        }
        return List.of();
    }
}
