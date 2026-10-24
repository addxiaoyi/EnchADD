package net.enchadd.mod;

import net.enchadd.mod.common.EvasionBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

final class EnchADDFabricEvasion {
    private EnchADDFabricEvasion() {}
    static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) ->
                !(entity instanceof ServerPlayer player && EvasionBehavior.shouldDodge(player, source)));
    }
}
