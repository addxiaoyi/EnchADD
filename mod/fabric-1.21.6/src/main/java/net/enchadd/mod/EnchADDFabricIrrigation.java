package net.enchadd.mod;

import net.enchadd.mod.common.IrrigationBehavior;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

final class EnchADDFabricIrrigation {
    private EnchADDFabricIrrigation() {}
    static void init() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (player instanceof ServerPlayer serverPlayer && !level.isClientSide()) {
                IrrigationBehavior.hydrate(level, serverPlayer, hit.getBlockPos(), player.getItemInHand(hand));
            }
            return InteractionResult.PASS;
        });
    }
}
