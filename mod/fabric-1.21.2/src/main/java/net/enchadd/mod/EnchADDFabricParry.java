package net.enchadd.mod;

import net.enchadd.mod.common.ParryBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricParry {
    private EnchADDFabricParry() {
    }

    static void init() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> {
            if (blocked) ParryBehavior.arm(entity, source, base);
        });
    }
}
