package net.enchadd.mod;

import net.enchadd.mod.common.RicochetBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

final class EnchADDNeoForgeRicochet {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, EnchADDNeoForgeRicochet::onDamage); }
    private static void onDamage(LivingDamageEvent.Post event) { RicochetBehavior.onHit(event.getEntity(), event.getSource()); }
}
