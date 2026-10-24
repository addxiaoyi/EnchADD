package net.enchadd.mod;

import net.enchadd.mod.common.BraceBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeBrace {
    static void register() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeBrace()::onKnockback);
    }

    @SubscribeEvent
    public void onKnockback(LivingKnockBackEvent event) {
        event.setStrength(BraceBehavior.reduceStrength(event.getEntity(), event.getStrength()));
    }
}
