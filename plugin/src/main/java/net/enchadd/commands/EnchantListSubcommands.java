package net.enchadd.commands;

import java.util.Locale;
import java.util.Set;

final class EnchantListSubcommands {

    private static final Set<String> VALID = Set.of(
            "list",
            EnchantListCommand.SUB_EXPORT,
            "info",
            "help",
            EnchantListCommand.SUB_EXPORTJSON,
            EnchantListCommand.SUB_EXPORTCSV,
            "find",
            EnchantListCommand.SUB_CI,
            EnchantListCommand.SUB_PERF,
            EnchantListCommand.SUB_VERIFY,
            EnchantListCommand.SUB_BALANCE,
            EnchantListCommand.SUB_SAFEMODE,
            EnchantListCommand.SUB_RELOAD,
            EnchantListCommand.SUB_LEGACYSCAN
    );

    private EnchantListSubcommands() {
    }

    static boolean isValid(String[] args) {
        if (args.length == 0) {
            return false;
        }
        return VALID.contains(normalize(args[0]));
    }

    static String normalize(String raw) {
        return raw.toLowerCase(Locale.ROOT);
    }

    static boolean isExport(String sub) {
        return EnchantListCommand.SUB_EXPORT.equals(sub)
                || EnchantListCommand.SUB_EXPORTJSON.equals(sub)
                || EnchantListCommand.SUB_EXPORTCSV.equals(sub);
    }
}
