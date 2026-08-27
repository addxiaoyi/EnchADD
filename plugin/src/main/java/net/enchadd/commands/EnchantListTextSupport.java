package net.enchadd.commands;

import net.enchadd.utils.LangManager;
import net.kyori.adventure.text.Component;

final class EnchantListTextSupport {

    private EnchantListTextSupport() {
    }

    static String localized(String key, String fallback) {
        String translated = LangManager.get(key);
        return translated.equals(key) ? fallback : translated;
    }

    static Component text(String key, String fallback) {
        return Component.text(localized(key, fallback));
    }
}
