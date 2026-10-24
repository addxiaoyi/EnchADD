package net.enchadd.mod;

import net.enchadd.mod.common.DelvesenseBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

final class EnchADDNeoForgeDelvesense {
    static void register() { NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickItem.class, EnchADDNeoForgeDelvesense::onRightClick); }
    private static void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) DelvesenseBehavior.activate(player, player.getItemInHand(event.getHand()));
    }
}
