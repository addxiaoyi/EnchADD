package net.enchadd.mod;

import net.enchadd.mod.common.DelvesenseBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeDelvesense {
    static void register() { PlayerInteractEvent.RightClickItem.BUS.addListener(new EnchADDForgeDelvesense()::onRightClick); }
    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) DelvesenseBehavior.activate(player, player.getItemInHand(event.getHand()));
    }
}
