package net.enchadd.mod;

import net.enchadd.mod.common.StonewakeBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeStonewake {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeStonewake()::onBreak); }
    @SubscribeEvent public void onBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel level && event.getPlayer() instanceof ServerPlayer player) {
            StonewakeBehavior.onBreak(player, event.getState(), player.getMainHandItem());
        }
    }
}
