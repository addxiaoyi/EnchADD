package net.enchadd.commands;

import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.Locale;

final class EnchantListOptionParser {

    private EnchantListOptionParser() {
    }

    static EnchantListOptions parse(String[] args, int fromIdx) {
        EnchantListOptions options = new EnchantListOptions();
        for (int i = Math.max(0, fromIdx); i < args.length; i++) {
            ParsedOption parsed = ParsedOption.from(args[i]);
            applyOption(options, parsed.key(), parsed.value());
        }
        return options;
    }

    static Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static void applyOption(EnchantListOptions options, String key, String value) {
        switch (key) {
            case "sort" -> applySort(options, value);
            case "order" -> applyOrder(options, value);
            case "slot" -> applySlot(options, value);
            case "source", "tier" -> applySourceTier(options, value);
            case "minweight" -> options.minWeight = tryParseInt(value);
            case "maxweight" -> options.maxWeight = tryParseInt(value);
            case "minlevel" -> options.minLevel = tryParseInt(value);
            case "maxlevel" -> options.maxLevel = tryParseInt(value);
            case "namespace" -> options.namespace = value;
            case "keywords" -> options.keywords = EnchantListKeywordParser.parse(value);
            default -> {
                // Unknown list filters are ignored so old clients keep working.
            }
        }
    }

    private static void applySort(EnchantListOptions options, String value) {
        String sort = value.toLowerCase(Locale.ROOT);
        if (sort.equals("name") || sort.equals("weight") || sort.equals("max")) {
            options.sort = sort;
        }
    }

    private static void applyOrder(EnchantListOptions options, String value) {
        String order = value.toLowerCase(Locale.ROOT);
        if (order.equals("asc") || order.equals("desc")) {
            options.order = order;
        }
    }

    private static void applySlot(EnchantListOptions options, String value) {
        EquipmentSlotGroup group = EquipmentSlotGroup.getByName(value.toUpperCase(Locale.ROOT));
        if (group != null) {
            options.slot = group;
        }
    }

    private static void applySourceTier(EnchantListOptions options, String value) {
        EnchantListDisplayMetadataSupport.SourceTier tier = EnchantListDisplayMetadataSupport.parseSourceTier(value);
        if (tier != null) {
            options.sourceTier = tier;
        }
    }

    private record ParsedOption(String key, String value) {

        static ParsedOption from(String raw) {
            int eq = raw.indexOf('=');
            String key = eq > 0 ? raw.substring(0, eq) : raw;
            String value = eq > 0 ? raw.substring(eq + 1) : "";
            return new ParsedOption(key.toLowerCase(Locale.ROOT), value);
        }
    }
}
