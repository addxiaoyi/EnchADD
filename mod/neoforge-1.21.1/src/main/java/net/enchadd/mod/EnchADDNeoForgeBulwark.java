package net.enchadd.mod;

import net.enchadd.mod.common.BulwarkBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

final class EnchADDNeoForgeBulwark {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, EnchADDNeoForgeBulwark::onDamage); }
    private static void onDamage(LivingDamageEvent.Post event) {
        if (event.getBlockedDamage() > 0.0f && event.getEntity() instanceof ServerPlayer player) {
            BulwarkBehavior.maybeApply(player);
        }
    }
}
