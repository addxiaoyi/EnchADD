package net.enchadd.mod;

import net.enchadd.mod.common.WardBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeWard {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeWard::onDamage); }
    private static void onDamage(LivingIncomingDamageEvent event) { event.setCanceled(WardBehavior.block(event.getEntity(), event.getAmount())); }
}
