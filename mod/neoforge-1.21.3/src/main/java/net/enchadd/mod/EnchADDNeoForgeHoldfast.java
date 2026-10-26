package net.enchadd.mod;

import net.enchadd.mod.common.HoldfastBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeHoldfast {
    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeHoldfast::onDamage);
    }

    private static void onDamage(LivingIncomingDamageEvent event) {
        HoldfastBehavior.maybeSuppress(event.getEntity(), event.getSource());
    }
}
