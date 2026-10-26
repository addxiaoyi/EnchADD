package net.enchadd.mod;

import net.enchadd.mod.common.BreakguardBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeBreakguard {
    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeBreakguard::onDamage);
    }

    private static void onDamage(LivingIncomingDamageEvent event) {
        event.setAmount(BreakguardBehavior.adjustIncoming(
                event.getEntity(), event.getSource(), event.getAmount()));
    }
}
