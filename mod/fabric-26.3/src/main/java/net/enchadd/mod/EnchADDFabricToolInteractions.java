package net.enchadd.mod;

import net.enchadd.mod.common.ToolInteractionBehavior;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

final class EnchADDFabricToolInteractions {
    private EnchADDFabricToolInteractions() {}
    static void init() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide()) return InteractionResult.PASS;
            var tool = player.getItemInHand(hand);
            ToolInteractionBehavior.arborist(level, serverPlayer, hit.getBlockPos(), level.getBlockState(hit.getBlockPos()), tool);
            ToolInteractionBehavior.trailblazer(level, serverPlayer, hit.getBlockPos(), hit.getDirection(), tool);
            return InteractionResult.PASS;
        });
    }
}
