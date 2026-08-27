package net.enchadd.commands;

import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;

final class EnchantListAdminIdentitySupport {

    boolean translationReadyForVerify(String keyArg) {
        String name = resolveCiEnchantName(keyArg);
        return isTranslatedName(name);
    }

    String resolveCiEnchantName(String keyArg) {
        EnchADDEnchant enchant = EnchantQueryService.findEnchantByArg(EnchADDConfig.ENCHANTS, keyArg);
        if (enchant == null) {
            return "missing";
        }
        return EnchantListFormatSupport.resolveName(enchant);
    }

    static boolean isTranslatedName(String name) {
        return name != null && !name.isBlank()
                && !name.startsWith("EnchADD.enchant.")
                && !name.startsWith("enchantment.");
    }

    static String sanitizeMetricValue(String value) {
        if (value == null || value.isBlank()) {
            return "missing";
        }
        return value.replaceAll("\\s+", "_").replace('=', '-');
    }
}
