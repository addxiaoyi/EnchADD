package net.enchadd.mod;

import net.enchadd.mod.common.SoulboundBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

final class EnchADDNeoForgeSoulbound {
    private EnchADDNeoForgeSoulbound() {}
    static void register() { NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, EnchADDNeoForgeSoulbound::onDeath); }
    private static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) SoulboundBehavior.capture(player);
    }
}
