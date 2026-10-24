package net.enchadd.mod;

import net.enchadd.mod.common.MomentumBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeMomentum {
    private EnchADDForgeMomentum() {}

    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeMomentum()::onDeath); }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        MomentumBehavior.onDeath(event.getEntity(), event.getSource());
    }
}
