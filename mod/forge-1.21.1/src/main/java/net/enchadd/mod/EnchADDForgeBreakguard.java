package net.enchadd.mod;

import net.enchadd.mod.common.BreakguardBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeBreakguard {
    static void register() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeBreakguard()::onHurt);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        event.setAmount(BreakguardBehavior.adjustIncoming(
                event.getEntity(), event.getSource(), event.getAmount()));
    }
}
