package net.enchadd.commands;

import net.enchadd.enchants.EnchADDEnchant;

import java.util.ArrayList;
import java.util.List;

final class EnchantListPaginationSupport {

    private EnchantListPaginationSupport() {
    }

    static int[] adjustPageSize(int page, int size, int total) {
        int changed = 0;
        if (size < 1) {
            size = 1;
            changed = 1;
        }
        if (size > 100) {
            size = 100;
            changed = 1;
        }
        if (page < 1) {
            page = 1;
            changed = 1;
        }
        int totalPages = (int) Math.ceil(Math.max(total, 1) / (double) size);
        if (page > totalPages) {
            page = totalPages;
            changed = 1;
        }
        return new int[] {page, size, changed};
    }

    static List<String> mapNames(List<EnchADDEnchant> list, int page, int size) {
        int start = Math.max(0, (page - 1) * size);
        int end = Math.min(start + size, list.size());
        List<String> names = new ArrayList<>();
        for (int i = start; i < end; i++) {
            names.add(EnchantListFormatSupport.resolveName(list.get(i)));
        }
        return names;
    }

    static List<EnchADDEnchant> pageItems(List<EnchADDEnchant> list, int page, int size) {
        int start = Math.max(0, (page - 1) * size);
        int end = Math.min(start + size, list.size());
        List<EnchADDEnchant> pageItems = new ArrayList<>();
        for (int i = start; i < end; i++) {
            pageItems.add(list.get(i));
        }
        return pageItems;
    }
}
