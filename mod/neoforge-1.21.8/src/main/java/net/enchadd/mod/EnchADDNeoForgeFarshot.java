package net.enchadd.mod;

import net.enchadd.mod.common.FarshotBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

final class EnchADDNeoForgeFarshot {
    static void register() {
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, EnchADDNeoForgeFarshot::onJoin);
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, EnchADDNeoForgeFarshot::onDamage);
    }

    private static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel) || !(event.getEntity() instanceof AbstractArrow arrow)) return;
        FarshotBehavior.recordLaunch(arrow);
    }

    private static void onDamage(LivingIncomingDamageEvent event) {
        event.setAmount(FarshotBehavior.adjustDamage(event.getEntity(), event.getSource(), event.getAmount()));
    }
}
