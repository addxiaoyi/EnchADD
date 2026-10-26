package net.enchadd.mod;

import net.enchadd.mod.common.RicochetBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeRicochet {
    static void register() { LivingDamageEvent.BUS.addListener(new EnchADDForgeRicochet()::onDamage); }
    @SubscribeEvent public void onDamage(LivingDamageEvent event) {
        RicochetBehavior.onHit(event.getEntity(), event.getSource());
    }
}
