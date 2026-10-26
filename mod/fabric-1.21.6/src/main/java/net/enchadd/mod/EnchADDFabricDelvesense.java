package net.enchadd.mod;

import net.enchadd.mod.common.DelvesenseBehavior;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResult;

final class EnchADDFabricDelvesense {
    private EnchADDFabricDelvesense() {}

    static void init() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level.isClientSide()) return InteractionResult.PASS;
            DelvesenseBehavior.activate((net.minecraft.server.level.ServerPlayer) player, player.getItemInHand(hand));
            return InteractionResult.PASS;
        });
    }
}
