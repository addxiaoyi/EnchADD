package net.enchadd.mod;

import net.enchadd.mod.common.PoiseBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgePoise {
    private EnchADDForgePoise() {}

    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgePoise()::onDamage); }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        event.setAmount(PoiseBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getAmount()));
    }
}
