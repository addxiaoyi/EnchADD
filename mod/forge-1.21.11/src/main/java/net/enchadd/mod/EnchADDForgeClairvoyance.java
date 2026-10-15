package net.enchadd.mod;

import net.enchadd.mod.common.ClairvoyanceBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeClairvoyance {
    private EnchADDForgeClairvoyance() {}

    static void register() { LivingDamageEvent.BUS.addListener(new EnchADDForgeClairvoyance()::onDamage); }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        ClairvoyanceBehavior.onDamage(event.getEntity(), event.getSource());
    }
}
