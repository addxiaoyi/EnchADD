package net.enchadd.mod;

import net.enchadd.mod.common.BraceBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

final class EnchADDNeoForgeBrace {
    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingKnockBackEvent.class, EnchADDNeoForgeBrace::onKnockback);
    }

    private static void onKnockback(LivingKnockBackEvent event) {
        event.setStrength(BraceBehavior.reduceStrength(event.getEntity(), event.getStrength()));
    }
}
