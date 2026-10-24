package net.enchadd.mod;

import net.enchadd.mod.common.FortitudeBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeFortitude {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeFortitude()::onHurt); }
    @SubscribeEvent public void onHurt(LivingHurtEvent event) { FortitudeBehavior.maybeApply(event.getEntity(), event.getAmount()); }
}
