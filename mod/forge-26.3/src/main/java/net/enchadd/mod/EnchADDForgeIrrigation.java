package net.enchadd.mod;

import net.enchadd.mod.common.IrrigationBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeIrrigation {
    static void register() { PlayerInteractEvent.RightClickBlock.BUS.addListener(new EnchADDForgeIrrigation()::onRightClick); }
    @SubscribeEvent public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IrrigationBehavior.hydrate(event.getLevel(), player, event.getPos(), player.getItemInHand(event.getHand()));
        }
    }
}
