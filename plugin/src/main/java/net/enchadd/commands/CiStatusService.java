package net.enchadd.commands;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

final class CiStatusService {

    private CiStatusService() {
    }

    static String build(JavaPlugin plugin,
                        Function<String, String> resolveCiEnchantName,
                        Function<String, Boolean> translationReadyForVerify) {
        return CiStatusComposer.compose(plugin, resolveCiEnchantName, translationReadyForVerify);
    }
}
