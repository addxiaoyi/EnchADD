package net.enchadd.mod;

import net.enchadd.mod.common.DebilitateBehavior;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeDebilitate {
    static void register() {
        LivingDamageEvent.BUS.addListener(new EnchADDForgeDebilitate()::onDamage);
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        DebilitateBehavior.onDamage(event.getEntity(), event.getSource(), event.getAmount());
    }
}
