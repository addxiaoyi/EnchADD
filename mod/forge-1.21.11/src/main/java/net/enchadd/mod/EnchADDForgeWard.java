package net.enchadd.mod;

import net.enchadd.mod.common.WardBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeWard {
    static void register() { LivingHurtEvent.BUS.addListener(new EnchADDForgeWard()::onHurt); }
    @SubscribeEvent public boolean onHurt(LivingHurtEvent event) {
        return WardBehavior.block(event.getEntity(), event.getAmount());
    }
}
