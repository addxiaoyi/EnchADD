package net.enchadd.commands;

import net.enchadd.enchants.EnchADDEnchant;
import java.util.Map;

final class EnchantQueryService {

    private EnchantQueryService() {
    }

    static EnchADDEnchant findEnchantByArg(Map<net.kyori.adventure.key.Key, EnchADDEnchant> enchants, String arg) {
        String argLower = arg.toLowerCase();
        for (var entry : enchants.entrySet()) {
            String full = entry.getKey().asString();
            if (full.equalsIgnoreCase(arg)) {
                return entry.getValue();
            }
            int colonIdx = full.indexOf(':');
            String shortName = colonIdx >= 0 ? full.substring(colonIdx + 1) : full;
            if (shortName.equalsIgnoreCase(arg)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
