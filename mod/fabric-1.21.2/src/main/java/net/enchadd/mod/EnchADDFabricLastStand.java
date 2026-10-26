package net.enchadd.mod;

import net.enchadd.mod.common.LastStandBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricLastStand {
    private EnchADDFabricLastStand() {}
    static void init() { ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, damage) -> !LastStandBehavior.rescue(entity)); }
}
