package net.enchadd.legacy;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record LegacyEnchantDefinition(
        @NotNull Key key,
        @NotNull String displayName,
        @Nullable Key migrationTarget,
        @NotNull List<String> supportedItemTags
) {
    public boolean hasMigrationTarget() {
        return migrationTarget != null;
    }

    public @NotNull String sectionKey() {
        return key.value();
    }

    public @NotNull String sectionKeyWithoutCurseSuffix() {
        String sectionKey = sectionKey();
        if (sectionKey.endsWith("_curse")) {
            return sectionKey.substring(0, sectionKey.length() - "_curse".length());
        }
        return sectionKey;
    }
}
