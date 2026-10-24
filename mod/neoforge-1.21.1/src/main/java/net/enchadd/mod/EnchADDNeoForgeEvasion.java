package net.enchadd.mod;

import net.enchadd.mod.common.EvasionBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeEvasion {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeEvasion::onDamage); }
    private static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && EvasionBehavior.shouldDodge(player, event.getSource())) {
            event.setCanceled(true);
        }
    }
}
