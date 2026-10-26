package net.enchadd.mod;

import net.enchadd.mod.common.ClairvoyanceBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeClairvoyance {
    private EnchADDForgeClairvoyance() {}

    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeClairvoyance()::onDamage); }

    @SubscribeEvent
    public void onDamage(LivingDamageEvent event) {
        ClairvoyanceBehavior.onDamage(event.getEntity(), event.getSource());
    }
}
