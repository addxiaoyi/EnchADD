package net.enchadd.mod;

import net.enchadd.mod.common.BarrierBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricBarrier {
    private EnchADDFabricBarrier() {
    }

    static void init() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) ->
                BarrierBehavior.pulse(entity, source, blocked ? base : 0.0f));
    }
}
