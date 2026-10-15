package net.enchadd.mod;

import net.enchadd.mod.common.HoldfastBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricHoldfast {
    private EnchADDFabricHoldfast() {
    }

    static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            HoldfastBehavior.maybeSuppress(entity, source);
            return true;
        });
    }
}
