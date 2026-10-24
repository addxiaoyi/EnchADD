package net.enchadd.mod;

import net.enchadd.mod.common.FortitudeBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricFortitude {
    private EnchADDFabricFortitude() {}
    static void init() { ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> { FortitudeBehavior.maybeApply(entity, amount); return true; }); }
}
