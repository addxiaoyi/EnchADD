package net.enchadd.mod;

import net.enchadd.mod.common.RicochetBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricRicochet {
    private EnchADDFabricRicochet() {}
    static void init() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> RicochetBehavior.onHit(entity, source));
    }
}
