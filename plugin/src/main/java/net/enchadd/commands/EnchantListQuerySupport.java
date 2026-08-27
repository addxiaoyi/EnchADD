package net.enchadd.commands;

final class EnchantListQuerySupport {

    boolean hasLangArg(String[] args) {
        return args.length >= 2 && (args[1].equalsIgnoreCase("zh") || args[1].equalsIgnoreCase("en"));
    }

    int[] resolvePageSize(String[] args, boolean hasLang) {
        int base = hasLang ? 2 : 1;
        int page = args.length > base ? parseInt(args[base], 1) : 1;
        int size = args.length > base + 1 ? parseInt(args[base + 1], 10) : 10;
        return new int[]{page, size};
    }

    EnchantListOptions parseOptions(String[] args, int fromIdx) {
        return EnchantListOptionParser.parse(args, fromIdx);
    }

    Integer tryParseInt(String s) {
        return EnchantListOptionParser.tryParseInt(s);
    }

    int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

}
