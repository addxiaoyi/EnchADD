/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.papermc.paper.registry.RegistryAccess
 *  io.papermc.paper.registry.RegistryKey
 *  org.bukkit.Bukkit
 *  org.bukkit.Registry
 *  org.bukkit.enchantments.Enchantment
 *  org.bukkit.entity.Item
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.EventPriority
 *  org.bukkit.event.Listener
 *  org.bukkit.event.block.BlockDropItemEvent
 *  org.bukkit.event.player.PlayerTeleportEvent$TeleportCause
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.plugin.Plugin
 *  org.jetbrains.annotations.NotNull
 */
package net.enchadd.listeners;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.UUID;
import net.enchadd.EnchADD;
import net.enchadd.EnchADDConfig;
import net.enchadd.enchants.EnchADDEnchant;
import net.enchadd.enchants.TelepathyEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class TelepathyListener
implements Listener {
    private final Registry<@NotNull Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
    private final Enchantment telepathy = (Enchantment)this.registry.get(TelepathyEnchant.KEY);
    private final TelepathyEnchant config;

    public TelepathyListener() {
        EnchADDEnchant enchantObj = EnchADDConfig.ENCHANTS.get(TelepathyEnchant.KEY);
        this.config = enchantObj instanceof TelepathyEnchant ? (TelepathyEnchant)enchantObj : null;
    }

    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    public void onTelepathyTool(BlockDropItemEvent event) {
        if (this.telepathy == null || this.config == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!PerformanceUtils.isPlayerValid(player)) {
            return;
        }
        if (player.getInventory() == null) {
            return;
        }
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (PerformanceUtils.getEnchantLevel(tool, this.telepathy) <= 0) {
            return;
        }
        if (event.getItems() == null || event.getItems().isEmpty()) {
            return;
        }
        ArrayList<Item> droppedItems = new ArrayList<Item>();
        UUID ownerId = player.getUniqueId();
        for (Item item : event.getItems()) {
            if (item == null || !item.isValid()) continue;
            item.setPickupDelay(0);
            if (this.config.isOnlyUserCanPickupItems()) {
                item.setOwner(ownerId);
            }
            droppedItems.add(item);
        }
        if (droppedItems.isEmpty()) {
            return;
        }
        Bukkit.getScheduler().runTask((Plugin)EnchADD.getPlugin(EnchADD.class), () -> {
            if (!PerformanceUtils.isPlayerValid(player)) {
                return;
            }
            if (player.getWorld() == null) {
                return;
            }
            for (Item item : droppedItems) {
                if (item == null || !item.isValid() || item.isDead() || !item.getWorld().equals((Object)player.getWorld())) continue;
                item.teleport(player.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }
        });
    }
}
