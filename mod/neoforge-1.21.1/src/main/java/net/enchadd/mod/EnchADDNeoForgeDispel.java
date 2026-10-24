package net.enchadd.mod;

import net.enchadd.mod.common.DispelBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeDispel {
    private EnchADDNeoForgeDispel() {}

    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeDispel::onDamage); }

    private static void onDamage(LivingIncomingDamageEvent event) {
        DispelBehavior.onDamage(event.getEntity(), event.getSource());
    }
}
