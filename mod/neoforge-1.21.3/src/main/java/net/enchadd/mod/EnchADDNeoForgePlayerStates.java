package net.enchadd.mod;

import net.enchadd.mod.common.PlayerStateBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

final class EnchADDNeoForgePlayerStates {
    static void register() { NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, EnchADDNeoForgePlayerStates::onTick); }
    private static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) PlayerStateBehavior.tick(player);
    }
}
