package net.enchadd.mod;

import net.enchadd.mod.common.HeadDropBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeHeadDrops {
    private EnchADDForgeHeadDrops() {}
    static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeHeadDrops()::onDeath); }
    @SubscribeEvent public void onDeath(LivingDeathEvent event) {
        HeadDropBehavior.onDeath(event.getEntity(), event.getSource());
    }
}
