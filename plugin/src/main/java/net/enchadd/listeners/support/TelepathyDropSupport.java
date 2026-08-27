package net.enchadd.listeners.support;

import net.enchadd.enchants.TelepathyEnchant;
import net.enchadd.utils.PerformanceUtils;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class TelepathyDropSupport {

    private final TelepathyEnchant config;

    public TelepathyDropSupport(@NotNull TelepathyEnchant config) {
        this.config = config;
    }

    public @NotNull List<Item> collectDroppedItems(@Nullable Player player, @Nullable Collection<Item> items) {
        if (!PerformanceUtils.isPlayerValid(player) || items == null || items.isEmpty()) {
            return List.of();
        }

        UUID ownerId = player.getUniqueId();
        boolean restrictPickup = config.isOnlyUserCanPickupItems();
        List<Item> collected = new ArrayList<>(items.size());
        for (Item item : items) {
            if (item == null || !item.isValid()) {
                continue;
            }
            item.setPickupDelay(0);
            if (restrictPickup) {
                item.setOwner(ownerId);
            }
            collected.add(item);
        }
        return collected;
    }

    public void teleportCollectedItems(@Nullable Player player, @NotNull List<Item> droppedItems) {
        if (!PerformanceUtils.isPlayerValid(player) || player.getWorld() == null || droppedItems.isEmpty()) {
            return;
        }

        for (Item item : droppedItems) {
            if (item == null || !item.isValid() || item.isDead() || !player.getWorld().equals(item.getWorld())) {
                continue;
            }
            item.teleport(player.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }
}
