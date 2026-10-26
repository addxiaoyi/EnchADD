package net.enchadd.mod;

import net.enchadd.mod.common.BulwarkBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

final class EnchADDNeoForgeBulwark {
    static void register() {
        // 1.21.11-style post-damage path: fires when the damage container carries a
        // positive blocked amount.
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, EnchADDNeoForgeBulwark::onDamage);
        // 1.21.1 damage pipeline keeps shield absorption in its own event, so the
        // same trigger is wired there as well.
        NeoForge.EVENT_BUS.addListener(LivingShieldBlockEvent.class, EnchADDNeoForgeBulwark::onShieldBlock);
    }
    private static void onDamage(LivingDamageEvent.Post event) {
        if (event.getBlockedDamage() > 0.0f && event.getEntity() instanceof ServerPlayer player) {
            BulwarkBehavior.maybeApply(player);
        }
    }
    private static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getBlockedDamage() > 0.0f && event.getEntity() instanceof ServerPlayer player) {
            BulwarkBehavior.maybeApply(player);
        }
    }
}
