package net.enchadd.listeners;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * 集中管理所有附魔监听器注册，避免主流程中堆积大量注册代码。
 */
public final class EnchantListenerRegistrar {

    private EnchantListenerRegistrar() {
    }

    public static void registerAll(@NotNull JavaPlugin plugin) {
        ListenerRegistrationAssembler.registerAll(plugin);
    }
}
