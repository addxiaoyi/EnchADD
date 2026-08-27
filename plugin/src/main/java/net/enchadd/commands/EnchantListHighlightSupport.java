package net.enchadd.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class EnchantListHighlightSupport {

    private EnchantListHighlightSupport() {
    }

    static Component highlightName(String name, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return Component.text(name);
        }
        Pattern pattern = Pattern.compile("(?i)(" + quotedAlternation(keywords) + ")");
        Matcher matcher = pattern.matcher(name);
        int pos = 0;
        Component result = Component.text("");
        while (matcher.find()) {
            if (matcher.start() > pos) {
                result = result.append(Component.text(name.substring(pos, matcher.start())));
            }
            result = result.append(Component.text(name.substring(matcher.start(), matcher.end())).color(NamedTextColor.YELLOW));
            pos = matcher.end();
        }
        if (pos < name.length()) {
            result = result.append(Component.text(name.substring(pos)));
        }
        return result;
    }

    private static String quotedAlternation(List<String> keywords) {
        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 0; i < keywords.size(); i++) {
            if (i > 0) {
                patternBuilder.append("|");
            }
            patternBuilder.append(Pattern.quote(keywords.get(i)));
        }
        return patternBuilder.toString();
    }
}
