package net.enchadd.mod;

import net.enchadd.mod.common.ProjectileStatusBehavior;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

final class EnchADDFabricProjectileStatus {
    private EnchADDFabricProjectileStatus() {}
    static void init() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, base, taken, blocked) ->
                ProjectileStatusBehavior.applyOnHit(entity, source, base));
    }
}
