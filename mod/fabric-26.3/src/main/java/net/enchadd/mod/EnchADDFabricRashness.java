package net.enchadd.mod;

import net.enchadd.mod.common.RashnessBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricRashness {
    private EnchADDFabricRashness() {}
    static void init() { ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) -> RashnessBehavior.maybeApply(entity, source)); }
}
