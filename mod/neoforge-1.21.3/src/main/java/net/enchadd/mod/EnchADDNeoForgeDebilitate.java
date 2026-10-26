package net.enchadd.mod;

import net.enchadd.mod.common.DebilitateBehavior;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

final class EnchADDNeoForgeDebilitate {
    static void register() {
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, event ->
                DebilitateBehavior.onDamage(event.getEntity(), event.getSource(), event.getOriginalDamage()));
    }
}
