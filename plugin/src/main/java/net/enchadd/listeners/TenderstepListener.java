package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.TenderstepEnchant;
import net.enchadd.listeners.support.TenderstepProtectionSupport;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class TenderstepListener implements Listener {

    private final Enchantment enchant;
    private final TenderstepEnchant config;
    private final TenderstepProtectionSupport protectionSupport;

    public TenderstepListener() {
        this(resolveEnchant(TenderstepEnchant.KEY), resolveConfig());
    }

    public TenderstepListener(@Nullable Enchantment enchant, @Nullable TenderstepEnchant config) {
        this.enchant = enchant;
        this.config = config;
        this.protectionSupport = new TenderstepProtectionSupport();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFragileBlockStep(EntityChangeBlockEvent event) {
        if (enchant == null || config == null) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Material type = event.getBlock().getType();
        if (!protectionSupport.isProtectedBlock(type, config)) {
            return;
        }
        if (!protectionSupport.hasTenderstepBoots(player, enchant)) {
            return;
        }

        event.setCancelled(true);
    }

    private static @Nullable Enchantment resolveEnchant(net.kyori.adventure.key.Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(key);
    }

    private static @Nullable TenderstepEnchant resolveConfig() {
        Object enchantObj = EnchADDConfig.ENCHANTS.get(TenderstepEnchant.KEY);
        return enchantObj instanceof TenderstepEnchant tenderstepEnchant ? tenderstepEnchant : null;
    }
}
