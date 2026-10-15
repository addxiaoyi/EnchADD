package net.enchadd.mod;

import net.enchadd.mod.common.SteadfastBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeSteadfast {
    static void register() { LivingKnockBackEvent.BUS.addListener(new EnchADDForgeSteadfast()::onKnockback); }
    @SubscribeEvent public void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            event.setStrength((float) SteadfastBehavior.reduceStrength(player, event.getStrength()));
        }
    }
}
