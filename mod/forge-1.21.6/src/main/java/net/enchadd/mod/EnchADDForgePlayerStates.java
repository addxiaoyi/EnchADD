package net.enchadd.mod;

import net.enchadd.mod.common.PlayerStateBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgePlayerStates {
    static void register() { TickEvent.PlayerTickEvent.BUS.addListener(new EnchADDForgePlayerStates()::onTick); }
    @SubscribeEvent public void onTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player) PlayerStateBehavior.tick(player);
    }
}
