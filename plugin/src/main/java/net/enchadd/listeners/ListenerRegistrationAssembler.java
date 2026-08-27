package net.enchadd.listeners;

import net.enchadd.utils.ListenerRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ListenerRegistrationAssembler {

    private ListenerRegistrationAssembler() {
    }

    public static void registerAll(@NotNull JavaPlugin plugin) {
        for (ListenerRegistrationPlan.Entry entry : ListenerRegistrationPlan.allEntries()) {
            ListenerRegistry.registerIfEnabled(entry.key(), () -> entry.factory().create(plugin));
        }
    }
}
