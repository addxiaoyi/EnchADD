package net.enchadd.mod;

import net.enchadd.mod.common.StonewakeBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

final class EnchADDNeoForgeStonewake {
    static void register() { NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, EnchADDNeoForgeStonewake::onDrops); }
    private static void onDrops(BlockDropsEvent event) {
        if (event.getBreaker() instanceof ServerPlayer player) {
            StonewakeBehavior.onBreak(player, event.getLevel().getBlockState(event.getPos()), event.getTool());
        }
    }
}
