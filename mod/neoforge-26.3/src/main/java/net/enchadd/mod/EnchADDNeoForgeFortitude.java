package net.enchadd.mod;

import net.enchadd.mod.common.FortitudeBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeFortitude {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeFortitude::onDamage); }
    private static void onDamage(LivingIncomingDamageEvent event) { FortitudeBehavior.maybeApply(event.getEntity(), event.getAmount()); }
}
