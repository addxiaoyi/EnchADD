package net.enchadd.mod;

import net.enchadd.mod.common.MomentumBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

final class EnchADDNeoForgeMomentum {
    private EnchADDNeoForgeMomentum() {}

    static void register() { NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, EnchADDNeoForgeMomentum::onDeath); }

    private static void onDeath(LivingDeathEvent event) {
        MomentumBehavior.onDeath(event.getEntity(), event.getSource());
    }
}
