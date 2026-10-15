package net.enchadd.mod;

import net.enchadd.mod.common.IrrigationBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

final class EnchADDNeoForgeIrrigation {
    static void register() { NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, EnchADDNeoForgeIrrigation::onRightClick); }
    private static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IrrigationBehavior.hydrate(event.getLevel(), player, event.getPos(), player.getItemInHand(event.getHand()));
        }
    }
}
