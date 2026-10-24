package net.enchadd.mod;

import net.enchadd.mod.common.ToolInteractionBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeToolInteractions {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeToolInteractions()::onRightClick); }
    @SubscribeEvent public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var pos = event.getPos();
        var tool = player.getItemInHand(event.getHand());
        ToolInteractionBehavior.arborist(event.getLevel(), player, pos, event.getLevel().getBlockState(pos), tool);
        ToolInteractionBehavior.trailblazer(event.getLevel(), player, pos, event.getFace(), tool);
    }
}
