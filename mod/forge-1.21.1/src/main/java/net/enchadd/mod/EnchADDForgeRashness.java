package net.enchadd.mod;

import net.enchadd.mod.common.RashnessBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeRashness {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeRashness()::onHurt); }
    @SubscribeEvent public void onHurt(LivingHurtEvent event) { RashnessBehavior.maybeApply(event.getEntity(), event.getSource()); }
}
