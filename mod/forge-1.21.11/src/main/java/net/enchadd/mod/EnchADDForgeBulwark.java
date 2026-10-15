package net.enchadd.mod;

import net.enchadd.mod.common.BulwarkBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeBulwark {
    static void register() { LivingHurtEvent.BUS.addListener(new EnchADDForgeBulwark()::onHurt); }
    @SubscribeEvent public void onHurt(LivingHurtEvent event) { BulwarkBehavior.maybeApply(event.getEntity()); }
}
