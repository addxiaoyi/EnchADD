package net.enchadd.mod;

import net.enchadd.mod.common.BarrierBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeBarrier {
    static void register() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeBarrier()::onDamage);
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        BarrierBehavior.pulse(event.getEntity(), event.getSource(),
                event.getEntity().isBlocking() ? event.getAmount() : 0.0f);
    }
}
