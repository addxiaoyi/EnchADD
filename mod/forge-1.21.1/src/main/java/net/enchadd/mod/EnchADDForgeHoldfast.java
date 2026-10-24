package net.enchadd.mod;

import net.enchadd.mod.common.HoldfastBehavior;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

final class EnchADDForgeHoldfast {
    static void register() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(new EnchADDForgeHoldfast()::onAttack);
    }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent event) {
        HoldfastBehavior.maybeSuppress(event.getEntity(), event.getSource());
    }
}
