package net.enchadd.mod;

import net.enchadd.mod.common.LastStandBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeLastStand {
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeLastStand()::onDeath); }
    @SubscribeEvent public boolean onDeath(LivingDeathEvent event) {
        return LastStandBehavior.rescue(event.getEntity());
    }
}
