package net.enchadd.mod;

import net.enchadd.mod.common.DispelBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeDispel {
    private EnchADDForgeDispel() {}

    static void register() { LivingDamageEvent.BUS.addListener(new EnchADDForgeDispel()::onDamage); }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        DispelBehavior.onDamage(event.getEntity(), event.getSource());
    }
}
