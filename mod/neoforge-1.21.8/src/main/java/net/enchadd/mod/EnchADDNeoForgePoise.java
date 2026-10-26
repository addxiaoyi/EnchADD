package net.enchadd.mod;

import net.enchadd.mod.common.PoiseBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgePoise {
    private EnchADDNeoForgePoise() {}

    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgePoise::onDamage); }

    private static void onDamage(LivingIncomingDamageEvent event) {
        event.setAmount(PoiseBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getAmount()));
    }
}
