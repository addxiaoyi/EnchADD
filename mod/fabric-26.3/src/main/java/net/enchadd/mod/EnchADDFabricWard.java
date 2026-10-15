package net.enchadd.mod;

import net.enchadd.mod.common.WardBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricWard {
    private EnchADDFabricWard() {}
    static void init() { ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !WardBehavior.block(entity, amount)); }
}
