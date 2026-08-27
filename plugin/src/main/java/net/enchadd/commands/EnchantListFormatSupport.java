package net.enchadd.commands;

import net.enchadd.enchants.EnchADDEnchant;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.ArrayList;
import java.util.List;

final class EnchantListFormatSupport {

    private EnchantListFormatSupport() {
    }

    static String resolveName(EnchADDEnchant enchant) {
        return enchant.getDescriptionText();
    }

    static String slotsToString(Iterable<EquipmentSlotGroup> slots) {
        List<String> names = new ArrayList<>();
        for (EquipmentSlotGroup slot : slots) {
            names.add(slot.toString());
        }
        return String.join(",", names);
    }
}
