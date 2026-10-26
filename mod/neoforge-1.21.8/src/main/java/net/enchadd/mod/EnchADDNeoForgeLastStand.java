package net.enchadd.mod;

import net.enchadd.mod.common.LastStandBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

final class EnchADDNeoForgeLastStand {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, EnchADDNeoForgeLastStand::onDeath); }
    private static void onDeath(LivingDeathEvent event) { if (LastStandBehavior.rescue(event.getEntity())) event.setCanceled(true); }
}
