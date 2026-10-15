package net.enchadd.mod;

import net.enchadd.mod.common.BulwarkBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricBulwark {
    private EnchADDFabricBulwark() {}
    static void init() { ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> { BulwarkBehavior.maybeApply(entity); return true; }); }
}
