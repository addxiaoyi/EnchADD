package net.enchadd.mod;

import net.enchadd.mod.common.DebilitateBehavior;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeDebilitate {
    static void register() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeDebilitate()::onDamage);
    }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        DebilitateBehavior.onDamage(event.getEntity(), event.getSource(), event.getAmount());
    }
}
