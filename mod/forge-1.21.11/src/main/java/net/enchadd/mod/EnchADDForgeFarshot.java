package net.enchadd.mod;

import net.enchadd.mod.common.FarshotBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

final class EnchADDForgeFarshot {
    static void register() {
        EntityJoinLevelEvent.BUS.addListener(new EnchADDForgeFarshot()::onJoin);
        LivingHurtEvent.BUS.addListener(new EnchADDForgeFarshot()::onHurt);
    }

    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel) || !(event.getEntity() instanceof AbstractArrow arrow)) return;
        FarshotBehavior.recordLaunch(arrow);
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        event.setAmount(FarshotBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getAmount()));
    }
}
