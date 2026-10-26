package net.enchadd.mod;

import net.enchadd.mod.common.StonewakeBehavior;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

final class EnchADDFabricStonewake {
    private EnchADDFabricStonewake() {}
    static void init() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel && player instanceof ServerPlayer serverPlayer) {
                StonewakeBehavior.onBreak(serverPlayer, state, serverPlayer.getMainHandItem());
            }
        });
    }
}
