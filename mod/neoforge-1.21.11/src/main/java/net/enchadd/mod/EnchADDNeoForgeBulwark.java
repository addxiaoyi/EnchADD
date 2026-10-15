package net.enchadd.mod;

import net.enchadd.mod.common.BulwarkBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeBulwark {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeBulwark::onDamage); }
    private static void onDamage(LivingIncomingDamageEvent event) { BulwarkBehavior.maybeApply(event.getEntity()); }
}
