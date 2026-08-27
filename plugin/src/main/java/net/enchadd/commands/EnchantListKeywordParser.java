package net.enchadd.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class EnchantListKeywordParser {

    private EnchantListKeywordParser() {
    }

    static List<String> parse(String value) {
        List<String> keywords = new ArrayList<>();
        if (value == null) {
            return keywords;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            String keyword = part.trim();
            if (!keyword.isEmpty()) {
                keywords.add(keyword.toLowerCase(Locale.ROOT));
            }
        }
        return keywords;
    }
}
