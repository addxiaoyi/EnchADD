package net.enchadd.mod;

import net.enchadd.mod.common.SteadfastBehavior;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

final class EnchADDNeoForgeSteadfast {
    static void register() { NeoForge.EVENT_BUS.addListener(LivingKnockBackEvent.class, EnchADDNeoForgeSteadfast::onKnockback); }
    private static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player) {
            event.setStrength((float) SteadfastBehavior.reduceStrength(player, event.getStrength()));
        }
    }
}
