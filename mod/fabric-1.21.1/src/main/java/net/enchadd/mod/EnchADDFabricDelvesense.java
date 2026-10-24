package net.enchadd.mod;

import net.enchadd.mod.common.DelvesenseBehavior;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResultHolder;

final class EnchADDFabricDelvesense {
    private EnchADDFabricDelvesense() {}

    static void init() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (level.isClientSide()) return InteractionResultHolder.pass(player.getItemInHand(hand));
            DelvesenseBehavior.activate((net.minecraft.server.level.ServerPlayer) player, player.getItemInHand(hand));
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });
    }
}
