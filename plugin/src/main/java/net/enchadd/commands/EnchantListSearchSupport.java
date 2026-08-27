package net.enchadd.commands;

import net.enchadd.enchants.EnchADDEnchant;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class EnchantListSearchSupport {

    private EnchantListSearchSupport() {
    }

    static List<EnchADDEnchant> findByName(Iterable<EnchADDEnchant> enchants, String query) {
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        List<EnchADDEnchant> found = new ArrayList<>();
        for (EnchADDEnchant enchant : enchants) {
            String name = EnchantListFormatSupport.resolveName(enchant);
            if (name.toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                found.add(enchant);
            }
        }
        sortByName(found);
        return found;
    }

    static List<EnchADDEnchant> prepareListResults(Iterable<EnchADDEnchant> enchants, EnchantListOptions options) {
        List<EnchADDEnchant> filtered = filterListResults(enchants, options);
        List<EnchADDEnchant> keywordFiltered = filterByKeywords(filtered, options.keywords);
        return sortListResults(keywordFiltered, options);
    }

    static List<EnchADDEnchant> filterListResults(Iterable<EnchADDEnchant> enchants, EnchantListOptions options) {
        List<EnchADDEnchant> filtered = new ArrayList<>();
        for (EnchADDEnchant enchant : enchants) {
            if (matchesListFilters(enchant, options)) {
                filtered.add(enchant);
            }
        }
        return filtered;
    }

    static List<EnchADDEnchant> sortListResults(List<EnchADDEnchant> enchants, EnchantListOptions options) {
        List<EnchADDEnchant> sorted = new ArrayList<>(enchants);
        sortByOption(sorted, options);
        return sorted;
    }

    private static boolean matchesListFilters(EnchADDEnchant enchant, EnchantListOptions options) {
        boolean slotOk = options.slot == null || hasSlot(enchant, options.slot);
        boolean sourceTierOk = options.sourceTier == null || EnchantListDisplayMetadataSupport.sourceTier(enchant) == options.sourceTier;
        String full = enchant.getKey().asString();
        String namespace = full.contains(":") ? full.substring(0, full.indexOf(':')) : "";
        boolean namespaceOk = options.namespace == null || options.namespace.isEmpty() || namespace.equalsIgnoreCase(options.namespace);
        boolean weightOk = (options.minWeight == null || enchant.getWeight() >= options.minWeight) && (options.maxWeight == null || enchant.getWeight() <= options.maxWeight);
        boolean levelOk = (options.minLevel == null || enchant.getMaxLevel() >= options.minLevel) && (options.maxLevel == null || enchant.getMaxLevel() <= options.maxLevel);
        return slotOk && sourceTierOk && namespaceOk && weightOk && levelOk;
    }

    private static boolean hasSlot(EnchADDEnchant enchant, EquipmentSlotGroup slot) {
        for (EquipmentSlotGroup activeSlot : enchant.getActiveSlots()) {
            if (activeSlot == slot) {
                return true;
            }
        }
        return false;
    }

    private static void sortByOption(List<EnchADDEnchant> enchants, EnchantListOptions options) {
        switch (options.sort) {
            case "weight" -> enchants.sort((a, b) -> Integer.compare(a.getWeight(), b.getWeight()));
            case "max" -> enchants.sort((a, b) -> Integer.compare(a.getMaxLevel(), b.getMaxLevel()));
            default -> sortByName(enchants);
        }
        if ("desc".equals(options.order)) {
            Collections.reverse(enchants);
        }
    }

    static List<EnchADDEnchant> filterByKeywords(List<EnchADDEnchant> enchants, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return enchants;
        }
        List<EnchADDEnchant> found = new ArrayList<>();
        for (EnchADDEnchant enchant : enchants) {
            String name = EnchantListFormatSupport.resolveName(enchant).toLowerCase(Locale.ROOT);
            for (String keyword : keywords) {
                if (name.contains(keyword)) {
                    found.add(enchant);
                    break;
                }
            }
        }
        return found;
    }

    private static void sortByName(List<EnchADDEnchant> enchants) {
        if (enchants.size() < 2) {
            return;
        }
        List<NamedEnchant> decorated = new ArrayList<>(enchants.size());
        for (EnchADDEnchant enchant : enchants) {
            String name = EnchantListFormatSupport.resolveName(enchant);
            decorated.add(new NamedEnchant(enchant, name == null ? "" : name));
        }
        decorated.sort(Comparator.comparing(NamedEnchant::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(NamedEnchant::name));
        enchants.clear();
        for (NamedEnchant entry : decorated) {
            enchants.add(entry.enchant());
        }
    }

    private record NamedEnchant(EnchADDEnchant enchant, String name) {
    }
}
