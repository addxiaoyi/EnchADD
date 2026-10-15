package net.enchadd.mod;

import net.enchadd.mod.common.BarrierBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

final class EnchADDNeoForgeBarrier {
    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, EnchADDNeoForgeBarrier::onDamage);
    }

    private static void onDamage(LivingDamageEvent.Post event) {
        BarrierBehavior.pulse(event.getEntity(), event.getSource(), event.getBlockedDamage());
    }
}
