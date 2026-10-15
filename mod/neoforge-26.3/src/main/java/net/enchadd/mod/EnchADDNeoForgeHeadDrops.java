package net.enchadd.mod;

import net.enchadd.mod.common.HeadDropBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

final class EnchADDNeoForgeHeadDrops {
    private EnchADDNeoForgeHeadDrops() {}
    static void register() { NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, EnchADDNeoForgeHeadDrops::onDeath); }
    private static void onDeath(LivingDeathEvent event) {
        HeadDropBehavior.onDeath(event.getEntity(), event.getSource());
    }
}
