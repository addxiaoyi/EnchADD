package net.enchadd.mod;

import net.enchadd.mod.common.RashnessBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeRashness {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeRashness::onDamage); }
    private static void onDamage(LivingIncomingDamageEvent event) { RashnessBehavior.maybeApply(event.getEntity(), event.getSource()); }
}
