package net.enchadd.listeners;

import net.enchadd.listeners.support.WaysongEffectSupport;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.WaysongEnchant;
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
public final class WaysongListener implements Listener {

    private final Enchantment enchant;
    private final WaysongEnchant config;
    private final NamespacedKey cooldownKey;
    private final WaysongEffectSupport effectSupport;

    public WaysongListener() {
        this(resolveEnchant(WaysongEnchant.KEY), resolveConfig(),
                PerformanceUtils.namespacedKeyWithSuffix(WaysongEnchant.KEY, "_cooldown"));
    }

    public WaysongListener(@Nullable Enchantment enchant,
                           @Nullable WaysongEnchant config,
                           @NotNull NamespacedKey cooldownKey) {
        this.enchant = enchant;
        this.config = config;
        this.cooldownKey = cooldownKey;
        this.effectSupport = config == null ? null : new WaysongEffectSupport(config, cooldownKey);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onWaysong(PlayerInteractEvent event) {
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
        if (item == null || item.getType() != Material.GOAT_HORN) {
            return;
        }

        int level = PerformanceUtils.getEnchantLevel(item, enchant);
        if (level <= 0) {
            return;
        }
        if (!effectSupport.startCooldown(player)) {
            return;
        }

        effectSupport.applyMarchBuffs(player);
        effectSupport.playCosmeticFeedback(player);
    }

    private static @Nullable Enchantment resolveEnchant(net.kyori.adventure.key.Key key) {
        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        return registry.get(key);
    }

    private static @Nullable WaysongEnchant resolveConfig() {
        EnchADDEnchant enchant = EnchADDConfig.ENCHANTS.get(WaysongEnchant.KEY);
        return enchant instanceof WaysongEnchant waysongEnchant ? waysongEnchant : null;
    }
}
