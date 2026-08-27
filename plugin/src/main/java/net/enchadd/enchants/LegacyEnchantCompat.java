package net.enchadd.enchants;

import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.TagEntry;
import net.enchadd.EnchADDConfig;
import net.enchadd.legacy.LegacyEnchantDefinition;
import net.enchadd.legacy.LegacyEnchantDefinitions;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public final class LegacyEnchantCompat {

    private static final Set<EquipmentSlotGroup> LEGACY_ACTIVE_SLOTS = Set.of(EquipmentSlotGroup.ANY);
    private static final List<EnchADDEnchant> LEGACY_BOOTSTRAP_ENCHANTS = createLegacyBootstrapEnchants();

    private LegacyEnchantCompat() {
    }

    public static @NotNull Collection<EnchADDEnchant> bootstrapEnchants() {
        return LEGACY_BOOTSTRAP_ENCHANTS;
    }

    public static @NotNull Set<Key> legacyKeys() {
        return Set.copyOf(LegacyEnchantDefinitions.keys());
    }

    public static boolean isLegacyKey(@NotNull Key key) {
        return LegacyEnchantDefinitions.get(key) != null;
    }

    public static Key migrationTarget(@NotNull Key key) {
        LegacyEnchantDefinition definition = LegacyEnchantDefinitions.get(key);
        return definition == null ? null : definition.migrationTarget();
    }

    public static @NotNull String displayName(@NotNull Key key) {
        LegacyEnchantDefinition definition = LegacyEnchantDefinitions.get(key);
        return definition == null ? "Legacy Enchant (removed)" : definition.displayName();
    }

    private static List<EnchADDEnchant> createLegacyBootstrapEnchants() {
        List<EnchADDEnchant> enchants = new ArrayList<>();
        for (LegacyEnchantDefinition definition : LegacyEnchantDefinitions.entries()) {
            enchants.add(new LegacyTombstoneEnchant(definition));
        }
        return List.copyOf(enchants);
    }

    private static final class LegacyTombstoneEnchant extends AbstractEnchADDEnchant {

        private final String displayName;

        private LegacyTombstoneEnchant(@NotNull LegacyEnchantDefinition definition) {
            super(
                    definition.key(),
                    0,
                    1,
                    1,
                    EnchantmentRegistryEntry.EnchantmentCost.of(0, 0),
                    EnchantmentRegistryEntry.EnchantmentCost.of(0, 0),
                    List.<TagKey<Enchantment>>of(),
                    Collections.unmodifiableSet(EnchADDConfig.getItemTagEntriesFromList(definition.supportedItemTags())),
                    false,
                    "LEGACY"
            );
            this.displayName = definition.displayName();
        }

        @Override
        public @NotNull Iterable<EquipmentSlotGroup> getActiveSlots() {
            return LEGACY_ACTIVE_SLOTS;
        }

        @Override
        public @NotNull String getDescriptionText() {
            return displayName;
        }

        @Override
        public @NotNull Component getDescriptionComponent() {
            return Component.text(displayName);
        }
    }
}
