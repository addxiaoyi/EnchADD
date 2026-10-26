package net.enchadd.mod;

import net.enchadd.mod.common.ToolInteractionBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

final class EnchADDNeoForgeToolInteractions {
    static void register() { NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, EnchADDNeoForgeToolInteractions::onRightClick); }
    private static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var pos = event.getPos();
        var tool = player.getItemInHand(event.getHand());
        ToolInteractionBehavior.arborist(event.getLevel(), player, pos, event.getLevel().getBlockState(pos), tool);
        ToolInteractionBehavior.trailblazer(event.getLevel(), player, pos, event.getFace(), tool);
    }
}
