package net.enchadd.mod;

import net.enchadd.mod.common.ClairvoyanceBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeClairvoyance {
    private EnchADDNeoForgeClairvoyance() {}

    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeClairvoyance::onDamage); }

    private static void onDamage(LivingIncomingDamageEvent event) {
        ClairvoyanceBehavior.onDamage(event.getEntity(), event.getSource());
    }
}
