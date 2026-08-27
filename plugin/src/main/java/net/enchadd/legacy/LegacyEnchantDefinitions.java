package net.enchadd.legacy;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LegacyEnchantDefinitions {

    private static final String RESOURCE_NAME = "legacy-enchants.json";
    private static final Pattern ENTRY_PATTERN = Pattern.compile(
            "\\{\\s*\"key\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"displayName\"\\s*:\\s*\"([^\"]+)\"(?:\\s*,\\s*\"migrationTarget\"\\s*:\\s*\"([^\"]+)\")?\\s*,\\s*\"supportedItemTags\"\\s*:\\s*\\[(.*?)]\\s*}",
            Pattern.DOTALL
    );
    private static final Pattern ARRAY_VALUE_PATTERN = Pattern.compile("\"([^\"]+)\"");

    private static final List<LegacyEnchantDefinition> ENTRIES = loadEntries();
    private static final Map<Key, LegacyEnchantDefinition> BY_KEY = buildByKey();

    private LegacyEnchantDefinitions() {
    }

    public static @NotNull List<LegacyEnchantDefinition> entries() {
        return ENTRIES;
    }

    public static @NotNull Collection<Key> keys() {
        return BY_KEY.keySet();
    }

    public static LegacyEnchantDefinition get(@NotNull Key key) {
        return BY_KEY.get(key);
    }

    private static Map<Key, LegacyEnchantDefinition> buildByKey() {
        Map<Key, LegacyEnchantDefinition> byKey = new LinkedHashMap<>();
        for (LegacyEnchantDefinition entry : ENTRIES) {
            byKey.put(entry.key(), entry);
        }
        return Collections.unmodifiableMap(byKey);
    }

    private static List<LegacyEnchantDefinition> loadEntries() {
        String text = readResource();
        List<LegacyEnchantDefinition> entries = new ArrayList<>();
        Matcher matcher = ENTRY_PATTERN.matcher(text);
        while (matcher.find()) {
            Key key = Key.key(matcher.group(1));
            String displayName = matcher.group(2);
            String migrationTargetRaw = matcher.group(3);
            Key migrationTarget = migrationTargetRaw == null || migrationTargetRaw.isBlank() ? null : Key.key(migrationTargetRaw);
            List<String> supportedItemTags = parseStringArray(matcher.group(4));
            entries.add(new LegacyEnchantDefinition(key, displayName, migrationTarget, List.copyOf(supportedItemTags)));
        }
        if (entries.isEmpty()) {
            throw new IllegalStateException("No legacy enchant definitions loaded from " + RESOURCE_NAME);
        }
        return List.copyOf(entries);
    }

    private static List<String> parseStringArray(String raw) {
        List<String> values = new ArrayList<>();
        Matcher matcher = ARRAY_VALUE_PATTERN.matcher(raw);
        while (matcher.find()) {
            values.add(matcher.group(1));
        }
        return values;
    }

    private static String readResource() {
        try (InputStream stream = LegacyEnchantDefinitions.class.getClassLoader().getResourceAsStream(RESOURCE_NAME)) {
            if (stream == null) {
                throw new IllegalStateException("Missing resource " + RESOURCE_NAME);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                StringBuilder builder = new StringBuilder();
                char[] buffer = new char[4096];
                int read;
                while ((read = reader.read(buffer)) >= 0) {
                    builder.append(buffer, 0, read);
                }
                return builder.toString();
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read resource " + RESOURCE_NAME, ex);
        }
    }
}
