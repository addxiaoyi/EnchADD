package net.enchadd.mod;

import net.enchadd.mod.common.HomewardBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeHomeward {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeHomeward::onDamage); }
    private static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            event.setAmount(HomewardBehavior.adjustDamage(player, event.getSource(), event.getAmount()));
        }
    }
}
