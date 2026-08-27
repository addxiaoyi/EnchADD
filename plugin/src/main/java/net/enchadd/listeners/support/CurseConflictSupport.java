package net.enchadd.listeners.support;

import net.enchadd.EnchADDConfig;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class CurseConflictSupport {

    public void filterEnchantRoll(@NotNull ItemStack item, @NotNull Map<Enchantment, Integer> toAdd) {
        if (toAdd.isEmpty()) {
            return;
        }

        int existingCurses = countCurses(item.getEnchantments().keySet());
        List<Enchantment> cursesToAdd = collectCursesToAdd(toAdd.keySet());
        handleCurseLimit(toAdd, existingCurses, cursesToAdd);
        if (toAdd.isEmpty()) {
            return;
        }

        filterIncompatibleWithExisting(item.getEnchantments().keySet(), toAdd);
        if (toAdd.size() > 1) {
            removeMutuallyIncompatible(toAdd);
        }
    }

    public boolean shouldRejectAnvilResult(@Nullable ItemStack left,
                                    @Nullable ItemStack right,
                                    @NotNull Map<Enchantment, Integer> resultEnchants) {
        int cursesOnResult = countCurses(resultEnchants.keySet());
        if (cursesOnResult > 1 && (hasCurse(left) || hasCurse(right))) {
            return true;
        }
        return hasIncompatiblePair(resultEnchants.keySet());
    }

    private @NotNull List<Enchantment> collectCursesToAdd(@NotNull Collection<Enchantment> enchantments) {
        List<Enchantment> cursesToAdd = new ArrayList<>();
        for (Enchantment enchantment : enchantments) {
            if (isCustomCurse(enchantment)) {
                cursesToAdd.add(enchantment);
            }
        }
        return cursesToAdd;
    }

    private void handleCurseLimit(@NotNull Map<Enchantment, Integer> toAdd,
                                  int existingCurses,
                                  @NotNull List<Enchantment> cursesToAdd) {
        if (cursesToAdd.isEmpty()) {
            return;
        }
        if (existingCurses > 0) {
            for (Enchantment curse : cursesToAdd) {
                toAdd.remove(curse);
            }
            return;
        }

        Enchantment keeper = cursesToAdd.getFirst();
        int highestLevel = toAdd.getOrDefault(keeper, 0);
        for (int i = 1; i < cursesToAdd.size(); i++) {
            Enchantment curse = cursesToAdd.get(i);
            int level = toAdd.getOrDefault(curse, 0);
            if (level > highestLevel) {
                toAdd.remove(keeper);
                keeper = curse;
                highestLevel = level;
                continue;
            }
            toAdd.remove(curse);
        }
    }

    private void filterIncompatibleWithExisting(@NotNull Collection<Enchantment> existing,
                                                @NotNull Map<Enchantment, Integer> toAdd) {
        if (existing.isEmpty()) {
            return;
        }
        List<Enchantment> candidates = new ArrayList<>(toAdd.keySet());
        for (Enchantment enchantment : candidates) {
            if (isIncompatibleWithAny(enchantment, existing)) {
                toAdd.remove(enchantment);
            }
        }
    }

    private void removeMutuallyIncompatible(@NotNull Map<Enchantment, Integer> toAdd) {
        List<Enchantment> list = new ArrayList<>(toAdd.keySet());
        for (int i = 0; i < list.size(); i++) {
            Enchantment first = list.get(i);
            if (!toAdd.containsKey(first)) {
                continue;
            }
            for (int j = i + 1; j < list.size(); j++) {
                Enchantment second = list.get(j);
                if (!toAdd.containsKey(second) || !areIncompatible(first, second)) {
                    continue;
                }
                if (shouldPreferSecond(toAdd, first, second)) {
                    toAdd.remove(first);
                    break;
                }
                toAdd.remove(second);
            }
        }
    }

    private boolean shouldPreferSecond(@NotNull Map<Enchantment, Integer> toAdd,
                                       @Nullable Enchantment first,
                                       @Nullable Enchantment second) {
        int firstLevel = toAdd.getOrDefault(first, 0);
        int secondLevel = toAdd.getOrDefault(second, 0);
        if (secondLevel != firstLevel) {
            return secondLevel > firstLevel;
        }
        return enchantmentKey(second).compareTo(enchantmentKey(first)) < 0;
    }

    private @NotNull String enchantmentKey(@Nullable Enchantment enchantment) {
        if (enchantment == null) {
            return "";
        }
        NamespacedKey key = enchantment.getKey();
        return key == null ? "" : key.toString();
    }

    private boolean hasCurse(@Nullable ItemStack item) {
        return item != null && countCurses(item.getEnchantments().keySet()) > 0;
    }

    private int countCurses(@NotNull Collection<Enchantment> enchantments) {
        int count = 0;
        for (Enchantment enchantment : enchantments) {
            if (isCustomCurse(enchantment)) {
                count++;
            }
        }
        return count;
    }

    private boolean isCustomCurse(@Nullable Enchantment enchantment) {
        Key key = toAdventureKey(enchantment);
        return key != null && EnchADDConfig.isCurse(key);
    }

    private boolean isIncompatibleWithAny(@Nullable Enchantment enchantment,
                                          @NotNull Collection<Enchantment> others) {
        for (Enchantment other : others) {
            if (areIncompatible(enchantment, other)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasIncompatiblePair(@NotNull Collection<Enchantment> enchantments) {
        List<Enchantment> list = new ArrayList<>(enchantments);
        for (int i = 0; i < list.size(); i++) {
            Enchantment first = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                if (areIncompatible(first, list.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean areIncompatible(@Nullable Enchantment first, @Nullable Enchantment second) {
        Key a = toAdventureKey(first);
        Key b = toAdventureKey(second);
        return a != null && b != null && EnchADDConfig.areIncompatible(a, b);
    }

    private @Nullable Key toAdventureKey(@Nullable Enchantment enchantment) {
        if (enchantment == null) {
            return null;
        }
        NamespacedKey namespacedKey = enchantment.getKey();
        if (namespacedKey == null) {
            return null;
        }
        return Key.key(namespacedKey.getNamespace(), namespacedKey.getKey());
    }
}
