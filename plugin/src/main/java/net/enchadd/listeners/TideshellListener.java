package net.enchadd.listeners;

import net.enchadd.listeners.support.TideshellEffectSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.TideshellEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public final class TideshellListener implements Listener {

    private final Enchantment enchant;
    private final TideshellEnchant config;
    private final NamespacedKey cooldownKey;
    private final TideshellEffectSupport effectSupport;

    public TideshellListener() {
        this(resolveEnchant(TideshellEnchant.KEY), resolveConfig(),
                PerformanceUtils.namespacedKeyWithSuffix(TideshellEnchant.KEY, "_cooldown"));
    }

    public TideshellListener(@Nullable Enchantment enchant,
                             @Nullable TideshellEnchant config,
                             @NotNull NamespacedKey cooldownKey) {
        this.enchant = enchant;
        this.config = config;
        this.cooldownKey = cooldownKey;
        this.effectSupport = config == null ? null : new TideshellEffectSupport(config, cooldownKey);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTideshell(PlayerInteractEvent event) {
        if (enchant == null || config == null || effectSupport == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.NAUTILUS_SHELL) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(item, enchant);
        if (level <= 0 || !effectSupport.isTouchingWater(player)) {
            return;
        }
        if (!effectSupport.activate(player)) {
            return;
        }

        effectSupport.playCosmeticFeedback(player);
    }

    private static @Nullable Enchantment resolveEnchant(net.kyori.adventure.key.Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(key);
    }

    private static @Nullable TideshellEnchant resolveConfig() {
        EnchADDEnchant enchant = EnchADDConfig.ENCHANTS.get(TideshellEnchant.KEY);
        return enchant instanceof TideshellEnchant tideshellEnchant ? tideshellEnchant : null;
    }
}
